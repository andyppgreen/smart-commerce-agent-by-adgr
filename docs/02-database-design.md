# 数据库设计分支

## 分支目标

设计智能电商运营平台的 MySQL 数据模型，为 Java 后端开发提供稳定基础。

## 当前状态

第一版 MVP 表结构已完成。

## 设计原则

### 1. 先服务 MVP，不提前做复杂电商模型

本阶段重点是跑通后台管理、下单、秒杀、AI 客服查询这些主链路，所以暂时不引入完整的 SPU/SKU、多仓库存、优惠券、支付流水、物流、售后等复杂模型。

工程原因：

- 表越多，接口、实体类、Mapper、Service 的复杂度越高。
- MVP 阶段应该优先保证核心业务链路完整。
- 等业务真的需要多规格、多仓库、库存流水时，再拆表更自然。

### 2. 统一用户表，用字段区分用户类型

`sys_user` 同时保存后台管理员和普通买家，通过 `user_type` 区分。

工程原因：

- 登录、JWT、密码加密、账号状态这些能力可以复用。
- 后续 AI 客服查询订单时，普通买家也是系统用户。
- 后台权限可以通过角色表扩展，不需要单独建管理员表。

### 3. 订单必须拆主表和明细表

`order_info` 保存整单信息，`order_item` 保存订单里的商品明细。

工程原因：

- 一个订单可能包含多个商品。
- 订单主表只关心整单状态、金额、用户。
- 订单明细要保存商品快照，避免商品改名、改价后影响历史订单。

### 4. 秒杀订单单独建表

`seckill_order` 用来记录用户在某个秒杀活动中的抢购结果，并关联最终生成的普通订单。

工程原因：

- 秒杀需要快速判断用户是否重复下单。
- `(activity_id, user_id)` 唯一索引可以在数据库层兜底防重。
- 秒杀下单一般会经过 Redis 和 RabbitMQ，单独表方便查询异步处理结果。

## 表关系说明

```text
sys_user 1 -- n sys_user_role n -- 1 sys_role

product_category 1 -- n product

sys_user 1 -- n order_info
order_info 1 -- n order_item
product 1 -- n order_item

product 1 -- n seckill_activity
seckill_activity 1 -- n seckill_order
sys_user 1 -- n seckill_order
order_info 1 -- 0/1 seckill_order
```

## 第一版 MVP 表清单

### 1. `sys_user`：用户表

为什么需要：

- 保存登录账号、密码、昵称、手机号等用户基础信息。
- 同时支持后台管理员和普通买家。
- 为订单、秒杀订单、AI 客服查询用户订单提供用户主键。

关键字段：

- `username`：登录账号，唯一。
- `password_hash`：加密后的密码，不保存明文密码。
- `user_type`：用户类型，1 表示后台管理员，2 表示普通买家。
- `status`：账号状态，方便禁用异常账号。

### 2. `sys_role`：角色表

为什么需要：

- 保存系统角色，比如 `ADMIN`、`CUSTOMER`。
- 后续做接口权限控制时，可以根据角色判断是否允许访问后台接口。

关键字段：

- `role_code`：角色编码，程序里通常用它判断权限。
- `role_name`：角色名称，给后台页面展示。
- `status`：角色是否启用。

### 3. `sys_user_role`：用户角色关联表

为什么需要：

- 用户和角色是多对多关系。
- 一个用户可以有多个角色，一个角色也可以分配给多个用户。

工程原因：

- 不把 `role_id` 直接放在 `sys_user` 里，是为了保留扩展性。
- 例如以后一个运营人员既是商品管理员，又是订单管理员。

关键约束：

- `(user_id, role_id)` 唯一，防止重复分配同一个角色。

### 4. `product_category`：商品分类表

为什么需要：

- 商品需要按分类管理和筛选。
- 后台商品管理页面通常需要分类树。

关键字段：

- `parent_id`：父分类 ID，0 表示一级分类。
- `category_name`：分类名称。
- `sort`：排序值。
- `status`：是否启用。

### 5. `product`：商品表

为什么需要：

- 保存商品名称、分类、价格、库存、上下架状态等核心信息。
- 普通订单和秒杀活动都会依赖商品表。

关键字段：

