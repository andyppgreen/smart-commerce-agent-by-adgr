# Java 后端分支

## 分支目标

使用 Spring Boot 完成电商运营平台核心业务后端。

## 当前状态

基础骨架、认证权限、商品分类和商品管理 MVP 已完成。下一步进入订单管理。

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
- 商品管理。已完成 MVP
- 订单管理。
- Redis 缓存接入。
- 秒杀活动接入。
- RabbitMQ 异步下单。

## 待解决问题

1. 订单创建时采用直接扣减库存，还是先做库存校验再扣减。
2. 普通用户订单列表和后台订单列表如何区分权限。
3. 第一版是否只实现单商品下单，还是直接支持多商品明细。
4. Spring Security 是否在权限复杂化后再接入。

## 当前任务

设计并实现订单创建、订单列表和订单详情接口。

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
- 接入 MyBatis-Plus 分页 SQL 解析组件，并启用分页和乐观锁拦截器。
- 完成通用分页响应 `PageResponse`。
- 完成商品 Entity / Mapper / Service / Controller。
- 完成后台商品分页筛选、详情、新增、修改和逻辑删除接口。
- 商品新增/修改会校验分类存在性、商品编码唯一性、价格、库存和上下架状态。
- 商品销量和乐观锁版本号由后端维护，不接收前端写入。
- 完成后台商品真实数据库集成测试，覆盖分页筛选、角色权限、编码重复和 CRUD 主流程。
- 完成普通用户商品目录 `GET /api/products` 和 `GET /api/products/{id}`。
- 普通用户商品目录允许匿名访问，只查询已上架、未删除商品。
- 普通用户商品响应隐藏精确库存、乐观锁版本和后台商品编码，只返回是否有货。
- 完成普通用户商品目录真实数据库集成测试，覆盖分页筛选、详情和下架商品不可见。

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
2. 补充订单接口设计。
3. 实现订单 Entity / Mapper / Service / Controller。
4. 增加订单创建、列表和详情集成测试。

## 与其他分支的关联

- 数据库设计分支提供表结构。
- Redis 秒杀分支会扩展本后端。
- API 设计分支提供接口规范。
- LangGraph Agent 分支会调用本后端接口。

## 本分支交接摘要

截至 2026-07-12，Java 后端已完成基础骨架、认证权限、商品分类和商品管理 MVP。下一步进入订单管理。
