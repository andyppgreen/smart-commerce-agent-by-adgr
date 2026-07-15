# API 设计分支

## 分支目标

设计 Java 后端和 Python AI 服务的接口边界。

## 当前状态

阶段 2 API 已完成 MVP，认证、商品分类、商品目录、商品管理和订单主链路均已落地。

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

1. AI 服务调用 Java 后端时如何鉴权。
2. 支付、订单取消和库存归还接口如何设计。
3. 秒杀异步下单的受理响应和结果查询结构如何设计。
4. 对外联调前是否引入 OpenAPI，以及是否增加 `/api/v1` 版本前缀。
5. 随业务增长继续细化错误码，避免不同业务共用模糊错误。

## 已形成的接口约定

- 管理端业务统一放在 `/api/admin/**`，并要求 `ADMIN` 角色。
- 普通买家订单放在 `/api/orders/**`，并要求 `CUSTOMER` 角色。
- 商品目录放在 `/api/products/**`，允许匿名访问，但只暴露已上架、未删除商品。
- 登录成功后使用 `Authorization: Bearer <JWT>` 访问受保护接口。
- 所有接口统一返回 `ApiResponse`，分页列表的数据部分使用 `PageResponse`。
- 当前接口未增加版本前缀；进入外部联调或公开部署前再统一决定，避免过早修改全部路径。

## 当前任务

阶段 2 接口已完成 MVP 并进入交接。下一步保持现有商品查询接口不变，在 Service 层接入 Redis 缓存；后续订单迭代再补充支付、取消和库存归还。

## 与其他分支的关联

- Java 后端分支实现大部分业务接口。
- FastAPI AI 服务分支实现 AI 接口。
- LangGraph Agent 分支依赖 Java 业务查询接口。

## 本分支交接摘要

截至 2026-07-15，认证权限、商品分类、商品管理和订单管理接口已落地，Java 后端基础功能阶段完成。管理端、买家端和匿名接口边界已经固定，可作为 Redis 阶段的接口基线。
