# API 设计分支

## 分支目标

设计 Java 后端和 Python AI 服务的接口边界。

## 当前状态

初步开始，已随 Java 后端分支落地认证与权限基础接口。

## 已确定方案

- Java 后端提供电商业务接口。
- Python FastAPI 提供 AI 客服和 Agent 接口。
- Python AI 服务通过 HTTP 调用 Java 后端业务接口。

## 初始接口模块

- 认证接口：登录、获取当前用户信息。已初步实现
- 管理员接口：管理员当前用户信息。已用于验证角色权限
- 商品接口：分类管理、后台商品管理和普通用户商品查询已实现 MVP。
- 订单接口：创建订单、用户订单列表与详情、后台订单列表与详情。已实现 MVP
- 秒杀接口：活动列表、发起秒杀、查询秒杀结果。
- AI 接口：文档上传、知识库问答、Agent 对话。

## 已实现接口

### 认证接口

- `POST /api/auth/login`：用户名密码登录，返回 JWT。
- `GET /api/auth/me`：通过 JWT 获取当前登录用户信息。

### 管理员权限验证接口

- `GET /api/admin/me`：需要 `ADMIN` 角色，用于验证角色权限拦截链路。

### 商品分类管理接口

- `GET /api/admin/categories`：查询未逻辑删除的商品分类。
- `POST /api/admin/categories`：新增商品分类。
- `PUT /api/admin/categories/{id}`：修改商品分类。
- `DELETE /api/admin/categories/{id}`：逻辑删除商品分类；存在子分类时拒绝删除。

### 后台商品管理接口

- `GET /api/admin/products`：分页查询商品，支持关键词、分类和状态筛选。
- `GET /api/admin/products/{id}`：查询商品详情。
- `POST /api/admin/products`：新增商品。
- `PUT /api/admin/products/{id}`：修改商品。
- `DELETE /api/admin/products/{id}`：逻辑删除商品。

### 普通用户商品目录接口

- `GET /api/products`：匿名分页查询已上架、未删除商品，支持关键词和分类筛选。
- `GET /api/products/{id}`：匿名查询已上架、未删除商品详情。
- 普通用户响应不暴露精确库存、乐观锁版本和后台商品编码，只返回是否有货。

### 普通用户订单接口

- `POST /api/orders`：普通买家创建多商品订单，后端计算金额并扣减库存。
- `GET /api/orders`：分页查询当前买家的订单，支持订单状态筛选。
- `GET /api/orders/{id}`：查询当前买家自己的订单详情和商品快照。

### 后台订单接口

- `GET /api/admin/orders`：管理员分页查询订单，支持订单号、用户和订单状态筛选。
- `GET /api/admin/orders/{id}`：管理员查询任意订单详情。

## 待解决问题

1. API 路径风格如何统一。
2. 管理端接口和用户端接口是否分开。
3. AI 服务调用 Java 后端时如何鉴权。
4. 接口错误码如何设计。

## 当前任务

阶段 2 接口已完成 MVP。下一步进入 Redis 缓存能力，并在后续订单迭代中补充支付、取消和库存归还。

## 与其他分支的关联

- Java 后端分支实现大部分业务接口。
- FastAPI AI 服务分支实现 AI 接口。
- LangGraph Agent 分支依赖 Java 业务查询接口。

## 本分支交接摘要

截至 2026-07-15，认证权限、商品分类、商品管理和订单管理接口已落地，Java 后端基础功能阶段完成。
