# 本地 QA 与安全修复交接文档

更新时间：2026-09-10

## 任务背景与约束

系统为德州学院谈话管理系统。当前只测试并维护以下业务：教师、学生、谈话会话、谈话记录、附件、标签、模板、统一查询、统计分析。

- 排除：学生端、历史废弃页面，以及 RuoYi 的系统管理、监控、代码生成等通用菜单功能测试。
- 测试环境仅限本机。MySQL、Redis 已由用户启动；数据库为 `xuexiaotanhua`。
- 用户允许创建、修改和清理 `QA_` 前缀的教师、学生、谈话、附件等测试数据。
- 两份台账是导入测试样本：
  - `C:\Users\13469\Desktop\德州学院教师管理台账.xlsx`
  - `C:\Users\13469\Desktop\德州学院学生教育管理台账(1).xlsx`
- 不应把用户凭据、JWT、数据库密码写入本文件或测试报告。数据库连接信息由用户在对话中提供；启动时通过环境变量注入，不修改持久化配置。

## 已完成的代码修复

### 1. P0：上传资源可被同源执行

问题：上传接口以客户端 MIME 类型为主要判断条件，默认后缀集合中允许 HTML；`/profile/**` 又允许匿名读取，存在上传 HTML 并在管理端同源执行的风险。

已修改：

- `ruoyi-common/src/main/java/com/ruoyi/common/utils/file/MimeTypeUtils.java`
  - 从默认上传后缀中移除 `html`、`htm`。
- `ruoyi-framework/src/main/java/com/ruoyi/framework/security/filter/ProfileResourceSecurityFilter.java`
  - 新增 `/profile/` 静态资源过滤器。
  - 对 `.html`、`.htm`、`.xhtml`、`.svg`、`.js`、`.mjs` 返回 404。
  - 对 profile 资源设置 `X-Content-Type-Options: nosniff`。
- `ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java`
  - 明确拒绝上述 profile 危险后缀的 GET 请求。

运行时证据：

- `GET /profile/QA_profile_probe.html`：404，空响应，未返回探针内容。
- `GET /profile/QA_profile_probe.txt`：200，`text/plain`，带 `nosniff`。

