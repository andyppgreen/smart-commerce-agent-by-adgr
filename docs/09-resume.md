# 简历包装分支

## 分支目标

把项目开发过程沉淀成可投递实习的项目经历。

## 当前状态

初稿待完善。

## 项目名称

智能电商运营平台 + AI 客服 Agent

## 项目描述草稿

基于 Spring Boot、MySQL、Redis、RabbitMQ 和 FastAPI 构建智能电商运营平台，实现商品、订单、库存、秒杀活动管理，并集成基于 RAG 和工具调用的 AI 客服 Agent，支持知识库问答、订单查询和售后咨询等场景。

## 技术栈草稿

Java、Spring Boot、MyBatis-Plus、MySQL、Redis、RabbitMQ、Python、FastAPI、LangChain/LangGraph、Docker Compose。

## 简历亮点候选

- 使用 Redis 缓存商品详情与活动库存，降低数据库访问压力。
- 使用 Redis 预扣库存和购买标记，解决秒杀场景下的超卖与重复下单问题。
- 使用 RabbitMQ 异步创建订单，实现高并发请求削峰。
- 使用 FastAPI 构建独立 AI 服务，通过 RAG 实现知识库客服问答。
- 使用 LangGraph 编排 Agent，使 AI 能调用 Java 后端订单查询接口。
- 使用 Docker Compose 编排 MySQL、Redis、RabbitMQ、Java 后端和 Python AI 服务。

## 待补充

- 实际完成的功能。
- 压测数据。
- 项目截图。
- GitHub/Gitee 地址。
- 简历最终表述。

## 本分支交接摘要

暂无。

