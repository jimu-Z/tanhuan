# Handoff — 学生谈心谈话管理系统

> 生成时间：2026-05-28 | 基于 RuoYi-Vue 3.9.2 | Spring Boot 4.0.3 + Vue 2 + Element UI

---

## 项目概述

在 RuoYi-Vue 框架上开发的高校学生谈心谈话管理系统。数据库为 `xuexiaotanhua`（MySQL），前后端分离架构。

---

## 当前进度

### ✅ 已完成

| 模块 | 状态 | 说明 |
|------|:--:|------|
| 数据库 DDL | ✅ | 4 张新表 + sys_dept 扩展 + 8 类字典 + 3 个角色 |
| 代码生成器骨架 | ✅ | talk_student / talk_session / talk_student_record 三表 CRUD |
| 菜单 + 权限 | ✅ | 顶部"学生谈心谈话"菜单组，按钮权限已分配 |
| **学生数据导入** | ✅ | Excel 上传→自动识别→校验确认→导入，含部门树自动创建 + 系统用户自动创建 |
| 字段映射修复 | ✅ | 父亲姓名/母亲姓名/政治面貌均已正确映射 |
| 性别显示 | ✅ | 0→男，1→女 |
| Git + GitHub | ✅ | 仓库 `github.com/jimu-Z/tanhuan`，AGENTS.md 已配置 |

### ❌ 待开发

| 模块 | 优先级 | 说明 |
|------|:--:|------|
| 集体谈话同步机制 | 🔴 高 | talk_session + talk_student_record 联动 |
| @DataScope 权限过滤 | 🔴 高 | 书记看本院、辅导员看自己 |
| .docx 文档生成 | 🔴 高 | 模板填充，按谈话类型差异化 |
| 前端页面完善 | 🟡 中 | 学生详情页、发起谈话页改进 |
| 数据备份功能 | 🟢 低 | 定时 mysqldump + 手动触发 |

---

## 关键文件索引

### 后端（ruoyi-system）

| 文件 | 说明 |
|------|------|
| `ruoyi-system/src/main/java/com/ruoyi/talk/controller/TalkStudentController.java` | 学生控制器，含导入接口 |
| `ruoyi-system/src/main/java/com/ruoyi/talk/controller/TalkSessionController.java` | 谈话会话控制器 |
| `ruoyi-system/src/main/java/com/ruoyi/talk/controller/TalkStudentRecordController.java` | 谈话记录明细控制器 |
| `ruoyi-system/src/main/java/com/ruoyi/talk/service/impl/TalkStudentServiceImpl.java` | 核心导入逻辑（~700行，含 Excel 解析、部门创建、用户创建） |
| `ruoyi-system/src/main/java/com/ruoyi/talk/domain/` | TalkStudent / TalkSession / TalkStudentRecord |
| `ruoyi-system/src/main/resources/mapper/talk/` | MyBatis XML 映射文件 |

### 前端（ruoyi-ui）

| 文件 | 说明 |
|------|------|
| `ruoyi-ui/src/views/talk/talkStudent/index.vue` | 学生列表 + 导入对话框 |
| `ruoyi-ui/src/views/talk/talkSession/index.vue` | 谈话会话列表 |
| `ruoyi-ui/src/views/talk/talkStudentRecord/index.vue` | 谈话记录列表 |
| `ruoyi-ui/src/api/talk/talkStudent.js` | 学生 API（含 importPreview/importExecute） |
| `ruoyi-ui/src/api/talk/talkSession.js` | 会话 API |
| `ruoyi-ui/src/api/talk/talkStudentRecord.js` | 记录 API |
| `ruoyi-ui/src/utils/request.js` | **已修改**：FormData 上传时自动移除 Content-Type |

### SQL

| 文件 | 说明 |
|------|------|
| `sql/talk_management.sql` | 建表 + 字典 + 角色（注意 TEXT 默认值已修复） |
| `sql/talk_menu.sql` | 菜单 + 权限 |
| `sql/talk_fix.sql` | 补全脚本（字典数据） |

### 配置

| 文件 | 说明 |
|------|------|
| `AGENTS.md` | Agent skills 配置（GitHub Issues + triage labels） |
| `docs/agents/` | issue-tracker.md / triage-labels.md / domain.md |
| `设计总结-学生谈心谈话管理系统.md` | 22 轮质询的设计决策汇总 |

---

## 数据库概要

- **数据库名**：`xuexiaotanhua`
- **账号**：root / 123456
- **连接**：`jdbc:mysql://localhost:3306/xuexiaotanhua`

### 核心表

```
talk_student          — 学生基础信息（学号唯一索引，dept_id→sys_dept）
talk_session          — 谈话会话（individual/group，talk_content 共享）
talk_student_record   — 学生谈话记录明细（session_id + student_id 联合）
talk_session_tag      — 会话-内容标签多对多
```

### 新增表相对于 RuoYi 原生表的改动
- `sys_dept` 新增字段：`dept_type`（college/grade/class/dept）

---

## 设计决策速查

| 决策 | 结论 |
|------|------|
| 部门树 | 学院→年级→班级（三级，无专业层） |
| 角色 | talk_admin / talk_secretary / talk_counselor |
| data_scope | admin=1, secretary=2, counselor=5 |
| 数据模型 | 主-从（talk_session + talk_student_record） |
| 谈话主题 | 多选标签字典，非单选 |
| 谈话人 | 默认当前班主任，可手动修改 |
| 导出格式 | 仅 .docx |
| 备份 | L1 级（定时 + 手动全库） |

---

## 修复过的 Bug 记录

| # | Bug | 根因 | 修复 |
|---|-----|------|------|
| 1 | 预览数据为空 | `totalRows = lastRowNum - 2` 多减了 1 | 改为 `lastRowNum - 1` |
| 2 | 预览列名不匹配 | 驼峰 vs 下划线 | 统一为 snake_case |
| 3 | 导入零条数据 | 前端传整行对象而非 data 字段 | `.map(row => row.data)` |
| 4 | `not a multipart request` | 全局 Content-Type 覆盖了 multipart | `config.headers['Content-Type'] = undefined` |
| 5 | `delete` 删不掉 Content-Type | 原型链继承 | 改用 `= undefined` |
| 6 | 父亲姓名/母亲姓名不导入 | `h.contains("姓名")` 提前匹配 | 调整检查顺序 |
| 7 | 性别显示数字 | 无格式化 | Vue template 转换 |
| 8 | INSERT TEXT DEFAULT 报错 | MySQL 8.0 严格模式 | 改为 VARCHAR(500) |

---

## 启动方式

```bash
# 后端（端口 8080）
cd RuoYi-Vue-master/ruoyi-admin
mvn spring-boot:run

# 前端（端口 80）
cd RuoYi-Vue-master/ruoyi-ui
npm run dev
```

登录：`admin` / `admin123`

---

## 建议下一步技能

下一步开发建议使用这些技能：
- `diagnose` — 调试集体谈话同步和数据权限
- `tdd` — 为导入逻辑编写测试
- `to-issues` — 将剩余任务拆分为 GitHub Issues

---

## 待用户提供的资源

- `F:\QQ\QQwenjian\学生谈心谈话记录表.docx` — 需从 .doc 转换后放入 `resources/templates/`
