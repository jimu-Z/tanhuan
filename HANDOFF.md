# 当前交接入口（2026-10-21）

当前领域规则以 `RuoYi-Vue-master/CONTEXT.md` 和 `docs/adr/0003-four-role-college-scope.md` 为准。下方 2026-06-16 内容仅为历史，不再作为角色或部署操作依据。

- 当前四角色：admin、school_admin、college_admin、talk_teacher；岗位不决定权限。
- 只部署后端和管理端（6060/6080），学生端及预约、预警等隐藏功能继续停用。
- 全角色浏览器阶段已结项：68 passed、1 completed-with-user-exception，详见 `qa-runtime/ui-full-role-20261012/all-role-full-function-report-final-20261020.md`，不是 69 项全部自动化通过。
- 最近部署包：`deploy/release-complete-20261020-212153/`，含学生导入 v10、附件存储修复和标签/模板连续序号。部署前核对实际版本与增量脚本，不用旧包覆盖新数据。
- 本地稳定性修复与定向验证已完成，断点与基线：`qa-runtime/stability-20261021/ledger.md`。候选包目录为 `deploy/candidate-20261021-stability/`，只有目录内 manifest.json 与完整 ZIP 校验完成才可交付；最终结果见同一证据目录 final-report.md。
- 公开仓库数据库入口：`database/README.md`，包含完整45表结构与角色/菜单/字典基础数据，不包含师生资料、谈话、日志或账号哈希；已有环境仅执行必要增量脚本。
- 不将历史密码、服务器目录或 Nginx 样例视为当前生产配置。

---

# 交接文档 — 学生谈心谈话管理系统

> 更新时间：2026-06-16

---

## 一、项目概述

基于若依（RuoYi-Vue）框架构建的高校谈心谈话管理系统，面向辅导员、学院书记/副书记的日常谈话管理场景。

| 属性 | 值 |
|------|-----|
| 框架 | RuoYi-Vue (Spring Boot + Vue2 + Element UI) |
| 后端路径 | `RuoYi-Vue-Master/ruoyi-admin/` + `ruoyi-system/src/main/java/com/ruoyi/talk/` |
| 前端路径 | `RuoYi-Vue-Master/ruoyi-ui/src/views/talk/` + `ruoyi-ui/src/api/talk/` |
| 学生端 | `Student-UI/` (独立 Vue2 项目) |
| SQL脚本 | `RuoYi-Vue-Master/sql/` 目录 |
| 构建产物 | `deploy/ruoyi-admin.jar` + `deploy/admin-ui/` |

---

## 二、生产部署状态

### 服务器信息

| 项目 | 值 |
|------|-----|
| 服务器IP | 211.64.39.248（教育网内网，校外无法直接访问） |
| 操作系统 | Linux（宝塔面板管理） |
| JDK | 21（宝塔 Java 项目管理器） |
| Nginx | 1.26.1（宝塔管理） |
| MySQL | 211.64.39.248:3306 |
| Redis | 211.64.39.248:6379（bind 127.0.0.1 211.64.39.248，protected-mode no） |

### 端口分配（60系）

| 服务 | 端口 | 说明 |
|------|:----:|------|
| 后端 Spring Boot | 6060 | 宝塔 Java 项目管理器运行 JAR |
| 管理端前端 | 6080 | Nginx 静态文件 + 反代 `/prod-api/` → 6060 |
| 学生端前端 | 6090 | 待部署（Nginx 配置已准备好：`nginx-student-6090.conf`） |

### 服务器文件路径

| 资源 | 服务器路径 |
|------|-----------|
| 后端 JAR | `/www/wwwroot/talk/deploy/ruoyi-admin.jar` |
| 管理端前端 | `/www/wwwroot/talk/deploy/admin-ui/` |
| Nginx 管理端配置 | 宝塔 HTML 项目站点 → 设置 → 配置文件 |
| Nginx 学生端配置 | `nginx-student-6090.conf`（待部署到 `/www/server/panel/vhost/nginx/`） |

### Nginx 管理端配置（6080）

```nginx
server {
    listen 6080;
    server_name 211.64.39.248;

    location / {
        root /www/wwwroot/talk/deploy/admin-ui;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    location /prod-api/ {
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header REMOTE-HOST $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_pass http://211.64.39.248:6060/;
    }

    access_log /www/wwwlogs/tanhuan-admin.log;
    error_log /www/wwwlogs/tanhuan-admin.error.log;
}
```

