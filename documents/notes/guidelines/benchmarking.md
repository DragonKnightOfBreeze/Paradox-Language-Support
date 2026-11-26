# 基准测试指南

> 本文档拆自 [开发规范](../guidelines.md)，明确微基准测试的工程约定、执行方式以及与性能剖析（Profiling）的适用边界。常规单元测试与平台测试规范请见 [testing.md](testing.md)。

## 定位与适用边界（JMH vs Profiling）

- **微基准测试适用范围**：仅用于受控合成环境下对**纯组件、匹配器（如 `AntMatcher`）、策略类、标有 `@Fast` 的扩展方法以及无状态算法**进行微基准 A/B 对比，提供可复现的**相对性能证据**。
- **不能测量插件的实际运行性能**：JMH 微基准受限于固定数据集、JVM 预热模型与精简类路径，**完全脱离了真实 IDE 索引生命周期、读写锁竞争、GC 压力、VFS 监听与大型模组负载**，因此**不能**可靠地代表或测量插件在真实 IDE 中的实际运行性能。
- **真实性能评估手段**：涉及索引构建耗时、项目分析吞吐、内存占用与 UI 响应瓶颈时，必须依赖真实大型游戏环境下的 **IDEA Profiler / JFR / 内存剖析**（参见 [analysis.md](../analysis.md)、[performance-profiling.md](../analysis/performance-profiling.md) 与 [memory-profiling.md](../analysis/memory-profiling.md)）。

---

## 目录、命名与架构约定

- **JMH 基准类**：统一位于 `src/jmh/kotlin/` 目录下，类名以 `Benchmark` 结尾（如 `AntMatcherBenchmark`、`SimilarityMatchServiceBenchmark`）。
- **基准行为校验测试**：用于验证基准相关行为不变量（如缓存命中、淘汰策略、容量边界）的 JUnit 单元测试放在 `src/test/kotlin/` 下，类名以 `BenchmarkTest` 结尾（如 `SimilarityMatchServiceBenchmarkTest`）。
- **技术选型**：基准必须基于 **JMH**，禁止直接使用 `measureTimeMillis` 等粗粒度标准库计时方法。
- **独立启动与精简类路径原因**：在 Windows 上若将 JMH `Runner` 放入依赖整个 IntelliJ Platform 测试框架的 `Test` 任务中，JMH fork 拼接完整类路径会超出命令行长度限制并触发 `CreateProcess error=206`。因此本项目采用 **JMH Gradle Plugin + 独立精简类路径的 `benchmark` JavaExec 任务 + `ChronicleBenchmarkLauncher`**（位于 `src/jmh/kotlin/icu/windea/pls/test/ChronicleBenchmarkLauncher.kt`），不再通过 `ChronicleAssume` 门控。
- **运行时依赖声明**：由于基准进程不包含 IntelliJ Platform 运行时类路径，平台在运行期提供的库（如 `guava`、`fastutil`）已在 `jmh` Gradle 配置中显式声明；若新增基准引入了类似平台运行时依赖，需同步补充到 `jmh` 配置中。

---

## 编写规范与防失真准则

- **开放类与方法**：JMH 会通过子类化与重写生成基准代码，Kotlin 类与 `@Benchmark` 方法必须为 `open`（或由配置了 `@State` 的 `allopen` 插件自动开放）。
- **防止死代码消除（DCE）**：所有 `@Benchmark` 方法必须返回计算结果，或消费到 `Blackhole.consume(...)`，不得出现脱离使用的空跑调用。
- **防止常量折叠**：测试输入必须置于 `@State` 字段中（或通过 `@Param` / 运行时构造注入），禁止直接将编译期字面量常量内联进基准方法体。
- **公平的对照项**：对比优化实现与常规实现时，二者应使用一致的输入数据、状态与 `@OperationsPerInvocation`，保持语义尽量等价并在命名上对齐（如 `matchesDateField` 与 `matchesDateFieldWithPrecheck`）。
- **结果解读**：重点关注 `Score`、`Error` 与置信区间（`CI`）的**相对比值**，切勿跨机器或以刚编译完首次运行的结果作为性能定论。

---

## 执行与报告输出

- 运行全部基准测试：
  ```bash
  ./gradlew benchmark --no-daemon --console=plain
  ```
- 按类名、方法名或通配符过滤运行（`--benchmarks` 可重复传入；PowerShell 中含 `*` 或 `?` 时建议加引号）：
  ```bash
  ./gradlew benchmark --benchmarks=AntMatcherBenchmark --no-daemon --console=plain
  ./gradlew benchmark --benchmarks="icu.windea.pls.core.match.*Benchmark" --no-daemon --console=plain
  ```
- 生成带时间戳的报告（或复用已有结果跳过重跑）：
  ```bash
  ./gradlew benchmarkReport --no-daemon --console=plain
  ./gradlew benchmarkReport -x benchmark --no-daemon --console=plain
  ```
- **报告产物位置**：`documents/reports/benchmarks/{timestamp}/`，包含原始 JSON、综合汇总 `summary.md`、各类明细 `<SimpleName>.txt` 与环境元数据 `metadata.md`。

参见决策记录：[adr/add-benchmarks.md](../adr/add-benchmarks.md)。
