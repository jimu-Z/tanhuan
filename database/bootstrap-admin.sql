-- Run only against a newly initialized empty database.
-- In your local protected MySQL session, first set @bootstrap_admin_hash to a freshly generated BCrypt hash.
-- This file contains no reusable password/hash. It does nothing when the variable is missing or malformed.
START TRANSACTION;
INSERT IGNORE INTO sys_user (user_id,dept_id,user_name,nick_name,password,status,del_flag,pwd_update_date,create_by)
SELECT 1,100,'admin','内置管理员',@bootstrap_admin_hash,'0','0',NOW(),'system'
WHERE CHAR_LENGTH(@bootstrap_admin_hash)=60
  AND LEFT(@bootstrap_admin_hash,4) IN ('$2a$','$2b$','$2y$')
  AND NOT EXISTS (SELECT 1 FROM sys_user);
SET @bootstrap_created_admin = ROW_COUNT();
INSERT IGNORE INTO sys_user_role (user_id,role_id)
SELECT 1,1 FROM sys_user WHERE user_id=1 AND user_name='admin' AND @bootstrap_created_admin=1;
COMMIT;
-- Verify that user_id=1 exists after execution; otherwise initialization was not completed.
