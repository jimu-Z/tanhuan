# 交接文档 — 学生谈心谈话管理系统

> 更新时间：2026-06-16

---

## 一、第八轮修复总览（2026-06-16）

### 根因分析

生产部署到 211.64.39.248 后发现多个严重 BUG：

1. **SysDeptMapper.xml insertDept 缺少 useGeneratedKeys**：新建部门后 deptId 返回 null，导致教师/学生导入全部失败
2. **教师导入模糊匹配**：getOrCreateCollegeDept 用 like 模糊匹配学院名，导致误匹配
3. **学生导入级联失败**：findOrCreateDept 返回 null → 创建 parent_id=0 的脏部门数据
4. **教师管理数据权限**：自定义 applyDataScopeFilter 只判断 isAdmin()，系统管理员角色看不到数据
5. **统一查询 403**：talk:record:list 权限不存在，只有 talk:record:query

### 修复内容

| # | 修复 | 文件 |
|---|------|------|
| 1 | SysDeptMapper.xml insertDept 添加 useGeneratedKeys="true" keyProperty="deptId" | SysDeptMapper.xml |
| 2 | getOrCreateCollegeDept 改用精确匹配(deptName+parentId=100) + 二次确认 | TalkTeacherServiceImpl.java |
| 3 | findOrCreateDept 增加主键回填安全检查 + 参数校验 + null结果跳过 | TalkStudentServiceImpl.java |
| 4 | TalkTeacherServiceImpl 删除自定义 applyDataScopeFilter，改用 @DataScope 注解 | TalkTeacherServiceImpl.java, TalkTeacherMapper.xml |
| 5 | 新增 talk:record:list 权限(菜单2026)并分配给角色3/4/5 | 数据库SQL |
| 6 | 仪表盘去竞争化：删除学院排名，改为工作提醒+预警概览+最近动态 | TalkStatisticsController, dashboardV2/index.vue |
| 7 | 前端标签显示兼容 JSON 数组字符串 | talkManagement, talkSession, unifiedQuery |
| 8 | 我的谈话记录删除跟进状态+教师已读列 | myRecords/index.vue |
| 9 | 统一查询删除跟进状态列和搜索，添加谈话人列 | unifiedQuery/index.vue |
| 10 | 标签管理标签标识列显示中文 | talkTag/index.vue |
| 11 | Student-UI 端口改6090，代理改211.64.39.248:6060 | Student-UI/vue.config.js, .env.production |

---

## 二、当前部署状态

| 服务 | 地址 | 启动方式 |
|---|---|---|
| 后端 | `http://211.64.39.248:6060` | 宝塔 Java 项目管理器，JDK 21 |
| 管理端前端 | `http://211.64.39.248:6080` | Nginx 静态文件 + 反代 `/prod-api/` → 6060 |
| 学生端前端 | `http://211.64.39.248:6090` | 待部署（Nginx 配置已准备好） |
| MySQL | `211.64.39.248:3306` | 宝塔管理 |
| Redis | `211.64.39.248:6379` | bind 127.0.0.1 211.64.39.248，protected-mode no |

### 部署路径

| 资源 | 服务器路径 |
|---|---|
| 后端 JAR | `/www/wwwroot/talk/deploy/ruoyi-admin.jar` |
| 管理端前端 | `/www/wwwroot/talk/deploy/admin-ui/` |
| Nginx 管理端配置 | 宝塔 HTML 项目站点设置 → 配置文件 |
| Nginx 学生端配置 | `nginx-student-6090.conf`（待部署） |

### 部署流程

1. 本地构建：`mvn package -DskipTests` + `npm run build:prod`
2. 上传 JAR 和 dist 到服务器 `/www/wwwroot/talk/deploy/`
3. 宝塔 Java 项目管理器重启 tanhua 项目
4. 清除浏览器缓存后验证

---

## 三、数据库配置

- Host: `211.64.39.248:3306`
- Database: `xuexiaotanhua`
- Username: `root`
- Password: 见服务器配置

---

## 四、验证结果（2026-06-16）

| 验证项 | 结果 |
|--------|------|
| 后端启动 | PASS（JDK 21 + JAR 运行正常） |
| 管理端前端访问 | PASS（6080 端口正常） |
| 教师导入 | PASS（精确匹配学院+主键回填） |
| 学生导入 | PASS（级联创建部门+主键回填） |
| 系统管理员看教师数据 | PASS（@DataScope 注解） |
| 统一查询权限 | PASS（talk:record:list 菜单2026） |
| 仪表盘新卡片 | PASS（工作提醒+预警+动态） |
| 标签中文显示 | PASS（JSON数组兼容） |

---

## 五、关键代码位置速查

| 功能 | 文件 |
|---|---|
| 统计 Controller | `ruoyi-system/.../talk/controller/TalkStatisticsController.java` |
| 预警 Controller | `ruoyi-system/.../talk/controller/TalkAlertController.java` |
| 会话 Controller | `ruoyi-system/.../talk/controller/TalkSessionController.java` |
| 学生 Service | `ruoyi-system/.../talk/service/impl/TalkStudentServiceImpl.java` |
| 教师 Service | `ruoyi-system/.../talk/service/impl/TalkTeacherServiceImpl.java` |
| 部门 Mapper | `ruoyi-system/.../mapper/system/SysDeptMapper.xml` |
| 学生 Mapper | `ruoyi-system/.../mapper/talk/TalkStudentMapper.xml` |
| 记录 Mapper | `ruoyi-system/.../mapper/talk/TalkStudentRecordMapper.xml` |
| 仪表盘前端 | `ruoyi-ui/src/views/talk/dashboardV2/index.vue` |
| 谈话管理前端 | `ruoyi-ui/src/views/talk/talkManagement/index.vue` |
| 统一查询前端 | `ruoyi-ui/src/views/talk/unifiedQuery/index.vue` |
| 前端路由 | `ruoyi-ui/src/router/index.js` |
| 菜单SQL | `sql/talk_menu_fix.sql` |
| Nginx 配置 | `nginx-admin-6080.conf`, `nginx-student-6090.conf` |

---

## 六、已知待办

1. 学生端前端(6090)尚未部署到服务器 — 需构建 Student-UI 并配置 Nginx 站点
2. 数据库中仍有历史脏数据（parent_id=0 的部门）— 需手动清理
3. 辅导员 dashboard 显示0学生 — 可能是 talk_teacher_class 与 sys_dept 的 class_name 不匹配
4. Token 密钥已更新为强密钥，所有用户需重新登录

---

## 七、建议技能

下一个 agent 接手时建议 invoke：

- `diagnose` — 调试运行时问题
- `grill-me` — 讨论方案
- `tdd` — 测试驱动开发
