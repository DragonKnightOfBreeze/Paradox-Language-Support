# Caffeine 学习笔记（含与 Guava 的区别与迁移指南）

> 更新时间：2025-09-02 20:31（本地时间）

## 目录
- **Caffeine 是什么**
- **与 Guava Cache 的核心区别**
- **API 对照与迁移指南**
- **Kotlin/Java 代码示例**
- **进阶特性与最佳实践**
- **常见坑与排查建议**
- **参考链接**
- **可扩展内容**

---

## Caffeine 是什么
- **高性能本地缓存库**：Caffeine 是 Guava Cache 的后继者，基于更先进的算法（W-TinyLFU）提供更高的命中率与吞吐。
- **JDK 版本**：主流版本需要 Java 8+。
- **设计目标**：在本地内存缓存场景提供更好的命中率、低延迟与丰富策略。

---

## 与 Guava Cache 的核心区别

- **[算法]**
  - Guava：LRU + 基于访问/写入时间的过期。
  - Caffeine：W-TinyLFU（window + protected + probation）策略，显著提升混合工作负载下的命中率。

- **[时间 API]**
  - Guava：以 `TimeUnit` 为主（如 `expireAfterAccess(10, TimeUnit.MINUTES)`）。
  - Caffeine：以 `java.time.Duration` 为主（如 `expireAfterAccess(Duration.ofMinutes(10))`）。

- **[构建 API]**
  - Guava：`CacheBuilder.newBuilder()`，`CacheLoader`（类继承）。
  - Caffeine：`Caffeine.newBuilder()`，更鼓励 lambda loader（`build(key -> ...)`）。

- **[软/弱引用]**
  - 两者都支持 `weakKeys/weakValues/softValues`。但软引用在现代 JVM 表现不确定，官方更推荐基于 **容量**（`maximumSize/maximumWeight`）+ **过期** 的组合。

- **[统计与监听]**
  - 二者均支持 `removalListener`、`recordStats`。Caffeine 统计细粒度更高，并提供 `policy()` 以运行时观察策略与手动驱逐。

- **[异步]**
  - Caffeine 提供原生 `AsyncCache`/`AsyncLoadingCache`（使用 `CompletableFuture`），Guava 需要额外封装。

- **[兼容层]**
  - Caffeine 提供与 Guava 接近的 API 但非完全二进制兼容；迁移时需改导入与部分 API 调用方式。

---

## API 对照与迁移指南

- **导入**
  - Guava：`com.google.common.cache.*`
  - Caffeine：`com.github.benmanes.caffeine.cache.*`

- **构建器**
  - Guava：`CacheBuilder.newBuilder()`
  - Caffeine：`Caffeine.newBuilder()`

- **加载器**
  - Guava：`CacheLoader<K, V>`（继承类/匿名类）
  - Caffeine：推荐 lambda：`build { key -> load(key) }`

- **过期设置**
  - Guava：`expireAfterAccess(long, TimeUnit)` / `expireAfterWrite(long, TimeUnit)`
  - Caffeine：`expireAfterAccess(Duration)` / `expireAfterWrite(Duration)` / `refreshAfterWrite(Duration)`

- **容量设置**
  - Guava：`maximumSize(long)` / `maximumWeight(long) + weigher`
  - Caffeine：同名 API，策略更优；重量需要 `weigher { k, v -> weight }`

- **获取**
  - Guava：`cache.get(key, Callable<V>)` 或 `LoadingCache.get(key)`
  - Caffeine：`cache.get(key) { k -> loader(k) }` 或 `LoadingCache.get(key)`

- **Map 视图**
  - 二者：`asMap()` 可直接查看/操作。

- **统计**
  - Guava：`recordStats()` + `stats()`
  - Caffeine：`recordStats()` + `stats()`，指标更丰富。

- **迁移清单（Checklist）**
  - 替换所有导入到 `com.github.benmanes.caffeine.cache.*`。
  - 用 `Caffeine.newBuilder()` 替换 `CacheBuilder.newBuilder()`。
  - 用 `Duration` 替换 `TimeUnit` 的所有过期配置。
  - 使用 lambda loader 替换 `CacheLoader`（可读性更好）。
  - 若使用 `softValues/weakValues/weakKeys`，逐一确认在 Caffeine 上的表现与风险（特别是 soft）。
  - 验证 `maximumSize/maximumWeight/weigher` 行为是否符合预期。
  - 若使用 `refreshAfterWrite`，检查 loader 的幂等与超时处理。
  - 开启 `recordStats()` 并在灰度/测试环境观察命中率变化。

**常见 API 对照表**

| 能力 | Guava | Caffeine |
|---|---|---|
| 构建器 | `CacheBuilder.newBuilder()` | `Caffeine.newBuilder()` |
| 过期-访问 | `expireAfterAccess(10, MINUTES)` | `expireAfterAccess(Duration.ofMinutes(10))` |
| 过期-写入 | `expireAfterWrite(10, MINUTES)` | `expireAfterWrite(Duration.ofMinutes(10))` |
| 刷新 | `refreshAfterWrite(10, MINUTES)`(LoadingCache) | `refreshAfterWrite(Duration.ofMinutes(10))` |
| 容量 | `maximumSize(4096)` | `maximumSize(4096)` |
| 加权 | `maximumWeight + weigher` | 同上 |
| 获取 | `cache.get(key, callable)` | `cache.get(key) { k -> ... }` |
| LoadingCache | `build(CacheLoader)` | `build { k -> ... }` |
| 统计 | `recordStats().build()` | `recordStats().build()` |
| 移除监听 | `removalListener(...)` | `removalListener(...)` |
| 异步 | 无原生 Async | `Caffeine.newBuilder().buildAsync{ ... }` |

