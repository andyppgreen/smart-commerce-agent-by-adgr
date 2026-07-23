# 项目主线：智能电商运营平台 + AI 客服 Agent

## 项目定位

面向中小型电商业务的后台管理与智能客服系统，支持商品、订单、库存、用户、秒杀活动管理，并集成基于知识库和业务工具调用的 AI 客服 Agent。

## 核心目标

1. 训练 Java 后端工程能力。
2. 掌握 MySQL、Redis、RabbitMQ 的真实项目使用方式。
3. 掌握 FastAPI + LangChain/LangGraph 的 AI 应用开发。
4. 形成可以写进简历、可以展示、可以复盘的完整项目经历。

## 当前技术栈

- Java 后端：Java、Spring Boot、MyBatis-Plus
- 数据库：MySQL
- 缓存：Redis
- 消息队列：RabbitMQ
- AI 服务：Python、FastAPI、LangChain/LangGraph
- 部署：Docker Compose

## 当前阶段

阶段 3：Redis 缓存能力开发中。商品详情缓存、热门商品缓存和商品公共接口限流已经完成实现和本地联调。

## 已完成

- 创建项目目录。
- 创建主线文档与各分支文档。
- 确定前期采用单体 Java 后端 + 独立 Python AI 服务。
- 完成数据库 MVP 设计。
- 完成 `V1__init_schema.sql`，包含用户、角色、商品分类、商品、订单、订单明细、秒杀活动、秒杀订单 9 张表。
- 完成 `V2__init_data.sql`，包含用于验证用户权限、商品、订单、秒杀链路的初始化测试数据。
- 完成 Java 后端基础模块 `backend/smart-commerce-admin`。
- 接入 Maven Wrapper、Spring Boot、MyBatis-Plus、MySQL、Flyway、Validation、JWT 和测试基础能力。
- 完成统一响应结构、全局异常处理、用户登录、JWT 拦截、当前用户查询、角色权限注解与管理员权限拦截。
- 完成商品分类管理 MVP，包括查询、新增、修改和逻辑删除。
- 完成后台商品管理 MVP，包括分页筛选、详情、新增、修改和逻辑删除。
- 完成普通用户商品目录，包括匿名分页查询已上架商品和商品详情。
- 完成订单管理 MVP，包括多商品下单、库存原子扣减、用户订单查询和管理员订单查询。
- 完成 Java 后端阶段验收，Maven 完整测试 21 项全部通过。
- 增加 `backend/smart-commerce-admin/README.md`，记录启动、测试、权限边界、已知限制和下一阶段交接方式。
- 在 `feature/redis-seckill` 接入 Spring Data Redis，实现商品详情旁路缓存、短时空值缓存和 Redis 故障降级。
- 商品新增、修改、逻辑删除和库存扣减成功后清理对应详情缓存，保持现有对外接口不变。
- Redis 第一增量完成 Maven 完整测试 33 项，并通过本地 Redis 真实读写和缓存失效验证。
- 使用 Docker Desktop 建立本地 Redis 7.4.9 和 MySQL 8.0.46 容器，数据分别持久化到 D 盘。
- 完成商品公共接口固定窗口限流第一增量，使用 Redis Lua 保证计数与过期设置原子化。
- 完成热门商品缓存增量，新增按销量排序的 `GET /api/products/hot`，默认缓存前 10 个商品 300 秒。
- 商品库存扣减同时原子增加销量，并清理热门商品缓存，保证排序数据可更新。

## 正在进行

- 确认阶段 3 的下一个独立增量和验收范围。
- 保持 Redis 和 MySQL 容器配置稳定，准备后续秒杀前置设计。

## 待完成

- Token 辅助管理。
- 秒杀活动、Redis 库存和防重复下单。
- RabbitMQ 异步下单。
- FastAPI RAG 知识库客服。
- LangGraph Agent 工具调用。
- Docker Compose 部署。
- README、截图、压测记录、简历描述。

## 使用方式

每次继续项目时，先确认当前讨论分支。如果讨论内容会影响其他分支，再引用相关分支文档。

