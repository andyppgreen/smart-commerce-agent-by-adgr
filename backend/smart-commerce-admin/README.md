# Smart Commerce Admin

智能电商项目的 Java 业务后端，当前完成阶段 2 基础功能，后续 Redis、秒杀和 RabbitMQ 能力继续在本模块中扩展。

## 技术基线

- Java 21
- Spring Boot 3.5.16
- MyBatis-Plus 3.5.16
- MySQL + Flyway
- JWT + 自定义角色权限拦截器
- Maven Wrapper
- JUnit 5 + Spring Boot Test

## 已完成模块

- `auth`：登录、JWT 生成与解析、当前登录用户、角色校验。
- `category`：后台商品分类查询、新增、修改和逻辑删除。
- `product`：后台商品管理、普通用户商品目录、库存原子扣减。
- `order`：多商品下单、订单和明细落库、用户与管理员订单查询。
- `common`：统一响应、分页响应、业务异常和全局异常处理。
- `config`：Web 拦截器和 MyBatis-Plus 分页、乐观锁配置。

## 目录结构

```text
src/main/java/com/adgr/smartcommerce/admin/
  auth/       认证和角色权限
  category/   商品分类
  product/    商品与库存
  order/      普通订单
  user/       用户基础层
  role/       角色基础层
  userrole/   用户角色关联基础层
  common/     通用响应与异常
  config/     全局配置

src/main/resources/
  db/migration/                  Flyway 数据库版本脚本
  application.properties        公共配置
  application-local.properties  本地 MySQL 配置
```

## 本地前置条件

1. 安装 Java 21。
2. 启动本地 MySQL，并准备 `smart_commerce` 数据库和应用账号。
3. 不需要单独安装 Maven，统一使用仓库中的 Maven Wrapper。

当前本地默认配置：

| 配置 | 默认值 | 可覆盖环境变量 |
| --- | --- | --- |
| 数据库地址 | `jdbc:mysql://127.0.0.1:3307/smart_commerce` | `SMART_COMMERCE_DB_URL` |
| 数据库用户 | `smart_commerce` | `SMART_COMMERCE_DB_USERNAME` |
| 数据库密码 | `SmartCommerce@123` | `SMART_COMMERCE_DB_PASSWORD` |
| JWT 密钥 | 本地开发默认值 | `SMART_COMMERCE_JWT_SECRET` |

默认值只用于本地开发，部署时必须通过环境变量覆盖密码和 JWT 密钥。

## 启动与验证

在 `backend/smart-commerce-admin` 目录执行：

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

启动后可访问：

- 应用健康接口：`GET http://localhost:8080/api/system/health`
- Actuator 健康接口：`GET http://localhost:8080/actuator/health`

完整测试包含使用 `local` 配置的真实 MySQL 集成测试，因此执行 `mvnw test` 前需要启动本地 MySQL。测试会清理自己创建的数据，并恢复测试期间修改的商品库存。

## 本地测试账号

Flyway 执行 `V1`、`V2`、`V3` 后可使用：

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `Admin@123` | `ADMIN` |
| `alice` | `Admin@123` | `CUSTOMER` |
| `bob` | `Admin@123` | `CUSTOMER` |

## 接口与权限边界

- 匿名接口：登录、商品目录、商品详情、健康检查。
- 登录接口：`GET /api/auth/me`。
- `ADMIN`：商品分类管理、后台商品管理、后台订单查询。
- `CUSTOMER`：创建普通订单、查询自己的订单。

详细接口清单见 `docs/03-api-design.md`。

## 阶段 2 验收结果

- Maven 完整测试：21 项通过，0 失败，0 错误，0 跳过。
- 分类、商品和订单核心链路通过真实 MySQL 集成测试。
- 下单金额由后端计算，订单明细保存商品快照。
- 库存通过带条件的原子 SQL 扣减。
- 订单、订单明细和库存更新位于同一事务中，库存不足时整体回滚。
- 用户只能读取自己的订单，管理员可以读取全部订单。

## 当前未实现

- 用户注册、用户管理、角色管理和角色分配接口。
- Token 刷新、退出登录和失效名单。
- 支付、取消订单、超时关单和库存归还。
- Redis 商品缓存、缓存穿透处理和接口限流。
- 秒杀活动业务、Redis 原子扣减和防重复下单。
- RabbitMQ 异步下单、失败重试和补偿。
- OpenAPI 接口文档和生产部署配置。

这些内容不是阶段 2 的遗漏，而是后续阶段的明确边界。

## 下一阶段交接

1. 将 `feature/java-backend` 合并到 `dev` 并推送远端。
2. 从更新后的 `dev` 创建 `feature/redis-seckill`。
3. 先接入 Redis 和商品详情缓存，不立即进入秒杀异步链路。
4. 缓存阶段需要覆盖命中、未命中、空值缓存和商品变更后失效测试。

当前远端 `origin/dev` 尚未包含数据库设计和 Java 后端提交。开始下一分支前必须先完成第 1 步，不能直接从当前 `origin/dev` 创建 Redis 分支。
