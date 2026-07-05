-- V2__init_data.sql
-- 第 2 个数据库版本：初始化 MVP 测试数据。
-- 这些数据用于验证用户权限、商品、订单和秒杀链路是否能够串联。

-- 1. 初始化角色
INSERT INTO sys_role (id, role_code, role_name, remark)
VALUES
    (1, 'ADMIN', '系统管理员', '拥有后台管理权限的默认管理员角色'),
    (2, 'CUSTOMER', '普通买家', '面向前台下单和客服查询的普通用户角色');

-- 2. 初始化用户
-- password_hash 使用常见 BCrypt 示例值，对应明文 password，仅用于本地开发测试。
-- 后续登录模块确定加密策略后，可以替换为项目实际生成的密码哈希。
INSERT INTO sys_user (id, username, password_hash, nickname, phone, email, user_type, status)
VALUES
    (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '后台管理员', '18800000001', 'admin@example.com', 1, 1),
    (2, 'alice', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '测试买家 Alice', '18800000002', 'alice@example.com', 2, 1),
    (3, 'bob', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '测试买家 Bob', '18800000003', 'bob@example.com', 2, 1);

-- 3. 绑定用户角色
INSERT INTO sys_user_role (id, user_id, role_id)
VALUES
    (1, 1, 1),
    (2, 2, 2),
    (3, 3, 2);

-- 4. 初始化商品分类
INSERT INTO product_category (id, parent_id, category_name, sort, status)
VALUES
    (1, 0, '手机数码', 1, 1),
    (2, 0, '家用电器', 2, 1),
    (3, 0, '食品生鲜', 3, 1);

-- 5. 初始化商品
INSERT INTO product (id, category_id, product_code, product_name, main_image, description, price, stock, sales, status)
VALUES
    (1, 1, 'P202607050001', 'iPhone 15 测试机', 'https://example.com/images/iphone15.jpg', '用于验证普通商品、订单和秒杀链路的手机商品。', 5999.00, 100, 10, 1),
    (2, 1, 'P202607050002', '无线降噪耳机', 'https://example.com/images/headphone.jpg', '用于验证普通订单多商品明细的数码配件。', 899.00, 200, 35, 1),
    (3, 3, 'P202607050003', '精品咖啡豆', 'https://example.com/images/coffee.jpg', '用于验证非数码分类下的普通商品。', 99.00, 500, 80, 1);

-- 6. 初始化普通订单
INSERT INTO order_info (
    id, order_no, user_id, total_amount, pay_amount,
    order_status, pay_status, source_type,
    receiver_name, receiver_phone, receiver_address, pay_time
)
VALUES
    (1, 'OD202607050001', 2, 6898.00, 6898.00, 1, 1, 1, 'Alice', '18800000002', '上海市浦东新区测试路 100 号', '2026-07-05 10:00:00');

-- 7. 初始化普通订单明细
INSERT INTO order_item (
    id, order_id, product_id, product_name, product_image,
    unit_price, quantity, total_price
)
VALUES
    (1, 1, 1, 'iPhone 15 测试机', 'https://example.com/images/iphone15.jpg', 5999.00, 1, 5999.00),
    (2, 1, 2, '无线降噪耳机', 'https://example.com/images/headphone.jpg', 899.00, 1, 899.00);

-- 8. 初始化秒杀活动
INSERT INTO seckill_activity (
    id, product_id, activity_name, seckill_price,
    activity_stock, start_time, end_time, status
)
VALUES
    (1, 1, 'iPhone 15 限时秒杀', 4999.00, 20, '2026-07-01 00:00:00', '2026-12-31 23:59:59', 1);

-- 9. 初始化秒杀订单及其关联的普通订单
INSERT INTO order_info (
    id, order_no, user_id, total_amount, pay_amount,
    order_status, pay_status, source_type,
    receiver_name, receiver_phone, receiver_address, pay_time
)
VALUES
    (2, 'OD202607050002', 3, 4999.00, 4999.00, 1, 1, 2, 'Bob', '18800000003', '杭州市西湖区测试街 200 号', '2026-07-05 11:00:00');

INSERT INTO order_item (
    id, order_id, product_id, product_name, product_image,
    unit_price, quantity, total_price
)
VALUES
    (3, 2, 1, 'iPhone 15 测试机', 'https://example.com/images/iphone15.jpg', 4999.00, 1, 4999.00);

INSERT INTO seckill_order (
    id, activity_id, user_id, order_id, seckill_price, status, fail_reason
)
VALUES
    (1, 1, 3, 2, 4999.00, 1, NULL);