- `category_id`：所属分类。
- `product_code`：商品编码，方便后台检索和对接外部系统。
- `price`：普通销售价格。
- `stock`：普通库存。
- `status`：上下架状态。
- `version`：乐观锁版本号，后续用 MyBatis-Plus 更新库存时可用。

关于库存是否拆表：

- 第一版不单独拆库存表，库存直接放在 `product.stock`。
- 原因是 MVP 只有单商品、单仓库、简单库存扣减。
- 后续如果加入库存流水、多仓库、库存冻结，再拆 `product_stock` 或 `stock_log`。

### 6. `order_info`：订单表

为什么需要：

- 保存一笔订单的整单信息。
- 订单查询、AI 客服查单、后台订单管理都会依赖它。

关键字段：

- `order_no`：订单号，给用户和客服查看，唯一。
- `user_id`：下单用户。
- `total_amount`：商品总金额。
- `pay_amount`：实际支付金额。
- `order_status`：订单状态。
- `pay_status`：支付状态。
- `source_type`：订单来源，1 普通订单，2 秒杀订单。

订单状态初稿：

| 状态值 | 含义 |
| --- | --- |
| 0 | 待付款 |
| 1 | 待发货 |
| 2 | 待收货 |
| 3 | 已完成 |
| 4 | 已取消 |

### 7. `order_item`：订单明细表

为什么需要：

- 保存订单中每个商品的购买数量和成交价。
- 支持一个订单包含多个商品。

关键字段：

- `order_id`：所属订单。
- `product_id`：商品 ID。
- `product_name`：下单时的商品名称快照。
- `product_image`：下单时的商品图片快照。
- `unit_price`：下单时单价。
- `quantity`：购买数量。
- `total_price`：该明细总价。

工程原因：

- 商品名称、图片、价格后续可能变化。
- 订单明细保存快照，可以保证历史订单展示稳定。

### 8. `seckill_activity`：秒杀活动表

为什么需要：

- 保存秒杀商品、秒杀价、活动库存、开始结束时间。
- Redis 秒杀分支会基于这张表预热活动库存。

关键字段：

- `product_id`：参与秒杀的商品。
- `seckill_price`：秒杀价。
- `activity_stock`：活动库存。
- `start_time` / `end_time`：活动时间范围。
- `status`：活动状态。

活动状态初稿：

| 状态值 | 含义 |
| --- | --- |
| 0 | 未启用 |
| 1 | 启用 |
| 2 | 已结束 |

### 9. `seckill_order`：秒杀订单表

为什么需要：

- 记录用户是否已经成功参与某个秒杀活动。
- 支持秒杀结果查询。
- 关联异步创建出来的普通订单。

关键字段：

- `activity_id`：秒杀活动 ID。
- `user_id`：参与用户。
- `order_id`：最终生成的普通订单 ID。
- `seckill_price`：秒杀成交价。
- `status`：秒杀订单处理状态。

状态初稿：

| 状态值 | 含义 |
| --- | --- |
| 0 | 排队中 |
| 1 | 下单成功 |
| 2 | 下单失败 |
| 3 | 已取消 |

关键约束：

- `(activity_id, user_id)` 唯一，保证同一用户在同一活动中只能成功占位一次。

## MySQL 建表 SQL

说明：

- 使用 `bigint unsigned` 作为主键，方便后续数据增长。
- 金额使用 `decimal(10,2)`，不要用 `float` 或 `double` 存钱。
- 时间字段统一使用 `datetime`。
- 基础数据表保留 `deleted` 字段，方便后续 MyBatis-Plus 逻辑删除。