### 部署流程

1. 本地构建后端：`mvn package -DskipTests`（项目根目录 `RuoYi-Vue-Master/`）
2. 本地构建前端：`npm run build:prod`（`ruoyi-ui/` 目录）
3. 构建产物复制到 `deploy/` 目录
4. 上传 JAR 和 dist 到服务器 `/www/wwwroot/talk/deploy/`
5. 宝塔 Java 项目管理器重启 tanhua 项目
6. 清除浏览器缓存后验证

---

## 三、角色与权限

### 角色定义

| 角色 | roleKey | role_id | 数据范围 | 可见菜单数 |
|------|---------|:-------:|---------|:----------:|
| 管理员 | admin | 3 | 全校全部 | 13 (全部) |
| 书记 | talk_secretary | 4 | 本学院及下属部门 | 11 (无预约/标签) |
| 辅导员 | talk_counselor | 5 | 仅自己管理的班级 | 9 (无统计/大屏/教师/预约/标签) |
| 学生 | talk_student | - | 仅自己的记录 | 2 (预约+记录) |

### 数据权限过滤链路

```
HTTP请求 → Controller
              ↓
         ServiceImpl.selectList()
              ↓
    ┌─────────┴──────────┐
    │ applyCounselorFilter() │ ← 辅导员路径：talk_teacher_class JOIN
    │ applySecretaryFilter() │ ← 书记路径：sys_dept.ancestors 匹配
    │ @DataScope AOP         │ ← 教师管理：deptAlias="d"（第八轮新增）
    └─────────┬──────────┘
              ↓
         Mapper XML (#{} 参数化)
              ↓
         返回过滤后数据
```

---

## 四、修复历史摘要

| 轮次 | 日期 | 核心内容 | 修复数 |
|------|------|---------|:------:|
| 第一轮 | — | SQL注入、级联删除、通知机制、空catch、页面整合 | ~20 |
| 第二轮 | — | DateUtils导入、teacher_notified逻辑、标签DB驱动、批量API | ~15 |
| 第三轮 | — | 软删除失效、SQL注入参数化、NPE、Student-UI表格Bug | ~20 |
| 第四轮 | 2026-06-02 | 文件上传校验、DROP TABLE风险、主键冲突、索引建议 | ~55 |
| 第五轮 | 2026-06-15 | 数据权限泄漏×3、菜单缺失×2、安全加固×2 | 10 |
| 第六轮 | 2026-06-15 | 编译错误×1、安全漏洞×1、跟进菜单隐藏×1 | 3 |
| 第七轮 | 2026-06-15 | 跨模块权限依赖×2、部门树过滤×1 | 3 |
| **第八轮** | **2026-06-16** | **生产部署+导入BUG×4、权限×2、仪表盘重构×1、前端优化×5** | **12** |

### 第八轮修复详情（本轮）

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | CRITICAL | SysDeptMapper.xml insertDept 缺少 useGeneratedKeys | 添加 useGeneratedKeys="true" keyProperty="deptId" | SysDeptMapper.xml |
| 2 | CRITICAL | 教师导入"所属学院不能为空" | 改用精确匹配(deptName+parentId=100) + 二次确认 | TalkTeacherServiceImpl.java |
| 3 | CRITICAL | 学生导入"dept_id doesn't have default value" | 增加主键回填安全检查 + 参数校验 | TalkStudentServiceImpl.java |
| 4 | CRITICAL | 学生导入创建 parent_id=0 的脏部门数据 | findOrCreateDept 增加 null 结果跳过 | TalkStudentServiceImpl.java |
| 5 | HIGH | 系统管理员看不到教师管理数据 | 删除自定义过滤，改用 @DataScope 注解 | TalkTeacherServiceImpl.java, TalkTeacherMapper.xml |
| 6 | HIGH | 统一查询 403 | 新增 talk:record:list 权限(菜单2026) | 数据库SQL |
| 7 | MEDIUM | 仪表盘"各学院谈话排名"竞争性 | 替换为工作提醒+预警概览+最近动态 | TalkStatisticsController, dashboardV2/index.vue |
| 8 | MEDIUM | 标签显示 ["mental_health"] 而非中文 | getTagLabel 增加 JSON 数组解析兼容 | talkManagement, talkSession, unifiedQuery |
| 9 | MEDIUM | 我的谈话记录多余列 | 删除跟进状态+教师已读列，谈话内容列拉长 | myRecords/index.vue |
| 10 | MEDIUM | 统一查询多余列和搜索 | 删除跟进状态列和搜索，添加谈话人列 | unifiedQuery/index.vue |
| 11 | LOW | 标签管理标签标识显示英文 | 改为显示 tagName（中文） | talkTag/index.vue |
| 12 | LOW | Student-UI 端口非60系 | 端口改6090，代理改211.64.39.248:6060 | Student-UI/vue.config.js, .env.production |

