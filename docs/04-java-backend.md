# Java 后端分支

## 分支目标

使用 Spring Boot 完成电商运营平台核心业务后端。

## 当前状态

基础骨架、登录认证、JWT 鉴权、角色权限底座和商品分类管理 MVP 已完成。下一步进入商品管理。

## 已确定方案

- 前期使用 Spring Boot 单体架构。
- 使用 Controller / Service / Mapper 分层。
- 使用 MyBatis-Plus 操作 MySQL。
- 使用 JWT 做登录认证。
- 使用统一响应结构和全局异常处理。
- 暂不完整接入 Spring Security，当前使用自定义 JWT 拦截器和角色权限拦截器。
- 使用 Maven Wrapper 统一构建入口。

## 计划模块

- 项目基础配置。已完成
- 用户登录与认证。已完成
- 角色权限。已完成底座
- 商品分类。已完成 MVP
- 商品管理。
- 订单管理。
- Redis 缓存接入。
- 秒杀活动接入。
- RabbitMQ 异步下单。

## 待解决问题

1. 商品管理接口是否先做后台管理端，再补前台查询端。
2. 商品新增/修改时如何校验商品分类和价格库存字段。
3. 订单创建是否在商品模块后立即接入库存扣减。
4. Spring Security 是否在权限复杂化后再接入。

## 当前任务

实现商品管理模块。

## 已完成内容

- 创建 `backend/smart-commerce-admin` Spring Boot 后端模块。
- 接入 Maven Wrapper。
- 接入 Spring Web、Validation、Actuator、MyBatis-Plus、MySQL Driver、Flyway、Lombok、JUnit 5、Spring Boot Test、JWT。
- 复用数据库迁移脚本 `V1__init_schema.sql` 和 `V2__init_data.sql`。
- 新增 `V3__update_seed_user_passwords.sql`，将本地种子用户密码对齐为 `Admin@123`。
- 完成统一响应结构 `ApiResponse` 和错误码 `ResultCode`。
- 完成全局异常处理 `GlobalExceptionHandler`。
- 完成用户、角色、用户角色的 Entity / Mapper / Service / Impl 基础层。
- 完成登录接口 `POST /api/auth/login`。
- 完成当前用户接口 `GET /api/auth/me`。
- 完成 JWT 生成、解析和请求拦截。
- 完成登录用户上下文 `LoginUserContext`。
- 完成角色权限注解 `@RequireRoles` 和角色权限拦截器。
- 完成管理员权限验证接口 `GET /api/admin/me`。
- 完成基础测试、登录测试、当前用户集成测试、管理员权限集成测试。
- 完成商品分类 Entity / Mapper / Service / Controller。
- 完成商品分类管理接口 `GET/POST /api/admin/categories`、`PUT/DELETE /api/admin/categories/{id}`。
- 商品分类删除使用逻辑删除；存在未删除子分类时拒绝删除。
- 完成商品分类真实数据库集成测试，覆盖管理员访问、普通用户拒绝访问和 CRUD 主流程。

## 当前分支

- 本地分支：`feature/java-backend`
- 远程分支：`origin/feature/java-backend`
- 当前代码以本地和远程 `feature/java-backend` 分支最新提交为准。

## 本地开发约定

- 本地 MySQL：`127.0.0.1:3307`
- 数据库：`smart_commerce`
- 应用用户：`smart_commerce`
- 本地种子账号：`admin / Admin@123`

## 下一步建议

1. 阅读并讲解当前后端骨架。
2. 补充商品管理接口设计。
3. 实现商品 Entity / Mapper / Service / Controller。
4. 增加商品查询、创建和修改接口测试。

## 与其他分支的关联

- 数据库设计分支提供表结构。
- Redis 秒杀分支会扩展本后端。
- API 设计分支提供接口规范。
- LangGraph Agent 分支会调用本后端接口。

## 本分支交接摘要

截至 2026-07-12，Java 后端已完成基础骨架、登录认证、JWT 鉴权、角色权限底座、真实数据库集成验证和商品分类管理 MVP。下一步从商品管理开始继续开发。
