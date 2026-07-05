-- V3__update_seed_user_passwords.sql
-- 第 3 个数据库版本：把本地开发种子用户密码统一为 Admin@123。
-- 这样登录链路和集成测试可以直接对齐当前项目约定。

UPDATE sys_user
SET password_hash = '$2a$10$yz22ZonL7zfGfx/tkWqUbuf.Ei/InMUy.lGPW0i/T7b12SRpop8aa'
WHERE username IN ('admin', 'alice', 'bob');