QA 探针文件位于 `F:\kaifa\XueXiaoTanHuan\qa-runtime\uploads\`，最终清理时一并处理。

### 2. P1：公开 Druid 和 OpenAPI 文档

问题：未认证用户可访问 Druid 登录页以及约 94KB 的 `/v3/api-docs` OpenAPI 定义；Druid 配置还启用了管理控制台。

已修改：

- `ruoyi-admin/src/main/resources/application.yml`
  - 设置 `springdoc.api-docs.enabled: false`。
- `ruoyi-admin/src/main/resources/application-druid.yml`
  - 设置 `spring.datasource.druid.statViewServlet.enabled: false`。
- `ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java`
  - 移除 Swagger/OpenAPI/Druid 的匿名放行规则。

复测：上述路径不再返回 Druid 页面或 OpenAPI 定义；当前 RuoYi 未认证异常处理仍以 HTTP 200 配合 JSON `code: 401` 表示失败，这是一个应记录的接口语义问题，不要误判为端点仍公开。

## 构建与启动状态

### 当前已验证构建

在项目根目录执行过：

```powershell
mvn -pl ruoyi-admin -am test -q
mvn -pl ruoyi-admin -am package -DskipTests -q
```

单元测试通过。构建产物为：

`ruoyi-admin/target/ruoyi-admin.jar`

注意：不要在 JAR 仍被 Java 进程占用时重新打包；Windows 下会留下很小的非可执行普通 JAR。重新构建前，先核对并停止实际监听 6060 端口的 Java 进程。正确的 Boot JAR 体积约 89MB，并包含 `BOOT-INF/lib/`。

### 本地启动方式

运行时必须覆盖项目配置中的远程 MySQL/Redis 地址。已成功使用环境变量将数据源、Redis 和 profile 路径指向本机；不要把凭据固化回 YAML。

后端端口：`6060`

本轮使用的上传根目录：

`F:\kaifa\XueXiaoTanHuan\qa-runtime\uploads`

日志目录：

`F:\kaifa\XueXiaoTanHuan\logs\`

若需要重新启动，请从用户或当前会话读取本机数据库密码，使用 `SPRING_DATASOURCE_DRUID_MASTER_*`、`SPRING_DATA_REDIS_HOST`、`RUOYI_PROFILE` 环境变量覆盖后再启动 JAR。

## 已建 QA 测试数据

以下数据均应在最终收尾时清理：

| 类型 | 标识 | 说明 |
| --- | --- | --- |
| 教师 | `QA_TEACHER_001` | 老师角色，学院 271 |
| 教师 | `QA_COLLEGE_001` | 已提升为院管理员，学院 271 |
| 教师 | `QA_SCHOOL_001` | 已调整为校管理员 |
| 学生 | `QA_STUDENT_001` | 学院 271 下的班级 294 |
| 学生 | `QA_STUDENT_002` | 跨学院隔离验证用，学院 101 |
| 文件 | `QA_profile_probe.html/.txt` | 上传静态资源安全探针 |
| 台账副本 | `F:\kaifa\XueXiaoTanHuan\qa-runtime\imports\QA_*.xlsx` | 导入测试副本 |

教师删除接口会级联物理删除关联的 `sys_user` 与角色关联；优先用管理员令牌通过业务接口清理，避免直接删库造成孤儿数据。

## 已验证的功能与证据

### 角色与学院范围

已创建并认证 admin、校管理员、院管理员、老师四类测试身份（不要把令牌写入文件）。

| 用例 | 实际结果 |
| --- | --- |
| 老师查询本学院学生 | 通过 |
| 老师新增教师 | 被拒绝，业务响应 `code: 403` |
| 老师删除学生 | 被拒绝，业务响应 `code: 403`；学生仍存在 |
| 老师查询异学院学生 | 返回 0 条 |
| 老师查看异学院学生详情 | 被拒绝 |
| 老师向异学院新增学生 | 被拒绝 |
| 校管理员角色 | `getInfo` 返回 `admin` 和全权限，这是当前“校管理员等同 admin”的预期实现 |

注意：多数权限拒绝响应 HTTP 状态仍为 200，实际授权结果在 JSON 的 `code` 字段。这应作为 P2 接口语义/网关兼容风险记录。

### 路由与菜单

运行时 `getRouters` 验证：

- 统一查询（`query`）和统计分析（`dashboard`）对三类业务角色均可见。
- 已废弃的 `talkrecord`、`myrecords`、`appointment`、`followup`、`alert`、`bigscreen` 等路由为隐藏状态。
- SQL 迁移脚本中曾发现注释/显隐规则不一致；当前本地数据库的实际路由结果符合预期，但部署其他库前需复核对应迁移脚本执行顺序。

### 导入

教师入口：`POST /talk/teacher/importData`

- 教师台账首行为标题、第二行为表头；后端表头行自动识别正常。
- 使用副本、`updateSupport=false` 导入成功 8 条。

学生入口：`POST /ruoyi-system/talk/import/preview`，随后 `POST /ruoyi-system/talk/import/execute?importMode=skip`

- 学生台账预览识别 3 行，字段映射完整，错误 0、警告 0。
- 正确的执行请求体是 `preview.data.previewRows.map(row => row.data)`；外层对象包含 `rowNum/data/status/message`，不能直接提交。
- 首次执行成功导入 3 行；再次以 `skip` 模式导入时，按学号识别重复并跳过。
- 手机号格式异常会被清空并在 `errors` 中提示，不会中断其他行。

前端对应实现：`ruoyi-ui/src/views/talk/talkStudent/index.vue` 约第 791 行。

## 静态扫描发现（待继续验证）

1. `application-druid.yml` 仍存有历史远程数据库连接信息及明文凭据。这是源码秘密管理风险。当前运行时通过环境变量覆盖，建议后续改为部署环境变量或密钥管理，不要在代码库中保留真实凭据。
2. `ResourcesConfig` 使用宽松 CORS 来源模式，需结合真实前端域名和 JWT 使用方式验证跨域攻击面。
3. `TalkStudentRecordController.submitFeedback` 的授权注解/业务身份校验需要动态复核；学生端被排除，不要将静态判断直接写成已确认漏洞。
4. 首页包与部分 ElementUI 分包较大，前端生产构建已有资源体积告警，作为 P3 性能项记录。
5. 前端浏览器自动化当前被本机 Codex Browser 运行时版本缺失阻塞，尚未完成 Chrome/Edge 的逐按钮动态覆盖。不得把静态扫描当作浏览器按钮通过结果。

## 后续优先级

1. 继续接口与业务流：谈话发起、会话编辑、记录、附件、标签、模板、统一查询、统计分析。
2. 用 QA 数据进行幂等、重复提交、分页、筛选、排序、空数据、边界和并发测试。
3. 针对所有角色验证水平越权、垂直越权、未登录、参数模糊、XSS、SQL 注入、CSRF。
4. 修复/恢复浏览器自动化环境后，按页面、角色、弹窗和行操作建立真实 DOM 按钮清单，并计算按钮覆盖率。
5. 最终清理所有 `QA_` 数据、探针文件和导入副本，复核没有遗留测试账号或附件。
6. 输出用户要求的完整 Markdown 测试计划、按钮清单、用例、执行记录、Bug 列表、覆盖统计、回归建议及 Playwright 脚本。

## 有用文件位置

| 内容 | 位置 |
| --- | --- |
| 前端业务页面 | `ruoyi-ui/src/views/talk/` |
| 学生接口 | `ruoyi-system/src/main/java/com/ruoyi/talk/controller/TalkStudentController.java` |
| 教师接口 | `ruoyi-system/src/main/java/com/ruoyi/talk/controller/TalkTeacherController.java` |
| 学生导入服务 | `ruoyi-system/src/main/java/com/ruoyi/talk/service/impl/TalkStudentServiceImpl.java` |
| 权限工具 | `ruoyi-system/src/main/java/com/ruoyi/talk/security/TalkPermissionUtils.java` |
| Spring Security | `ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java` |
| profile 安全过滤器 | `ruoyi-framework/src/main/java/com/ruoyi/framework/security/filter/ProfileResourceSecurityFilter.java` |
| QA 台账副本 | `F:\kaifa\XueXiaoTanHuan\qa-runtime\imports\` |
