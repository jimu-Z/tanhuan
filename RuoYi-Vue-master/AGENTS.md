# AGENTS.md — 学生谈心谈话管理系统

## v10 当前基线（2026-09-09）

本节优先于下方保留的历史轮次记录。领域真相见 `CONTEXT.md`，设计决策见 `docs/adr/0003-four-role-college-scope.md`，数据库升级入口为 `sql/talk_role_permission_v9.sql`。

- 权限只由 `admin`、`school_admin`、`college_admin`、`talk_teacher` 决定，教师岗位不参与授权；旧 `talk_secretary`/`talk_counselor` 已迁移并停用。
- 所有在用学生、教师、谈话、记录、附件、学院标签和学院模板查询必须显式按学院隔离；谈话写操作还必须校验创建人所有权。
- 管理员操作范围：`admin`/校管理员全校，院管理员本学院。教师可查看本学院教师名单；可维护本学院学生但不可删除；不可维护教师账号、学院、班级。
- 统一查询和统计分析作为核心管理端能力上线；学生端及预约、预警、大屏、回访、学生反馈、我的记录暂不上线，代码与数据保留、入口隐藏。
- 系统标签/模板仅 `admin` 和校管理员维护；学院标签/模板由本学院教师和院管理员共同维护。

## Agent skills

### Issue tracker

GitHub Issues on `jimu-Z/tanhuan`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role labels: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout: one `CONTEXT.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.

## 权限模型（第八轮修复后）

### 角色定义

| 角色 | roleKey | 数据范围 | 可见菜单数 |
|------|---------|---------|:----------:|
| 管理员 | admin | 全校全部 | 13 (全部) |
| 书记 | talk_secretary | 本学院及下属部门 | 11 (无预约/标签) |
| 辅导员 | talk_counselor | 仅自己管理的班级 | 9 (无统计/大屏/教师/预约/标签) |
| 学生 | talk_student | 仅自己的记录 | 2 (预约+记录) |

### 数据权限过滤链路

```
HTTP请求 → Controller
              ↓
         ServiceImpl.selectList()
              ↓
    ┌─────────┴──────────┐
    │ applyCounselorFilter() │ ← 辅导员路径：talk_teacher_class JOIN
    │ applySecretaryFilter() │ ← 书记路径：sys_dept.ancestors 匹配
    └─────────┬──────────┘
              ↓
         Mapper XML (#{} 参数化)
              ↓
         @DataScope AOP (框架层兜底)
              ↓
         返回过滤后数据
```

### 关键文件索引

| 文件 | 职责 |
|------|------|
| [TalkStatisticsController.java](ruoyi-system/src/main/java/com/ruoyi/talk/controller/TalkStatisticsController.java) | 统计仪表盘+预警数据，**第六轮修复重复scopeParams编译错误** |
| [TalkStudentServiceImpl.java](ruoyi-system/src/main/java/com/ruoyi/talk/service/impl/TalkStudentServiceImpl.java) | 学生CRUD+未谈学生查询，**第五轮修复selectUntalkedStudentsInPeriod权限缺失** |
| [TalkSessionServiceImpl.java](ruoyi-system/src/main/java/com/ruoyi/talk/service/impl/TalkSessionServiceImpl.java) | 谈话会话CRUD，含applyCounselorFilter |
| [TalkStudentRecordServiceImpl.java](ruoyi-system/src/main/java/com/ruoyi/talk/service/impl/TalkStudentRecordServiceImpl.java) | 谈话记录CRUD，含applyDataScopeFilter参数化 |
| [TalkAlertController.java](ruoyi-system/src/main/java/com/ruoyi/talk/controller/TalkAlertController.java) | 预警管理，**第六轮删除@Anonymous debug接口** |
| [TalkAlertServiceImpl.java](ruoyi-system/src/main/java/com/ruoyi/talk/service/impl/TalkAlertServiceImpl.java) | 预警管理，含书记维度过滤 |
| [TalkTeacherController.java](ruoyi-system/src/main/java/com/ruoyi/talk/controller/TalkTeacherController.java) | 教师管理(仅管理员/书记)，含getCounselors按学院查询 |
| [TalkAppointmentController.java](ruoyi-system/src/main/java/com/ruoyi/talk/controller/TalkAppointmentController.java) | 预约管理(管理员+学生)，含状态流转 |
| [TalkStudentMapper.xml](ruoyi-system/src/main/resources/mapper/talk/TalkStudentMapper.xml) | 学生SQL映射，**第五轮新增params条件** |
| [talk_menu_fix.sql](sql/talk_menu_fix.sql) | **第六轮隐藏跟进菜单2075(visible='1')** |

## Agent 操作约束

### 禁止事项
- 禁止在 Mapper XML 中使用 `${}` 拼接用户输入
- 禁止在统计类接口中返回未经角色过滤的全量数据
- 禁止修改 SQL 脚本时使用非幂等语句（必须 INSERT IGNORE）
- 禁止在前端页面残留 console.log/console.error 调试代码
- 禁止使用 @Anonymous 注解暴露需要认证的接口
- 禁止前端 talk 模块页面直接调用 `/system/*` 系统级 API（应使用 talk 模块自己的接口，避免跨模块权限依赖）

### 必做事项
- 新增任何涉及学生/谈话的查询接口时，必须在 Service 层调用过滤器
- 新增菜单时，同步更新 `talk_menu_fix.sql` 和 CLAUDE.md 的菜单矩阵
- 前端需要部门树时，使用 `getDeptTree()` 而非 `listDept()`，避免 `system:dept:list` 权限依赖
- 修改角色相关逻辑前，先查阅 CONTEXT.md 的「角色」章节和「数据权限架构」
- 每次修复 BUG 后，更新 CLAUDE.md 的 BUG 修复历史表
- 新增 Mapper insert 语句时，必须包含 `useGeneratedKeys="true" keyProperty="xxx"`，否则主键不会回填到 Java 对象
- 前端标签显示必须兼容 `tagValue` 可能是 JSON 数组字符串的情况（`try { JSON.parse(value) } catch {}`）
