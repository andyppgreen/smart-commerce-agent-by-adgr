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

状态：进行中。

模块：

- 用户登录。已完成
- JWT 认证。已完成
- 角色权限底座。已完成
- 商品分类管理。
- 商品管理。
- 订单创建与查询。
- 全局异常处理。已完成
- 参数校验。已接入基础能力

当前产出：

- `backend\smart-commerce-admin`：Spring Boot 后端基础模块。
- Maven Wrapper：统一后端构建入口。
- Flyway：复用数据库迁移脚本，并新增 `V3__update_seed_user_passwords.sql` 对齐本地登录测试密码。
- 认证接口：`POST /api/auth/login`。
- 当前用户接口：`GET /api/auth/me`。
- 管理员测试接口：`GET /api/admin/me`。
- 角色权限注解：`@RequireRoles`。
- 测试：后端基础测试与认证权限集成测试已通过。

## 阶段 3：Redis 缓存能力

目标：把普通 CRUD 项目升级成更接近真实后端项目的系统。

模块：

- 商品详情缓存。
- 登录状态或 Token 辅助管理。
- 热门商品缓存。
- 缓存穿透处理。
- 简单接口限流。

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

