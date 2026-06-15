# 学校谈话管理系统

基于若依（RuoYi-Vue）框架构建，面向高校辅导员、学院书记/副书记的日常谈话管理场景。

## 项目信息

| 属性 | 值 |
|------|-----|
| 框架 | RuoYi-Vue (Spring Boot + Vue2 + Element UI) |
| 后端路径 | `ruoyi-admin/` + `ruoyi-system/src/main/java/com/ruoyi/talk/` |
| 前端路径 | `ruoyi-ui/src/views/talk/` + `ruoyi-ui/src/api/talk/` |
| 学生端 | `Student-UI/` (独立 Vue2 项目) |
| SQL脚本 | `sql/` 目录（12个文件） |
| 文档 | `CONTEXT.md` (领域术语) + 本文件 |

## Agent skills

### Issue tracker

使用 GitHub Issues，仓库 `jimu-Z/tanhuan`。详见 `docs/agents/issue-tracker.md`。

### Triage labels

使用标准 5 级标签体系：`needs-triage` / `needs-info` / `ready-for-agent` / `ready-for-human` / `wontfix`。详见 `docs/agents/triage-labels.md`。

### Domain docs

单上下文布局，`CONTEXT.md` + `docs/adr/` 位于项目根目录。详见 `docs/agents/domain.md`。

## Agent 开发规范

### 数据权限铁律（第五轮修复后强制）

**任何新增或修改的查询接口必须通过以下检查清单：**

```
[ ] 该接口是否涉及学生/谈话记录数据？
[ ] 如果是，是否调用了 applyCounselorFilter() 或 applySecretaryFilter()？
[ ] 统计类接口是否有角色维度过滤（书记限学院、辅导员隐藏）？
[ ] Mapper XML 是否使用了参数化查询（#{} 而非 ${}）？
[ ] 新增接口是否在 TalkStatisticsController 中添加了防御性过滤？
```

### 角色数据范围速查表

| 角色 | 学生列表 | 谈话记录 | 统计分析 | 预警 | 教师管理 | 预约 |
|------|:--------:|:--------:|:-------:|:----:|:--------:|:----:|
| admin | 全校 | 全校 | 全校排名 | 全部 | CRUD | 查看 |
| secretary | 本学院 | 本学院 | **本学院排名** | **本学院** | 本学院CRUD | - |
| counselor | **本班** | **自己创建** | **隐藏** | **本班** | - | - |
| student | 仅自己 | 仅自己(脱敏) | - | - | - | 自己创建 |

### 代码修改必读

1. **Service层**：所有列表查询方法必须调用 `applyCounselorFilter(params)` 或 `applySecretaryFilter(params)`
2. **Controller层**：统计/聚合接口必须在 Controller 内做角色判断，不能依赖 Service 层
3. **Mapper XML**：禁止 `${}` 直接拼接用户输入，统一使用 `#{}` 参数化
4. **前端页面**：API 路径必须与 Controller 的 `@RequestMapping` 匹配
5. **SQL脚本**：全部使用 `INSERT IGNORE` 保证幂等

## BUG 修复历史

### 第七轮修复 — 2026-06-15（书记/辅导员页面403权限错误）

> 触发条件：书记/辅导员登录后，多个页面弹"当前操作没有权限"

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | CRITICAL | 前端调用 `/system/dept/list` 需 `system:dept:list` 权限，书记/辅导员无此权限 | 前端改用 talk 模块自己的 `getDeptTree` API | talkStudent/index.vue, teacher/index.vue |
| 2 | CRITICAL | `/talkrecord/list` 需 `talk:record:list` 权限，书记/辅导员只有 `talk:record:query` | 给书记和辅导员补上菜单2020权限 | talk_menu_fix.sql |
| 3 | CRITICAL | `/talktag/*` 需 `talk:tag:list` 权限，书记/辅导员无此权限 | 给书记和辅导员补上菜单2080权限 | talk_menu_fix.sql |
| 4 | CRITICAL | `/talk/alert/list` 需 `talk:alert:list` 权限，预警菜单(2055)缺少子菜单 | 新建预警子菜单(2056-2059)并分配给书记/辅导员 | talk_menu_fix.sql |
| 5 | MEDIUM | 后端 `deptTree` 只查 `deptType=class` 导致学院节点缺失 | 移除 `dept.setDeptType("class")` 过滤 | TalkStudentController.java |

