# 部署分支

## 分支目标

使用 Docker Compose 组织本地开发和演示环境。

## 当前状态

未开始。

## 已确定方案

- 先支持本地一键启动基础中间件。
- 后续再加入 Java 后端和 Python AI 服务镜像。

## 计划组件

- MySQL。
- Redis。
- RabbitMQ。
- Java Spring Boot 后端。
- Python FastAPI AI 服务。
- Nginx，可选。

## 待解决问题

1. 本地端口如何规划。
2. 数据卷如何保存。
3. 环境变量如何管理。
4. 是否需要区分开发环境和演示环境。

## 当前任务

等待具体服务生成后再补充 Docker Compose。

## 与其他分支的关联

- Java 后端分支需要数据库、Redis、RabbitMQ。
- FastAPI AI 服务分支需要向量数据库和模型配置。
- 简历分支需要记录部署方式。

## 本分支交接摘要

暂无。

