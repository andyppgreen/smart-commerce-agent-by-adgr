# FastAPI AI 服务分支

## 分支目标

使用 Python FastAPI 构建独立 AI 客服服务，先实现 RAG 知识库问答。

## 当前状态

未开始。

## 已确定方案

- AI 服务独立于 Java 后端。
- 使用 FastAPI 提供 HTTP 接口。
- 使用 LangChain 或 LangGraph 组织 AI 流程。
- 第一版先实现知识库问答，不直接追求复杂 Agent。

## 计划模块

- FastAPI 项目骨架。
- 文档上传接口。
- 文档切分。
- Embedding 向量化。
- 向量数据库存储。
- 检索问答接口。
- 基础日志与异常处理。

## 待解决问题

1. 向量数据库选择 Chroma 还是 Qdrant。
2. Embedding 模型如何选择。
3. 文档数据如何组织。
4. 是否需要用户会话上下文。

## 当前任务

等待 Java 后端 MVP 后再进入，避免同时开太多战线。

## 与其他分支的关联

- LangGraph Agent 分支会在本服务基础上增强。
- 部署分支需要部署本服务。
- API 设计分支需要定义 AI 服务接口。

## 本分支交接摘要

暂无。