---

## Kotlin/Java 代码示例

### 1. 简单手动加载（Cache-aside）
```kotlin
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

val cache = Caffeine.newBuilder()
    .maximumSize(4096)
    .expireAfterAccess(Duration.ofMinutes(10))
    .recordStats()
    .build<String, String>()

fun getUserName(id: String): String =
    cache.get(id) { k -> loadFromDb(k) }

fun loadFromDb(id: String): String {
    // TODO: DB 查询
    return "user-$id"
}
```

### 2. LoadingCache（推荐 lambda loader）
```kotlin
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

val loadingCache = Caffeine.newBuilder()
    .maximumSize(4096)
    .expireAfterWrite(Duration.ofMinutes(5))
    .build<String, Int> { key -> key.length }

val v = loadingCache.get("abc") // 3
```

### 3. 异步缓存
```kotlin
import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

val asyncCache = Caffeine.newBuilder()
    .maximumSize(1024)
    .expireAfterAccess(Duration.ofMinutes(2))
    .buildAsync<String, String> { key ->
        // 返回 CompletableFuture
        java.util.concurrent.CompletableFuture.supplyAsync { expensiveLoad(key) }
    }

val future = asyncCache.get("k")
future.thenAccept { println(it) }
```

### 4. 监听与统计
```kotlin
val cache = Caffeine.newBuilder()
    .removalListener<String, Any> { key, value, cause ->
        println("removed key=$key cause=$cause")
    }
    .recordStats()
    .build<String, Any>()

// ... 运行一段时间后
println(cache.stats())
```

### 5. 软/弱引用与嵌套缓存（谨慎）
```kotlin
val outer = Caffeine.newBuilder().weakKeys().build<String, MutableMap<String, Any>>()
val inner = Caffeine.newBuilder().softValues().build<String, Any>()
```
> 提示：软引用的可达性受 GC 行为影响较大，优先考虑 `maximumSize + expire*` 的组合。

---

## 进阶特性与最佳实践

- **W-TinyLFU 策略**：无需手动配置，默认启用；对热点识别更精确。
- **容量优先**：优先设置 `maximumSize` 或 `maximumWeight`，与过期策略组合以避免 cache 膨胀。
- **Duration 化**：统一用 `java.time.Duration`，易读、类型安全。
- **Loader 幂等与超时**：特别是 `refreshAfterWrite`/异步加载路径，确保幂等并设置上游超时与降级。
- **统计与可观测性**：开启 `recordStats()`；结合 logs/metrics 观察命中率与驱逐情况。
- **policy() 观察**：`cache.policy()` 可在运行时查询剩余容量、手动驱逐等。
- **防缓存穿透**：对不存在数据缓存空值/标记位，并设置较短过期。
- **防缓存击穿**：关键热点可使用 `refreshAfterWrite` 或加锁/批量加载策略。
- **线程安全**：Caffeine 是线程安全的，无需额外同步。

---

## 常见坑与排查建议

- **软引用导致的诡异丢失**：在内存压力下 soft values 可能被 GC 回收，优先用 `maximumSize + expire*`。
- **TimeUnit 与 Duration 混用**：迁移后忘记改为 `Duration`，导致编译错误或行为偏差。
- **CacheLoader 到 lambda 的迁移**：`CacheLoader.from(...)` 需要改为 `build { key -> ... }`。
- **泛型推断失败**：Kotlin 中对 `buildCache()` 扩展（若项目自定义）常需要**显式**类型参数，例如 `buildCache<String, Foo> { ... }`。
- **Windows 清理构建失败**：非库问题。遇到 `Unable to delete directory build/...` 多为进程占用，关闭沙箱 IDE/杀软后 `clean build`。
- **权重策略**：使用 `maximumWeight` 必须提供 `weigher`，否则没有效果。

---

## 参考链接
- Caffeine 官方文档：https://github.com/ben-manes/caffeine
- Wiki（设计与算法）：https://github.com/ben-manes/caffeine/wiki
- W-TinyLFU 论文解读（英文）：https://arxiv.org/abs/1512.00727
- Guava Cache 文档：https://github.com/google/guava/wiki/CachesExplained

---

## 可扩展内容
- **实践对比**：在你项目内对比 Guava 与 Caffeine 的命中率与延迟，结合 `recordStats()` 出报告。
- **AsyncLoadingCache 落地**：将 IO 密集型请求改造成异步缓存，评估线程占用与吞吐变化。
- **多级缓存**：Caffeine（L1）+ Redis（L2）示例与穿透/击穿/雪崩防护方案。
- **权重驱逐**：真实对象大小估算与 `weigher` 设计案例。
- **可视化**：基于 `policy()` 导出实时指标，制作仪表盘或可视化图表。

---

## 附：获取流程示意（Mermaid）
```mermaid
flowchart LR
  A[cache.get(key)] -->|hit| B[返回 value]
  A -->|miss| C[loader(key)]
  C --> D[更新缓存]
  D --> B
```
