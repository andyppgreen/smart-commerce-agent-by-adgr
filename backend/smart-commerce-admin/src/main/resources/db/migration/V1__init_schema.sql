-- V1__init_schema.sql
-- 第 1 个数据库版本：初始化智能电商运营平台的核心表结构。
-- 当前包含用户权限、商品分类、商品、订单、订单明细、秒杀活动和秒杀订单表。

CREATE TABLE sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID，系统内部主键',
    username VARCHAR(50) NOT NULL COMMENT '登录账号，后台管理员和普通买家都可使用',
    password_hash VARCHAR(100) NOT NULL COMMENT '加密后的密码，禁止保存明文密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '用户昵称，用于页面展示和客服识别',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号，后续可扩展手机号登录',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱，预留账号通知或找回密码能力',
    user_type TINYINT NOT NULL DEFAULT 2 COMMENT '用户类型：1后台管理员，2普通买家',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用，1启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间，用于审计和运营分析',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_phone (phone),
    KEY idx_sys_user_type_status (user_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE sys_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID，系统内部主键',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码，例如 ADMIN、CUSTOMER，程序判断权限时优先使用',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称，例如 系统管理员、普通用户，用于页面展示',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '角色状态：0禁用，1启用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '角色备注，记录角色用途或说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE sys_user_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID，关联 sys_user.id',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID，关联 sys_role.id',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role_user_role (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE product_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID，系统内部主键',
    parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称，例如 手机数码、家用电器',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '分类状态：0禁用，1启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    KEY idx_product_category_parent_id (parent_id),
    KEY idx_product_category_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE product (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID，系统内部主键',
    category_id BIGINT UNSIGNED NOT NULL COMMENT '分类ID，关联 product_category.id',
    product_code VARCHAR(64) NOT NULL COMMENT '商品编码，方便后台检索和外部系统对接',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    main_image VARCHAR(500) DEFAULT NULL COMMENT '商品主图地址',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '普通销售价格，金额字段使用 DECIMAL 避免精度问题',
    stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '普通库存，MVP 阶段暂不单独拆库存表',
    sales INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量，用于后台统计或商品排序',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '商品状态：0下架，1上架',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，后续扣减库存时可用于并发控制',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (product_code),
    KEY idx_product_category_id (category_id),
    KEY idx_product_status (status),
    KEY idx_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE order_info (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID，系统内部主键',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号，面向用户、客服和后台展示',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '下单用户ID，关联 sys_user.id',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实际支付金额，后续优惠、折扣会影响该字段',
    order_status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0待付款，1待发货，2待收货，3已完成，4已取消',
    pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0未支付，1已支付，2已退款',
    source_type TINYINT NOT NULL DEFAULT 1 COMMENT '订单来源：1普通订单，2秒杀订单',
    receiver_name VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) DEFAULT NULL COMMENT '收货人手机号',
    receiver_address VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_info_order_no (order_no),
    KEY idx_order_info_user_id (user_id),
    KEY idx_order_info_order_status (order_status),
    KEY idx_order_info_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE order_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单明细ID，系统内部主键',
    order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID，关联 order_info.id',
    product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID，关联 product.id',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称快照，记录下单时的商品名称',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '商品图片快照，记录下单时的商品主图',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '下单单价，记录下单时的成交价格',
    quantity INT UNSIGNED NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '明细总价，通常等于 unit_price * quantity',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_item_order_id (order_id),
    KEY idx_order_item_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

CREATE TABLE seckill_activity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '秒杀活动ID，系统内部主键',
    product_id BIGINT UNSIGNED NOT NULL COMMENT '参与秒杀的商品ID，关联 product.id',
    activity_name VARCHAR(100) NOT NULL COMMENT '活动名称，例如 iPhone 限时秒杀',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价格，金额字段使用 DECIMAL 避免精度问题',
    activity_stock INT UNSIGNED NOT NULL COMMENT '活动库存，后续会预热到 Redis 中做原子扣减',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '活动状态：0未启用，1启用，2已结束',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    KEY idx_seckill_activity_product_id (product_id),
    KEY idx_seckill_activity_time_status (start_time, end_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

CREATE TABLE seckill_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '秒杀订单ID，系统内部主键',
    activity_id BIGINT UNSIGNED NOT NULL COMMENT '秒杀活动ID，关联 seckill_activity.id',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '参与秒杀的用户ID，关联 sys_user.id',
    order_id BIGINT UNSIGNED DEFAULT NULL COMMENT '最终生成的普通订单ID，关联 order_info.id',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀成交价，记录用户抢购时的活动价格',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0排队中，1下单成功，2下单失败，3已取消',
    fail_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因，例如库存不足、异步下单失败',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_seckill_order_activity_user (activity_id, user_id),
    UNIQUE KEY uk_seckill_order_order_id (order_id),
    KEY idx_seckill_order_user_id (user_id),
    KEY idx_seckill_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';
