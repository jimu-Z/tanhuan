# 交接文档 — 学生谈心谈话管理系统

> 生成时间：2026-06-10

---

## 一、本会话修改总览

### 1. 删除学生/教师 → 物理删除 sys_user

**文件变更：**
- `SysUserMapper.java` — 新增 `deleteUserPhysically(Long userId)`
- `SysUserMapper.xml` — 新增 `DELETE FROM sys_user WHERE user_id = #{userId}`
- `TalkTeacherServiceImpl.java` — `deleteTalkTeacherByIds()` 改用 `deleteUserPhysically`
- `TalkTeacherServiceImpl.java` — `createSysUserForTeacher()` 新增回填 `talk_teacher.user_id`
- `TalkTeacherMapper.java` — 新增 `updateTalkTeacherUserId(TalkTeacher)`
- `TalkTeacherMapper.xml` — 新增 `update talk_teacher set user_id = ? where teacher_id = ?`
- `TalkStudentServiceImpl.java` — `deleteTalkStudentByStudentIds/ByStudentId` 增加通过 `studentCode` 查 `sys_user` 并物理删除

### 2. 数据权限修复（辅导员/书记看不到学生）

**数据库变更：** `sys_role` 表
```sql
UPDATE sys_role SET data_scope = 4 WHERE role_key IN ('talk_counselor', 'talk_secretary');
```

**文件变更：**
- `TalkStudentServiceImpl.java` — 新增 `applyCounselorFilter()`，辅导员登录时按 `teacher_id` 过滤
- `TalkStudentMapper.xml` — `selectTalkStudentList` 和 `selectTalkStudentListWithLastTalk` 增加 `counselorTeacherId` 条件

**权限效果：**
| 角色 | 可见学生 |
|---|---|
| 管理员 | 全校 |
| 书记/副书记 | 全院（data_scope=4 + dept ancestors） |
| 辅导员/班主任 | 自己关联班级（data_scope=4 + teacher_id 过滤） |

### 3. 教师管理 → "学生"按钮

**文件变更：**
- `TalkStudentController.java` — `listByTeacher()` 增加 `position` 参数，书记/副书记走 `selectByCollegeDeptId`，辅导员走 `selectByTeacherId`
- `TalkStudentMapper.java/xml` — 新增 `selectByCollegeDeptId`（按学院查所有学生，含子部门）
- `ITalkStudentService.java` / `TalkStudentServiceImpl.java` — 新增 `selectByCollegeDeptId(Long)`
- `ruoyi-ui/src/api/talk/teacher.js` — `getTeacherStudents` 增加 `position` 参数
- `ruoyi-ui/src/views/talk/teacher/index.vue` — 调用时传 `row.position`

> **注意：** `position` 字段存的是中文（"书记"/"副书记"/"辅导员"/"班主任"），后端用中文匹配。

### 4. 前端显示修复

**文件变更：**
- `ruoyi-ui/src/views/talk/talkStudent/index.vue` — 列名 "部门ID(班级)" → "班级"，prop `deptId` → `deptName`
- 同上 — 搜索栏 "部门" → "班级"

### 5. 导入学生不再自动创建教师用户

**文件变更：**
- `TalkStudentServiceImpl.java` — 删除 `importExecute` 中为书记/辅导员/班主任自动创建 `sys_user` 的代码
- `TalkStudentServiceImpl.java` — `createUserIfNotExists` 方法签名改为 `(userName, nickName, deptId, roleKey, ...)`，nickName 与 userName 分离

### 6. 清理逻辑删除用户

执行过 `DELETE FROM sys_user WHERE del_flag = '2'`（清理了 47 条记录）。

---

## 二、当前部署状态

| 服务 | 地址 | 启动方式 |
|---|---|---|
| 后端 | `http://localhost:8080` | `mvn spring-boot:run -pl ruoyi-admin`（项目根目录） |
| 前端 | `http://localhost:80` | `npm run dev`（ruoyi-ui 目录） |

**注意：** 重新部署时必须先 `mvn install -pl ruoyi-common,ruoyi-system -DskipTests` 确保 ruoyi-system 模块安装到本地 Maven 仓库，然后 `mvn spring-boot:run -pl ruoyi-admin`。

---

## 三、数据库配置

- Host: `localhost:3306`
- Database: `xuexiaotanhua`
- MySQL 路径: `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`

---

## 四、关键代码位置速查

| 功能 | 文件 |
|---|---|
| 学生 Service | `ruoyi-system/.../talk/service/impl/TalkStudentServiceImpl.java` |
| 教师 Service | `ruoyi-system/.../talk/service/impl/TalkTeacherServiceImpl.java` |
| 学生 Controller | `ruoyi-system/.../talk/controller/TalkStudentController.java` |
| 学生 Mapper XML | `ruoyi-system/.../resources/mapper/talk/TalkStudentMapper.xml` |
| 教师 Mapper XML | `ruoyi-system/.../resources/mapper/talk/TalkTeacherMapper.xml` |
| 系统用户 Mapper XML | `ruoyi-system/.../resources/mapper/system/SysUserMapper.xml` |
| DataScope 切面 | `ruoyi-framework/.../aspectj/DataScopeAspect.java` |
| 前端学生管理 | `ruoyi-ui/src/views/talk/talkStudent/index.vue` |
| 前端教师管理 | `ruoyi-ui/src/views/talk/teacher/index.vue` |
| 前端发起谈话 | `ruoyi-ui/src/views/talk/talkInitiate/index.vue` |

---

## 五、建议技能

下一个 agent 接手时建议 invoke：

- `diagnose` — 调试运行时问题
- `grill-me` — 讨论方案
- `tdd` — 测试驱动开发

---

## 六、已知待验证

1. 教师管理页面"学生"按钮功能（前端刚重启，需要重新验证）
2. 删除教师物理删除 sys_user 功能
3. 前端需要从 `http://localhost:80` 访问（不是 8080）
