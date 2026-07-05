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

阶段 1：数据库设计已完成，准备进入阶段 2 Java 后端基础功能。

## 已完成

- 创建项目目录。
- 创建主线文档与各分支文档。
- 确定前期采用单体 Java 后端 + 独立 Python AI 服务。
- 完成数据库 MVP 设计。
- 完成 `V1__init_schema.sql`，包含用户、角色、商品分类、商品、订单、订单明细、秒杀活动、秒杀订单 9 张表。
- 完成 `V2__init_data.sql`，包含用于验证用户权限、商品、订单、秒杀链路的初始化测试数据。

## 正在进行

- 准备进入 Java 后端基础项目搭建分支。

## 待完成

- Java 后端基础项目搭建。
- 用户登录与权限模块。
- 商品、分类、订单模块。
- Redis 缓存与秒杀模块。
- RabbitMQ 异步下单。
- FastAPI RAG 知识库客服。
- LangGraph Agent 工具调用。
- Docker Compose 部署。
- README、截图、压测记录、简历描述。

## 使用方式

每次继续项目时，先确认当前讨论分支。如果讨论内容会影响其他分支，再引用相关分支文档。

