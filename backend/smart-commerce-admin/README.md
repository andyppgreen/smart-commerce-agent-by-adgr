# Smart Commerce Admin

智能电商项目的 Java 业务后端。阶段 2 基础功能已验收完成，阶段 3 已完成商品详情、热门商品缓存和接口限流增量，秒杀和 RabbitMQ 能力后续继续在本模块中扩展。

## 技术基线

- Java 21
- Spring Boot 3.5.16
- MyBatis-Plus 3.5.16
- MySQL + Flyway
- Spring Data Redis
- JWT + 自定义角色权限拦截器
- Maven Wrapper
- JUnit 5 + Spring Boot Test

## 已完成模块

- `auth`：登录、JWT 生成与解析、当前登录用户、角色校验。
- `category`：后台商品分类查询、新增、修改和逻辑删除。
- `product`：后台商品管理、普通用户商品目录、详情缓存、缓存失效和库存原子扣减。
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
  application-local.properties  本地 MySQL 和 Redis 配置
```

## 本地前置条件

1. 安装 Java 21。
2. 安装并启动 Docker Desktop。
3. 启动项目依赖容器：`docker start smart-commerce-mysql smart-commerce-redis`。
4. 不需要单独安装 Maven，统一使用仓库中的 Maven Wrapper。

当前本地默认配置：

| 配置 | 默认值 | 可覆盖环境变量 |
| --- | --- | --- |
| 数据库地址 | `jdbc:mysql://127.0.0.1:3307/smart_commerce` | `SMART_COMMERCE_DB_URL` |
| 数据库用户 | `smart_commerce` | `SMART_COMMERCE_DB_USERNAME` |
| 数据库密码 | `SmartCommerce@123` | `SMART_COMMERCE_DB_PASSWORD` |
| 本地 MySQL 容器 | `smart-commerce-mysql`，MySQL 8.0.46 | - |
| 本地 Redis 容器 | `smart-commerce-redis`，Redis 7.4.9 | - |
| MySQL 数据目录 | `D:\docker-data\smart-commerce-mysql` | - |
| Redis 数据目录 | `D:\docker-data\smart-commerce-redis` | - |
| Redis 地址 | `127.0.0.1` | `SMART_COMMERCE_REDIS_HOST` |
| Redis 端口 | `6379` | `SMART_COMMERCE_REDIS_PORT` |
| Redis 密码 | 空 | `SMART_COMMERCE_REDIS_PASSWORD` |
| Redis 超时 | `2s` | `SMART_COMMERCE_REDIS_TIMEOUT` |
| 商品详情缓存时间 | `1800` 秒 | `SMART_COMMERCE_PRODUCT_DETAIL_CACHE_TTL_SECONDS` |
| 商品空值缓存时间 | `60` 秒 | `SMART_COMMERCE_PRODUCT_NULL_CACHE_TTL_SECONDS` |
| 热门商品缓存时间 | `300` 秒 | `SMART_COMMERCE_HOT_PRODUCT_CACHE_TTL_SECONDS` |
| 热门商品数量 | `10` 个 | `SMART_COMMERCE_HOT_PRODUCT_LIMIT` |
| 限流开关 | `true` | `SMART_COMMERCE_RATE_LIMIT_ENABLED` |
| 限流次数 | `60` 次 | `SMART_COMMERCE_RATE_LIMIT_MAX_REQUESTS` |
| 限流窗口 | `10` 秒 | `SMART_COMMERCE_RATE_LIMIT_WINDOW_SECONDS` |
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

运行商品缓存链路时需要启动 Redis。Redis 不可用时，商品详情读取会降级查询 MySQL，缓存写入和删除会降级为无操作，不阻断业务响应。

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

## Redis 第一增量验收结果

- 商品详情缓存 key 为 `product:catalog:detail:{id}`，正常数据 TTL 为 1800 秒。
- 不存在或不可见商品写入 `__NULL__`，TTL 为 60 秒，降低重复穿透 MySQL 的请求量。
- 商品新增、修改、逻辑删除和库存扣减成功后清理对应详情缓存。
- Redis 读写异常不会改变原有商品接口的业务语义。
- Maven 完整测试：33 项通过，0 失败，0 错误，0 跳过。
- 本地 Redis 联调已验证首次回源写入、二次命中、空值命中、修改失效和删除失效。

## Redis 限流第二增量验收结果

- `/api/products` 和 `/api/products/**` 按客户端 IP 进行固定窗口限流。
- 默认窗口为 10 秒，单个客户端默认最多 60 次请求。
- Redis Lua 脚本原子执行 `INCR` 和首次 `EXPIRE`，避免并发请求产生无 TTL 计数键。
- 超限返回 HTTP 429、业务码 `42900` 和 `Retry-After` 响应头。
- Redis 不可用时限流降级为放行，避免缓存基础设施故障阻断商品接口。
- Maven 完整测试：40 项通过，0 失败，0 错误，0 跳过。
- 本地真实验证中同一客户端前 60 次返回 200，第 61 次返回 429。

## Redis 热门商品缓存增量验收结果

- `GET /api/products/hot` 返回已上架、未删除商品，按 `sales DESC, id DESC` 排序，默认返回前 10 个。
- 热门商品缓存 key 为 `product:catalog:hot`，默认 TTL 为 300 秒。
- 商品新增、修改、逻辑删除和库存扣减成功后清理热门商品缓存。
- 商品扣库存同时原子增加 `sales`，保证热门排序数据可以随订单变化更新。
- Maven 完整测试：44 项通过，0 失败，0 错误，0 跳过。

## 当前未实现

- 用户注册、用户管理、角色管理和角色分配接口。
- Token 刷新、退出登录和失效名单。
- 支付、取消订单、超时关单和库存归还。
- 秒杀活动业务、Redis 原子扣减和防重复下单。
- RabbitMQ 异步下单、失败重试和补偿。
- OpenAPI 接口文档和生产部署配置。

这些内容不是阶段 2 的遗漏，而是后续阶段的明确边界。

## 下一增量边界

1. Token 辅助管理需要先确定退出登录、刷新或失效名单的业务范围。
3. 秒杀库存、RabbitMQ、分布式锁和热点 key 重建不与普通缓存增量混合实现；秒杀专用 Lua 脚本另行设计。
