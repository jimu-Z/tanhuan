"""Build a local candidate; never deploy, alter a database, or overwrite a release."""
from __future__ import annotations
import argparse
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import zipfile


def run_checked(command, cwd, log):
    with log.open("wb") as output:
        result = subprocess.run(command, cwd=cwd, stdout=output, stderr=subprocess.STDOUT)
    if result.returncode:
        raise RuntimeError(f"Command failed ({result.returncode}); see {log}")


def source_snapshot(root):
    def git(*args):
        return subprocess.check_output(["git", *args], cwd=root)
    names = set(git("ls-files", "-z", "--cached", "--others", "--exclude-standard").decode().split("\0")) - {""}
    hashes = {name: hashlib.sha256((root/name).read_bytes()).hexdigest()
              for name in sorted(names) if (root/name).is_file()}
    return {"commit": git("rev-parse", "HEAD").decode().strip(),
            "status": git("status", "--short").decode("utf-8"), "files": hashes}


def assert_environment_config(jar):
    with zipfile.ZipFile(jar) as archive:
        for name in ("application.yml", "application-druid.yml"):
            text = archive.read("BOOT-INF/classes/" + name).decode("utf-8")
            for line in text.splitlines():
                match = re.match(r"^\s*(password|secret):\s*(.*?)\s*$", line)
                if not match:
                    continue
                value = match.group(2)
                if value and value not in ("''", '\"\"') and not re.fullmatch(r"\$\{[A-Z_]+(?::)?\}", value):
                    raise RuntimeError(f"Non-environment credential in {name}; candidate refused")


def create_candidate(root, release_id, runner=run_checked):
    root = Path(root).resolve()
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_-]{0,80}", release_id):
        raise ValueError("Invalid release ID")
    destination = root / "deploy" / release_id
    stage = root / "deploy" / ("." + release_id + "-building")
    if destination.exists() or stage.exists():
        raise FileExistsError("Release/staging directory already exists; choose a new ID")
    stage.mkdir(parents=True)
    logs = stage / "validation"; logs.mkdir()
    (stage/"INCOMPLETE").write_text("Build in progress; not deployable", encoding="utf-8")
    before = source_snapshot(root)
    # A clean aggregate package always runs tests; no skip flags are accepted.
    runner(["mvn.cmd" if os.name == "nt" else "mvn", "-pl", "ruoyi-admin", "-am", "clean", "package"],
           root/"RuoYi-Vue-master", logs/"backend-package.log")
    jar = root/"RuoYi-Vue-master/ruoyi-admin/target/ruoyi-admin.jar"
    assert_environment_config(jar)
    runner(["node", str(root/"scripts/test_attachment_download.cjs")], root, logs/"attachment-frontend-test.log")
    runner(["npm.cmd" if os.name == "nt" else "npm", "run", "build:prod", "--", "--dest", str(stage/"admin-ui")],
           root/"RuoYi-Vue-master/ruoyi-ui", logs/"frontend-build.log")
    if not (stage/"admin-ui/index.html").is_file():
        raise RuntimeError("Frontend index missing")
    after = source_snapshot(root)
    if before != after:
        raise RuntimeError("Source changed during build; rebuild from a stable tree")
    shutil.copy2(jar, stage/"ruoyi-admin.jar")
    sql = stage/"database"; sql.mkdir()
    shutil.copy2(root/"RuoYi-Vue-master/sql/student_import_managers_v10.sql", sql)
    (stage/"source-state.json").write_text(json.dumps(before, ensure_ascii=False, indent=2), encoding="utf-8")
    (stage/"environment.example").write_text("DB_URL=jdbc:mysql://127.0.0.1:3306/xuexiaotanhua\nDB_USERNAME=\nDB_PASSWORD=\nREDIS_HOST=127.0.0.1\nREDIS_PASSWORD=\nTOKEN_SECRET=\nRUOYI_PROFILE=/absolute/persistent/uploadPath\n", encoding="utf-8")
    (stage/"README-部署说明.md").write_text("""# 本地候选部署包

本目录由 scripts/create_candidate.py 生成，包含源码版本及未提交文件哈希。通过构建不代表生产验收通过。

1. 先备份线上数据库、JAR、前端及上传目录；保留实际运行配置。
2. 设置 environment.example 中的环境变量，填写本环境凭据；TOKEN_SECRET 为独立随机密钥。示例不能直接作为已配置环境使用。
3. v10 三张表尚未建立时，经数据库备份后执行 database/student_import_managers_v10.sql；已有升级环境只核对结构，禁止重跑 v9 或导入旧全量快照。
4. 同步替换 JAR 和 admin-ui，保持原上传目录。若 Nginx 用 alias/root 直接公开附件目录，必须撤销该公开映射，附件只能由后端专用接口下载。
5. 反向代理 /prod-api/ 到本机后端，上传请求上限至少60m。后端默认6060，管理端6080，学生端不启用。
6. 验收登录、四角色学院隔离、中文附件下载、匿名/外院拒绝、谈话修改和学生导入。查看响应及后端错误日志。
7. 回滚时恢复配套旧JAR/前端和原配置；本轮不删改旧数据，v10新增表勿直接删除。若已产生新数据，数据库回滚须另行核对。

只有新下载接口可读取谈话附件；旧静态直链及通用下载入口返回404。头像等非谈话资源保留原行为。
本包不包含全量数据库覆盖脚本、生产凭据、运行时QA类或上传文件；不自动部署或更改服务器。
""", encoding="utf-8")
    (stage/"INCOMPLETE").unlink()
    hashes = {p.relative_to(stage).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
              for p in sorted(stage.rglob("*")) if p.is_file()}
    (stage/"manifest.json").write_text(json.dumps({"createdAt":dt.datetime.now(dt.timezone.utc).isoformat(),"sha256":hashes}, ensure_ascii=False, indent=2), encoding="utf-8")
    archive = stage/(release_id+".zip")
    with zipfile.ZipFile(archive,"w",zipfile.ZIP_DEFLATED) as z:
        for p in sorted(stage.rglob("*")):
            if p.is_file() and p != archive:
                z.write(p,p.relative_to(stage).as_posix())
    with zipfile.ZipFile(archive) as z:
        if z.testzip() is not None or any(hashlib.sha256(z.read(n)).hexdigest()!=h for n,h in hashes.items()):
            raise RuntimeError("Archive verification failed")
    archive_hash=hashlib.sha256(archive.read_bytes()).hexdigest()
    (stage/"archive.sha256").write_text(archive_hash+"  "+archive.name+"\n",encoding="utf-8")
    stage.rename(destination)
    return destination


if __name__ == "__main__":
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--release-id",default="candidate-"+dt.datetime.now().strftime("%Y%m%d-%H%M%S"))
    args=parser.parse_args()
    print(create_candidate(Path(__file__).resolve().parents[1],args.release_id))