### 第六轮修复 — 2026-06-15（编译错误 + 安全漏洞 + 跟进菜单隐藏）

> 触发条件：全量扫描后发现编译错误、安全漏洞和跟进菜单需隐藏

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | CRITICAL | TalkStatisticsController.dashboard() 重复声明 scopeParams 变量 | 删除第144行重复声明，复用第134行变量 | TalkStatisticsController.java L144 |
| 2 | CRITICAL | TalkAlertController /debug 接口 @Anonymous 绕过认证 | 删除整个 debug() 方法及相关 import/字段 | TalkAlertController.java |
| 3 | HIGH | 谈话跟进菜单(2075)需隐藏但功能保留 | sys_menu visible='1' + UPDATE 语句保证幂等 | talk_menu_fix.sql L61-62, L153-154 |

### 第五轮修复 — 2026-06-15（数据权限泄漏 + 菜单缺失 + 安全加固）

> 触发条件：以书记/辅导员账号登录系统，查看菜单发现报错和数据泄漏

| # | 严重度 | 问题 | 修复方案 | 文件 |
|---|:------:|------|---------|------|
| 1 | CRITICAL | dashboard collegeRanking 泄漏全校学院数据 | 添加 createQueryParams() 角色过滤 | TalkStatisticsController.java L141-160 |
| 2 | CRITICAL | alerts deptCoverage 泄漏全校部门数据 | 添加 ancestors 判定+角色过滤 | TalkStatisticsController.java L201-219 |
| 3 | CRITICAL | selectUntalkedStudentsInPeriod 无权限控制 | 新增 params 参数传递过滤器 | TalkStudentServiceImpl + Mapper 接口 + XML |
| 4 | HIGH | talk_menu_fix.sql 缺V3菜单INSERT(2090/2100) | 补全8个菜单+2个角色授权块 | talk_menu_fix.sql L119-136 |
| 5 | HIGH | 预约页教师用纯文本输入 | 改为el-select下拉选择器 | appointment/index.vue |
| 6 | MEDIUM | console.error调试残留 | 替换为$modal.msgError() | teacher/index.vue L520 |
| 7 | MEDIUM | Token弱密钥abcdefghijklmnopqrstuvwxyz | 替换为强随机密钥 | application.yml L100 |

### 历史轮次摘要

| 轮次 | 日期 | 核心内容 | 修复数 |
|------|------|---------|:------:|
| 第一轮 | — | SQL注入、级联删除、通知机制、空catch、页面整合 | ~20 |
| 第二轮 | — | DateUtils导入、teacher_notified逻辑、标签DB驱动、批量API、集体谈话汇总导出 | ~15 |
| 第三轮 | — | 全量扫描：软删除失效、SQL注入参数化、NPE、Student-UI表格Bug、字段误用 | ~20 |
| 第四轮 | 2026-06-02 | 深度扫描84项：文件上传校验、DROP TABLE风险、主键冲突、索引建议 | ~55累计 |
| **第五轮** | **2026-06-15** | **数据权限泄漏×3、菜单缺失×2、安全加固×2** | **10** |
| **第六轮** | **2026-06-15** | **编译错误×1、安全漏洞×1、跟进菜单隐藏×1** | **3** |
| **第七轮** | **2026-06-15** | **跨模块权限依赖×2、部门树过滤×1** | **3** |

## 关键架构决策记录 (ADR)

| ADR编号 | 决策 | 状态 |
|---------|------|:----:|
| ADR-0001 | 页面整合：会话管理+记录管理→谈话管理(4→2) | 已实施 |
| ADR-0002 | Student-UI独立部署，不混入ruoyi-ui | 已实施 |
| ADR-0003 | 标签从硬编码常量改为数据库驱动 | 已实施 |
| ADR-0004 | 数据权限双层过滤：业务自定义 + DataScope AOP | 已实施 |
| ADR-0005 | V3重构：talk_teacher表 + talk_alert表 + talk_appointment表 | 实施中 |
