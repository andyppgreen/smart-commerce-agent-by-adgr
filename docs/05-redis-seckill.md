# Redis 秒杀分支

## 分支目标

实现高并发秒杀链路，训练 Redis、RabbitMQ、并发控制和最终一致性能力。

## 当前状态

当前位于 `feature/redis-seckill`。Java 后端基线为 `f7ddfcf`，商品详情 Redis 缓存第一增量已完成实现、测试和本地联调，尚未提交或推送。

## 开发基线

- 上游业务分支：`feature/java-backend`。
- 下一阶段分支：建议使用 `feature/redis-seckill`。
- 分支起点：必须是已经合并数据库设计和 Java 后端的最新 `dev`。
- 现有商品查询入口：`ProductCatalogController` -> `ProductService` -> `ProductMapper`。
- 现有商品写入入口：`ProductController` -> `ProductService`，后续在此处理缓存失效。
- 对外商品接口路径和响应结构保持不变，Redis 只改变内部读取方式。

## 已确定方案

- 使用 Redis 存储秒杀活动库存。
- 使用 Redis 记录用户是否已购买，防止重复下单。
- 使用 RabbitMQ 异步创建订单。
- 使用轮询接口查询秒杀结果。

## 初始流程

```text
管理员创建秒杀活动
 -> 活动开始前加载库存到 Redis
 -> 用户发起秒杀
 -> Redis 判断库存和重复购买
 -> Redis 扣减库存
 -> RabbitMQ 投递下单消息
 -> 消费者异步创建订单
 -> 用户查询秒杀结果
```

## 待解决问题

1. Redis key 如何设计。
2. 扣减库存是否使用 Lua 脚本。
3. MQ 消息失败如何重试。
4. 秒杀成功但订单创建失败如何补偿。
5. JMeter 如何设计压测场景。

## 当前任务

商品详情缓存、缓存失效、缓存穿透处理、商品公共接口固定窗口限流和热门商品缓存已经完成。下一步继续确认 Token 辅助管理或进入秒杀前置设计，暂不直接混入 RabbitMQ。

## 第一阶段交付范围

1. 增加 Spring Data Redis 依赖和本地连接配置。
2. 定义商品详情缓存 key、过期时间和序列化方式。
3. `GET /api/products/{id}` 优先读取缓存，未命中时查询 MySQL 并写入缓存。
4. 商品修改、上下架和逻辑删除后清理对应缓存。
5. 对不存在或不可见商品使用短时间空值缓存，处理缓存穿透。
6. 明确 Redis 不可用时的降级策略，并增加缓存命中、缓存失效、空值缓存和降级场景测试。

状态：以上 6 项已完成。

## 第一阶段实现结果

- 使用 `StringRedisTemplate` 存储匿名商品详情 DTO 的 JSON，不缓存后台商品实体。
- 缓存 key 为 `product:catalog:detail:{id}`。
- 正常数据 TTL 为 1800 秒，空值标记 `__NULL__` 的 TTL 为 60 秒。
- 商品详情使用旁路缓存：先查 Redis，未命中时查询 MySQL，再写入 Redis。
- 商品新增、修改、逻辑删除和库存扣减成功后删除详情缓存。
- Redis 读取失败时回源 MySQL，写入或删除失败时记录告警并继续业务流程。
- 公共 API 路径和响应结构未改变。

## 第一阶段验收

- Maven 完整测试 33 项全部通过，0 失败、0 错误、0 跳过。
- `git diff --check` 通过。
- 本地 Redis 验证首次请求执行 `GET` 后回源并 `SET EX 1800`，第二次请求仅执行 `GET`。
- 不存在商品写入 `__NULL__ EX 60`，后续请求保持原有业务错误响应。
- 商品修改和逻辑删除后均执行对应详情 key 的 `DEL`。
- Redis 测试 key 已清理，未推送远端。

第一阶段暂不实现：

- 秒杀库存扣减。
- Lua 脚本。
- RabbitMQ。
- 分布式锁。
- 热点 key 重建。

这些内容在商品缓存链路稳定后逐步加入，避免把普通缓存和秒杀并发问题混在第一次 Redis 接入中。

## 下一增量候选

1. Token 辅助管理：需要先确定退出登录、刷新或失效名单的业务范围。
2. 秒杀前置设计：需要先确定活动状态、库存预热时机和重复下单约束。

在以上候选中确认一个独立增量后再开发，不同时引入秒杀库存、RabbitMQ 和分布式锁。

## 第二增量实现结果

- 限流拦截器作用于 `/api/products` 和 `/api/products/**`。
- 客户端标识使用 `HttpServletRequest.getRemoteAddr()`，当前不信任外部传入的 `X-Forwarded-For`。
- Redis key 为 `ratelimit:api:products:{clientIp}`。
- 默认固定窗口为 10 秒，每个客户端最多 60 次请求。
- Lua 脚本原子执行 `INCR`，并仅在首次计数时设置 `EXPIRE`。
- 超限返回 HTTP 429、业务码 `42900` 和 `Retry-After`。
- Redis 故障时记录告警并放行请求。
- 已通过 40 项 Maven 测试和真实 Redis 61 次请求验证。

## 热门商品缓存增量实现结果

- 新增 `GET /api/products/hot`，只返回已上架、未删除商品。
- 热门商品按 `sales DESC, id DESC` 排序，默认取前 10 个。
- Redis key 为 `product:catalog:hot`，默认 TTL 为 300 秒，使用 JSON 列表存储商品目录响应。
- 商品新增、修改、逻辑删除和成功扣减库存后删除热门商品 key。
- 库存扣减同时原子增加销量，为热门商品排序提供可验证的数据来源。
- Redis 读写失败时热门商品查询回源 MySQL，缓存操作失败不阻断商品业务。

## 与其他分支的关联

- 数据库设计分支提供秒杀活动和订单表。
- Java 后端分支承载秒杀接口。
- 部署分支提供 Redis 和 RabbitMQ。
- 简历分支会重点包装本模块。

## 本分支交接摘要

截至 2026-07-23，商品详情缓存、商品公共接口限流和热门商品缓存三个 Redis 增量已完成，完整测试由 Java 后端基线的 21 项增加到 44 项。当前使用 Docker Redis 7.4.9 和 Docker MySQL 8.0.46，数据持久化到 D 盘，Redis 与 MySQL 均只映射到本机。下一步先确定 Token 辅助管理或秒杀前置设计，再逐步进入秒杀链路。
