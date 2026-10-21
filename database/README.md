# 可公开的数据库初始化文件

本目录对应当前v10结构，用于新建空数据库。包含45张表的完整结构，以及角色、菜单、权限关联、字典、岗位、通用学校根节点和安全配置默认值。

不包含学生/教师资料、真实学院班级、谈话/心理预警、附件元数据、业务模板、操作/登录日志、账号、密码哈希或生产配置。现行角色为admin、school_admin、college_admin、talk_teacher；保留停用旧角色以兼容历史，学生端不启用。

## 新环境初始化

1. 自行创建一个空的MySQL 8数据库，字符集utf8mb4。确认选中的是新空库。
2. 依次导入schema.sql和seed.sql。schema.sql不含DROP TABLE或USE，遇到同名表会报错，不能作为线上覆盖脚本。
3. 初次管理员需在本地安全终端生成独立BCrypt密码哈希，并在MySQL当前会话设置变量@bootstrap_admin_hash，再导入bootstrap-admin.sql。脚本仅在用户表为空、变量形似有效BCrypt哈希时写入内置admin；不会更新任何已有账号。不得将真实密码/哈希提交到Git。
4. 确认sys_user存在user_id=1、user_name=admin，以及sys_user_role的1/1关联，再配置后端所需环境变量。首次登录后由管理员建立学院、年级、班级与教师。

已有环境不要执行整套初始化，先备份并按实际版本选择RuoYi-Vue-master/sql中的增量脚本。此结构已包含student_import_managers_v10.sql新增三表，不需重复执行历史v9初始化。

本目录是公开初始化资料，不是线上数据库备份；本地原始完整SQL和候选部署包仍单独保留。