```sql
CREATE TABLE sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password_hash VARCHAR(100) NOT NULL COMMENT '加密后的密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    user_type TINYINT NOT NULL DEFAULT 2 COMMENT '用户类型：1后台管理员，2普通买家',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_phone (phone),
    KEY idx_sys_user_type_status (user_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE sys_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE sys_user_role (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE product_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    KEY idx_category_parent_id (parent_id),
    KEY idx_category_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE product (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    category_id BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
    product_code VARCHAR(64) NOT NULL COMMENT '商品编码',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    main_image VARCHAR(500) DEFAULT NULL COMMENT '商品主图',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '普通销售价格',
    stock INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '普通库存',
    sales INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '销量',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0下架，1上架',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
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
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '下单用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
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
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_user_id (user_id),
    KEY idx_order_status (order_status),
    KEY idx_order_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE order_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
    order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称快照',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '商品图片快照',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '下单单价',
    quantity INT UNSIGNED NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '明细总价',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_item_order_id (order_id),
    KEY idx_order_item_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

CREATE TABLE seckill_activity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '秒杀活动ID',
    product_id BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
    activity_name VARCHAR(100) NOT NULL COMMENT '活动名称',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
    activity_stock INT UNSIGNED NOT NULL COMMENT '活动库存',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0未启用，1启用，2已结束',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    KEY idx_seckill_product_id (product_id),
    KEY idx_seckill_time_status (start_time, end_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

CREATE TABLE seckill_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '秒杀订单ID',
    activity_id BIGINT UNSIGNED NOT NULL COMMENT '秒杀活动ID',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    order_id BIGINT UNSIGNED DEFAULT NULL COMMENT '关联的普通订单ID',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀成交价',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0排队中，1下单成功，2下单失败，3已取消',
    fail_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_seckill_user_activity (activity_id, user_id),
    KEY idx_seckill_order_user_id (user_id),
    UNIQUE KEY uk_seckill_order_order_id (order_id),
    KEY idx_seckill_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';
```

## 索引设计初稿

### 用户与权限

- `sys_user.username` 唯一索引：登录时按用户名查询。
- `sys_user.phone` 唯一索引：后续支持手机号登录或绑定。
- `sys_role.role_code` 唯一索引：根据角色编码判断权限。
- `sys_user_role(user_id, role_id)` 唯一索引：防止重复分配角色。

### 商品

- `product.category_id`：按分类查询商品。
- `product.status`：查询上架商品。
- `product.product_code` 唯一索引：后台按商品编码检索。
- `product_category.parent_id`：查询分类树。

### 订单

- `order_info.order_no` 唯一索引：用户、客服、后台都常按订单号查询。
- `order_info.user_id`：查询某个用户的订单列表。
- `order_info.order_status`：后台按订单状态筛选。
- `order_item.order_id`：查询订单明细。

### 秒杀

- `seckill_activity(start_time, end_time, status)`：查询当前有效活动。
- `seckill_order(activity_id, user_id)` 唯一索引：防止同一用户重复抢购同一活动。
- `seckill_order.user_id`：用户查询自己的秒杀结果。
- `seckill_order.order_id` 唯一索引：从秒杀记录反查普通订单，并保证一条普通订单最多关联一条秒杀记录。

## 待解决问题

1. 是否在 Java 后端阶段引入数据库外键约束。
   - 第一版 SQL 暂不加外键，主要依赖业务代码维护关系。
   - 原因是后续做逻辑删除、异步下单、批量导入时，外键可能增加开发和排错成本。
2. 是否拆分库存表。
   - 当前不拆。
   - 等出现多仓库、库存流水、冻结库存需求时再拆。
3. 是否增加支付流水表。
   - 当前不加。
   - 等接入模拟支付或第三方支付时，再增加 `payment_record`。
4. 是否增加 AI 客服会话表。
   - 当前数据库分支先聚焦电商核心表。
   - FastAPI AI 客服阶段再设计知识库、会话、消息等表。

## 与其他分支的关联

- Java 后端分支需要基于本分支生成实体类、Mapper、Service。
- Redis 秒杀分支需要引用 `product`、`seckill_activity`、`seckill_order`。
- RabbitMQ 异步下单需要基于 `seckill_order.status` 更新秒杀处理结果。
- LangGraph Agent 分支可以读取 `order_info`、`order_item`、`product` 来回答查单和商品问题。

## 本分支交接摘要

- 第一版 MVP 共 9 张表：
  - `sys_user`
  - `sys_role`
  - `sys_user_role`
  - `product_category`
  - `product`
  - `order_info`
  - `order_item`
  - `seckill_activity`
  - `seckill_order`
- 用户表统一支持后台管理员和普通买家。
- 商品库存第一版放在 `product.stock`，暂不拆库存表。
- 普通订单使用 `order_info` + `order_item`。
- 秒杀订单使用 `seckill_order` 记录抢购结果，并通过 `order_id` 关联普通订单。
- 下一步可以进入 Java 后端基础项目搭建，或先补充初始化数据 SQL。
