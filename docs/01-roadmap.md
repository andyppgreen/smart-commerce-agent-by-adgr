# 项目路线图

## 阶段 0：项目主线搭建

目标：建立长期文档结构，避免后续上下文断裂。

任务：

- 创建项目目录。
- 创建主线文档。
- 创建各分支文档。
- 记录第一条架构决策。

状态：已完成。

## 阶段 1：数据库设计

目标：确定用户、角色、商品、分类、订单、秒杀活动等基础表结构。

预期产出：

- ER 关系说明。
- MySQL 建表 SQL。
- 核心字段解释。
- 索引设计初稿。

实际产出：

- `docs\02-database-design.md`：数据库设计说明。
- `backend\smart-commerce-admin\src\main\resources\db\migration\V1__init_schema.sql`：MVP 建表 SQL。
- `backend\smart-commerce-admin\src\main\resources\db\migration\V2__init_data.sql`：MVP 初始化测试数据。

状态：已完成。

## 阶段 2：Java 后端基础功能

目标：完成电商后台基础接口。

状态：已验收完成。

模块：

- 用户登录。已完成
- JWT 认证。已完成
- 角色权限底座。已完成
- 商品分类管理。已完成 MVP
- 商品管理。已完成 MVP
- 订单创建与查询。已完成 MVP
- 全局异常处理。已完成
- 参数校验。已接入基础能力

当前产出：

- `backend\smart-commerce-admin`：Spring Boot 后端基础模块。
- Maven Wrapper：统一后端构建入口。
- Flyway：复用数据库迁移脚本，并新增 `V3__update_seed_user_passwords.sql` 对齐本地登录测试密码。
- 认证接口：`POST /api/auth/login`。
- 当前用户接口：`GET /api/auth/me`。
- 管理员测试接口：`GET /api/admin/me`。
- 商品分类管理接口：`GET/POST /api/admin/categories`、`PUT/DELETE /api/admin/categories/{id}`。
- 后台商品管理接口：`GET/POST /api/admin/products`、`GET/PUT/DELETE /api/admin/products/{id}`。
- 商品列表支持分页、关键词、分类和上下架状态筛选。
- 普通用户商品目录：`GET /api/products`、`GET /api/products/{id}`，只返回已上架、未删除商品。
- 普通用户订单接口：`POST /api/orders`、`GET /api/orders`、`GET /api/orders/{id}`。
- 后台订单接口：`GET /api/admin/orders`、`GET /api/admin/orders/{id}`。
- 下单支持多商品明细、商品信息快照、后端金额计算和数据库事务内原子扣减库存。
- 角色权限注解：`@RequireRoles`。
- 测试：后端基础、认证权限、商品分类、商品管理和订单管理集成测试已通过。

阶段验收：

- Maven 完整测试共 21 项，0 失败、0 错误、0 跳过。
- 已验证登录鉴权、角色隔离、分类 CRUD、商品 CRUD、匿名商品目录和订单主链路。
- 已验证多商品下单、后端金额计算、商品快照、库存原子扣减和库存不足事务回滚。
- 已记录运行方式、测试账号、模块结构、已知边界和下一阶段入口。

阶段边界：

- 支付、订单取消、超时关单和库存归还留到订单后续迭代。
- Token 刷新与退出、完整用户角色管理和 Spring Security 暂不纳入本阶段。
- Redis、秒杀和 RabbitMQ 从阶段 3、阶段 4 开始实现。

交接条件：

- 将 `feature/java-backend` 合并并推送到 `dev`。
- 从更新后的 `dev` 创建下一阶段分支，避免遗漏数据库和 Java 后端提交。

## 阶段 3：Redis 缓存能力

目标：把普通 CRUD 项目升级成更接近真实后端项目的系统。

状态：进行中，商品详情缓存、热门商品缓存和商品公共接口限流增量已完成。

模块：

- 商品详情缓存。已完成第一增量
- 登录状态或 Token 辅助管理。待确认范围
- 热门商品缓存。已完成第一版
- 缓存穿透处理。已完成短时空值缓存
- 简单接口限流。已完成商品公共接口第一增量

第一增量产出：

- Spring Data Redis 依赖和本地连接配置。
- 商品详情缓存 key：`product:catalog:detail:{id}`。
- 正常商品缓存 1800 秒，不存在或不可见商品缓存 `__NULL__` 60 秒。
- `GET /api/products/{id}` 使用旁路缓存，接口路径和响应结构保持不变。
- 商品新增、修改、逻辑删除和库存扣减成功后清理详情缓存。
- Redis 读写失败时记录告警并降级到 MySQL，不阻断商品业务。
- Maven 完整测试共 33 项，0 失败、0 错误、0 跳过。
- 已使用本地 Redis 验证缓存命中、空值缓存、修改失效和删除失效。
- 商品公共接口使用 Redis Lua 实现固定窗口限流，默认每个客户端 IP 10 秒 60 次。
- 超限返回 HTTP 429 和 `Retry-After`，Redis 异常时降级放行。
- 限流相关单元和 MockMvc 测试已通过，Maven 完整测试共 40 项。

热门商品缓存增量产出：

- 新增 `GET /api/products/hot`，匿名返回已上架、未删除商品。
- 热门商品按 `sales DESC, id DESC` 排序，默认缓存前 10 个商品。
- 热门商品 key 为 `product:catalog:hot`，默认 TTL 为 300 秒。
- 商品新增、修改、逻辑删除和库存扣减成功后清理热门商品缓存。
- 库存扣减同时原子增加销量，避免热门排序长期使用旧销量。
- 热门缓存相关测试已通过，当前 Maven 完整测试共 44 项。

阶段边界：

- 热点 key 重建、分布式锁、秒杀库存、Lua 和 RabbitMQ 不纳入第一增量。
- 热门商品缓存当前采用失效后回源重建，暂不引入热点 key 互斥重建。
- 本地使用 Docker Redis 7.4.9，容器端口映射为 `127.0.0.1:6379`，数据持久化到 D 盘。
- 本地使用 Docker MySQL 8.0.46，容器端口映射为 `127.0.0.1:3307`，供 Flyway 和集成测试使用。

## 阶段 4：秒杀与异步下单

目标：完成项目的 Java 后端核心亮点。

模块：

- 秒杀活动配置。
- Redis 库存预热。
- 防重复下单。
- Redis 原子扣减库存。
- RabbitMQ 异步创建订单。
- 秒杀结果查询。
- JMeter 压测记录。

## 阶段 5：FastAPI AI 客服

目标：实现基于知识库的客服问答能力。

模块：

- 文档上传。
- 文档切分。
- 向量化存储。
- 知识库检索。
- RAG 问答接口。

## 阶段 6：LangGraph Agent 工具调用

目标：让 AI 能调用真实业务系统，而不只是回答文档问题。

模块：

- 订单查询工具。
- 商品库存查询工具。
- 售后政策查询工具。
- Agent 流程编排。
- Java 后端接口调用。

## 阶段 7：部署与简历包装

目标：让项目具备展示和投递简历的能力。

模块：

- Docker Compose 一键启动。
- README 完善。
- 架构图。
- 接口文档。
- 项目截图。
- 压测数据。
- 简历项目描述。