---

## 五、关键代码位置速查

### 后端

| 功能 | 文件 |
|------|------|
| 统计 Controller | `ruoyi-system/.../talk/controller/TalkStatisticsController.java` |
| 预警 Controller | `ruoyi-system/.../talk/controller/TalkAlertController.java` |
| 会话 Controller | `ruoyi-system/.../talk/controller/TalkSessionController.java` |
| 学生 Service | `ruoyi-system/.../talk/service/impl/TalkStudentServiceImpl.java` |
| 教师 Service | `ruoyi-system/.../talk/service/impl/TalkTeacherServiceImpl.java` |
| 部门 Mapper | `ruoyi-system/.../mapper/system/SysDeptMapper.xml` |
| 学生 Mapper | `ruoyi-system/.../mapper/talk/TalkStudentMapper.xml` |
| 记录 Mapper | `ruoyi-system/.../mapper/talk/TalkStudentRecordMapper.xml` |
| 菜单SQL | `sql/talk_menu_fix.sql` |

### 前端

| 功能 | 文件 |
|------|------|
| 仪表盘 | `ruoyi-ui/src/views/talk/dashboardV2/index.vue` |
| 谈话管理 | `ruoyi-ui/src/views/talk/talkManagement/index.vue` |
| 我的记录 | `ruoyi-ui/src/views/talk/myRecords/index.vue` |
| 统一查询 | `ruoyi-ui/src/views/talk/unifiedQuery/index.vue` |
| 标签管理 | `ruoyi-ui/src/views/talk/talkTag/index.vue` |
| 前端路由 | `ruoyi-ui/src/router/index.js` |
| 权限控制 | `ruoyi-ui/src/store/modules/permission.js` |

### 配置文件

| 配置 | 文件 |
|------|------|
| 后端端口/Token | `ruoyi-admin/src/main/resources/application.yml` |
| 数据库连接 | `ruoyi-admin/src/main/resources/application-druid.yml` |
| Redis连接 | `ruoyi-admin/src/main/resources/application.yml` |
| 管理端前端代理 | `ruoyi-ui/vue.config.js` |
| 管理端生产API | `ruoyi-ui/.env.production` |
| 学生端前端代理 | `Student-UI/vue.config.js` |
| 学生端生产API | `Student-UI/.env.production` |
| Nginx 管理端 | `nginx-admin-6080.conf` |
| Nginx 学生端 | `nginx-student-6090.conf` |

---

## 六、已知待办

1. **学生端前端(6090)尚未部署** — 需构建 Student-UI 并在宝塔创建 HTML 项目站点
2. **数据库脏数据** — 历史导入产生的 parent_id=0 部门需手动清理
3. **辅导员 dashboard 显示0学生** — 可能是 talk_teacher_class 与 sys_dept 不匹配
4. **校外无法访问** — 211.64.39.248 是教育网内网IP，需联系学校网络中心申请公网映射或使用校园VPN
5. **deploy/ 目录在 Git 中** — 构建产物不应提交，建议加入 .gitignore

---

## 七、开发注意事项

### 必做规则

- 新增 Mapper insert 语句必须包含 `useGeneratedKeys="true" keyProperty="xxx"`
- 前端标签显示必须兼容 tagValue 可能是 JSON 数组字符串
- 新增查询接口必须在 Service 层调用权限过滤器
- 新增菜单时同步更新 `talk_menu_fix.sql`
- 前端需要部门树时用 `getDeptTree()` 而非 `listDept()`
- SQL 脚本必须使用 `INSERT IGNORE` 保证幂等

### 禁止规则

- 禁止在 Mapper XML 中使用 `${}` 拼接用户输入
- 禁止在统计接口返回未经角色过滤的全量数据
- 禁止使用 @Anonymous 暴露需要认证的接口
- 禁止前端 talk 模块直接调用 `/system/*` 系统级 API

---

## 八、建议技能

下一个 agent 接手时建议 invoke：

- `diagnose` — 调试运行时问题
- `grill-me` — 讨论方案
- `tdd` — 测试驱动开发
