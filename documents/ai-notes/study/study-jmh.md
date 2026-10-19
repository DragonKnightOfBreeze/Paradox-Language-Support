# JMH 学习笔记（Java / Kotlin 最佳实践与本项目落地）

> 更新时间：2026-10-19
>
> 适用版本：JMH `1.37`、JMH Gradle Plugin `0.7.3`、Gradle `9.8`、JDK `21`、Kotlin `2.1.20`

## 目录

- **JMH 是什么**
- **版本与依赖**
- **Java 项目的标准用法**
- **Kotlin 项目的特殊处理**
- **JMH 核心注解与概念**
- **编写可靠基准测试的最佳实践**
- **运行、结果分析与报告**
- **本项目中的落地方式**
- **常见坑与排查**
- **参考链接**

---

## JMH 是什么

- **JMH（Java Microbenchmark Harness）** 是 OpenJDK 提供的 JVM 微基准测试框架。
- 它通过**独立 JVM 进程**、**预热迭代**、**测量迭代**和**死代码消除（DCE）抑制**等手段，尽量把 JIT、GC、类加载、CPU 频率等因素对结果的影响降到可控范围。
- 它适合评测：
  - 组件类、策略类、匹配器；
  - 扩展方法（包括标有 `@Fast` 的版本）；
  - 查询/遍历 API 的时间复杂度与空间复杂度表现；
  - 缓存命中/未命中路径。
- 它不适合直接评测：
  - 完整业务流程的端到端耗时（应使用宏观基准或 profiler）；
  - 依赖真实 IDE 索引、真实游戏目录等重环境的操作（这类场景更适合单独设计集成基准）。

**核心原则**：JMH 给出的是**可复现的相对性能证据**，不是绝对性能承诺。比较不同实现时，必须保证硬件、JVM、类路径、预热和测量参数一致。

---

## 版本与依赖

截至 2026-10，Maven Central 上 `org.openjdk.jmh:jmh-core` 的最新版本仍是 **`1.37`**（2023-08 发布）。

| 组件 | 版本 | 说明 |
|---|---|---|
| `org.openjdk.jmh:jmh-core` | `1.37` | JMH 运行时与注解 |
| `org.openjdk.jmh:jmh-generator-annprocess` | `1.37` | 基于注解处理器生成基准类（Java 常用） |
| `org.openjdk.jmh:jmh-generator-bytecode` | `1.37` | 基于字节码生成基准类（JMH Gradle Plugin 使用） |
| `me.champeau.jmh` | `0.7.3` | JMH Gradle 插件（2025-01 发布） |
| `org.jetbrains.kotlinx:kotlinx-benchmark` | `0.5.0` | Kotlin 的基准工具，JVM 端底层仍使用 JMH |

> 注意：`kotlinx-benchmark` 适合 Kotlin Multiplatform；本项目只需要 JVM 基准，直接使用 JMH 更简单，也便于与现有 `ChronicleAssume` 测试机制结合。

---

## Java 项目的标准用法

### 1. 依赖

Maven：

```xml
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.37</version>
</dependency>
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.37</version>
    <scope>provided</scope>
</dependency>
```

Gradle：

```kotlin
dependencies {
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}
```

### 2. 编写基准

```java
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class MyBenchmark {
    @Benchmark
    public boolean matches() {
        return SomeMatcher.matches("foo/bar", "foo/**");
    }
}
```

### 3. 打包与运行

Java 项目通常用 `maven-shade-plugin` 或 Gradle Shadow 打成可执行 JAR：

```bash
java -jar target/benchmarks.jar
```

也可以只编译基准类，然后通过 JMH Runner API 运行。**不要**把 `@Benchmark` 方法当作普通 JUnit 测试直接调用，否则测到的是“当前 JVM 里的普通方法调用”，而不是 JMH 受控迭代。

---

## Kotlin 项目的特殊处理

Kotlin 与 Java 的差异主要集中在**可见性、final 和注解目标**上。

### 1. 类和基准方法必须是 `open`

JMH 会生成基准类的子类，并重写基准方法。Kotlin 的类和成员默认是 `final`，因此必须显式开放：

```kotlin
@State(Scope.Benchmark)
open class MyBenchmark {
    @Benchmark
    open fun matches(): Boolean {
        return SomeMatcher.matches("foo/bar", "foo/**")
    }
}
```

也可以使用 `allOpen` 插件自动开放所有标注了 `@State` 的类：

```kotlin
plugins {
    kotlin("plugin.allopen") version "2.1.20"
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}
```

> 显式写 `open` 更直观，也更适合只有少量基准类的项目；`allOpen` 更适合基准类很多的项目。

### 2. 方法可见性保持 `public`

不要把基准类或基准方法声明为 `internal`。`internal` 在字节码层面会做名称修饰，JMH 的生成器可能无法正确识别或调用。

### 3. 注解目标

Kotlin 属性上的 Java 注解需要关注 use-site target：

```kotlin
@State(Scope.Benchmark)
open class ParamBenchmark {
    // @Param 的 Java 目标是 FIELD；显式使用 @field: 更稳妥
    @field:Param("1", "2", "4", "8")
    var size: Int = 0

    @Benchmark
    open fun run(): Int = work(size)
}
```

- `@Param`、`@State`、`@Benchmark` 等 JMH 注解直接标注即可。
- 如果属性没有 backing field（例如自定义 getter），注解可能不会出现在字段上；此时应改为字段或使用 `@JvmField`。
- Kotlin 的 `const val` 可以用于 `@OperationsPerInvocation` 等需要编译期常量的注解参数。

### 4. 入口方法

如果希望在基准类里提供 `main`，使用 `@JvmStatic`：

```kotlin
object MyBenchmarkRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        // Runner API
    }
}
```

### 5. 生成方式

- **推荐**：使用 JMH Gradle Plugin。它使用 `jmh-generator-bytecode`，无需 kapt，支持 Kotlin 源集，且会自动处理 `src/jmh` 的编译与生成。
- **不推荐**：在大型 Kotlin 测试源集上使用 kapt。kapt 会为整个测试源集生成 stub，成本高，且生成资源需要额外配置到 classpath。

---

## JMH 核心注解与概念

### `@Benchmark`

标记一个基准方法。JMH 会为每个基准方法生成独立的运行入口。

### `@State`

定义基准状态对象及其生命周期：

- `Scope.Benchmark`：所有线程共享一个实例；
- `Scope.Thread`：每个线程一个实例；
- `Scope.Group`：线程组内共享。

状态对象中的字段通常用于放置输入数据、待测实例和 `Blackhole` 之外的可变状态。

### `@Setup` / `@TearDown`

用于初始化和清理，级别包括：

- `Level.Trial`：整个基准运行一次；
- `Level.Iteration`：每个测量迭代一次；
- `Level.Invocation`：每次调用一次（可能显著影响测量，谨慎使用）。

### `@BenchmarkMode`

常用模式：

- `AverageTime`：每次操作平均耗时（最直观，推荐用于比较单次调用成本）；
- `Throughput`：单位时间完成的操作数；
- `SampleTime`：采样耗时分布；
- `SingleShotTime`：只运行一次，适合冷启动/一次性操作。

### `@OutputTimeUnit`

指定输出时间单位。比较“单次匹配”时通常用 `NANOSECONDS`。

### `@Warmup` / `@Measurement` / `@Fork`

- `@Warmup`：预热迭代，让 JIT 完成编译、缓存完成填充；
- `@Measurement`：正式测量迭代；
- `@Fork`：独立 JVM 进程数。**建议至少 `@Fork(1)`**，避免同一 JVM 中的编译产物和状态污染结果。

### `@OperationsPerInvocation`

当一次基准方法调用内部执行了 N 次目标操作时，用该注解告诉 JMH 折算：

```kotlin
private const val OPERATIONS = 11 * 11

@OperationsPerInvocation(OPERATIONS)
open class MatcherBenchmark {
    @Benchmark
    open fun matches(blackhole: Blackhole) {
        for (pattern in patterns) {
            for (path in paths) {
                blackhole.consume(Matcher.matches(path, pattern))
            }
        }
    }
}
```

### `Blackhole`

用于消费计算结果，防止 JIT 把“没有副作用的计算”整个优化掉：

```kotlin
@Benchmark
open fun matches(blackhole: Blackhole) {
    blackhole.consume(SomeMatcher.matches("foo/bar", "foo/**"))
}
```

也可以直接返回结果，JMH 会隐式消费返回值。对于循环内多个结果，使用 `Blackhole` 更清晰。

---

## 编写可靠基准测试的最佳实践

### 1. 先保证“测的是目标代码”

- 不要在基准方法里写与目标无关的重逻辑。
- 不要把 `System.nanoTime()` / `measureTimeMillis` 和 JMH 混用。
- 不要用常量直接调用，否则可能触发常量折叠：

```kotlin
// 不推荐：输入是编译期常量，JIT 可能直接算出结果
@Benchmark
fun bad(): Boolean = Matcher.matches("foo/bar", "foo/**")

// 推荐：输入来自 @State 字段
@State(Scope.Benchmark)
open class GoodBenchmark {
    var input = "foo/bar"
    var pattern = "foo/**"

    @Benchmark
    open fun good(): Boolean = Matcher.matches(input, pattern)
}
```

### 2. 处理死代码消除

- 返回值给 JMH 消费；
- 或者使用 `Blackhole`；
- 循环内的每个结果都要消费，否则 JIT 可能只保留最后一次。

### 3. 避免在基准方法里写大循环

JMH 推荐“一次调用 = 一次操作”。如果必须循环：

- 使用 `@OperationsPerInvocation(N)` 折算；
- 使用 `Blackhole` 消费每次结果；
- 确认循环没有被 JIT 展开/消除到失真。

### 4. 预热要覆盖缓存和 JIT

- 对于带缓存的实现（如正则缓存、Caffeine 缓存），预热迭代会填充缓存；
- 如果需要测量“冷启动”，应单独设计基准，并使用 `SingleShotTime` 或每个 invocation 清理缓存；
- 不要把“冷启动”和“缓存命中”混在同一个基准里比较。

### 5. 控制变量

- 同一组比较使用相同的 JVM、堆大小、GC、类路径和 CPU 环境；
- 关闭不必要的后台程序；
- 不要在编译、索引、下载等重负载同时运行基准；
- 至少运行 2~3 次，观察置信区间和运行间波动；
- 如果误差区间很大，优先增加预热/测量迭代或 fork 数，而不是直接相信平均值。

### 6. 合理选择模式

- 比较单次调用成本：`AverageTime` + `NANOSECONDS`；
- 比较吞吐：`Throughput` + `ops/s`；
- 观察长尾/抖动：`SampleTime`；
- 冷启动/一次性初始化：`SingleShotTime`。

### 7. 用 profiler 解释“为什么”

JMH 自带常用 profiler：

```bash
-prof gc       # GC 分配与回收
-prof stack    # 热点调用栈采样
-prof perfasm  # 汇编级分析（通常需要 Linux perf）
```

在 Windows 上，`perfasm` 往往不可用；优先使用 `gc`、`stack` 和 JFR/async-profiler 等替代方案。

---

## 运行、结果分析与报告

### 1. 命令行运行

```bash
java -jar benchmarks.jar -f 1 -wi 5 -i 5 -w 1s -r 1s
```

常用参数：

| 参数 | 含义 |
|---|---|
| `-f` | fork 数 |
| `-wi` | 预热迭代数 |
| `-i` | 测量迭代数 |
| `-w` | 每次预热时长 |
| `-r` | 每次测量时长 |
| `-p` | 参数化 |
| `-prof` | profiler |
| `-rf` | 结果格式（`TEXT`/`CSV`/`JSON` 等） |
| `-rff` | 结果文件 |

### 2. 结果格式

- `TEXT`：人类可读，适合快速查看；
- `CSV`：适合表格处理；
- `JSON`：适合脚本分析和可视化（JMH Visualizer 等）。

建议在正式报告中同时保留：

- JMH 原始结果文件；
- 运行命令与 JVM/硬件信息；
- 对结果的简短解读（不要只贴数字）。

### 3. 结果解读

- 关注 `Score`、`Error`、`CI`，不要只看最好的一次迭代；
- 比较两个实现时关注**比值**，而不是绝对纳秒；
- 如果两个实现的置信区间重叠，差异可能不显著；
- 首次运行（尤其是刚完成编译后）容易受 JIT/后台负载影响，建议先跑一次预热，再跑正式记录。

---

## 本项目中的落地方式

### 目录与任务

- 基准类放在 `src/jmh/kotlin`，由 JMH Gradle Plugin 编译和生成：
  - `AntMatcherBenchmarkTest`
  - `AntFromRegexBenchmarkTest`
- 通过 `benchmark` Gradle 任务运行：

```bash
./gradlew benchmark -Dchronicle.test.include.benchmark=true --no-daemon --console=plain
```

> 在 PowerShell 中，`-D` 参数需要加引号，否则会被 PowerShell 当作参数名截断：
>
> ```powershell
> .\gradlew benchmark "-Dchronicle.test.include.benchmark=true" --no-daemon --console=plain
> ```

- 结果写入 `build/reports/benchmarks/<BenchmarkClassName>.txt`。
- 启动器位于 `src/test/kotlin/icu/windea/pls/core/match/MatchersBenchmarkLauncher.kt`，它调用 `ChronicleAssume.includeBenchmark()` 来控制是否执行。

### 为什么不用普通 JUnit 直接跑 JMH

曾尝试把 JMH `Runner` 放进 IntelliJ Platform 的 `Test` 任务中，结果 JMH fork 时抛出：

```text
CreateProcess error=206, 文件名或扩展名太长。
```

原因是 IntelliJ Platform 测试类路径包含大量平台 JAR 和 JVM 参数，JMH fork 需要把完整 classpath 拼进命令行，超过 Windows 的命令行长度限制。

因此本项目采用：

- **JMH Gradle Plugin** 负责基准编译与生成；
- **独立的 `benchmark` JavaExec 任务**使用精简的 classpath 运行启动器；
- 启动器仍然通过 `ChronicleAssume.includeBenchmark()` 保持“受限测试”的语义。

### 本次验证结果

环境：JDK `21.0.6`（JBR）、Windows、JMH `1.37`、`AverageTime`、5 次预热 + 5 次测量、`@Fork(2)`。

| 基准 | 分数（ns/op） | 误差（99.9%） |
|---|---:|---:|
| `AntMatcherBenchmarkTest.baseline` | 0.784 | ±0.024 |
| `AntMatcherBenchmarkTest.matches` | 175.856 | ±1.670 |
| `AntMatcherBenchmarkTest.matchesIgnoreCase` | 129.402 | ±2.598 |
| `AntFromRegexBenchmarkTest.baseline` | 0.793 | ±0.022 |
| `AntFromRegexBenchmarkTest.matches` | 181.468 | ±8.253 |
| `AntFromRegexBenchmarkTest.matchesIgnoreCase` | 210.153 | ±37.421 |

结论：

- 在正则缓存已预热的前提下，`AntMatcher.matches` 为 `175.9 ns/op`，`AntFromRegexMatcher.matches` 为 `181.5 ns/op`，两者非常接近，`AntMatcher` 约有 **3%** 的优势。
- `AntFromRegexMatcher.matchesIgnoreCase` 的方差明显偏大（`210.2 ± 37.4 ns/op`），单次结果不足以得出稳定结论；需要更多 fork/迭代或 profiler 进一步分析。
- 该结果**不支持**“纯 Kotlin 实现耗时约为正则实现一半”的旧结论。正则缓存命中后，两个实现的单次调用成本处于同一量级。
- 第一次运行曾出现较大抖动（`AntMatcher.matches` 约 `447 ns/op`），后续运行稳定在 `175 ns/op` 左右。这说明**基准结果必须结合运行环境、预热和 fork 数解读**，不能只取单次结果。

---

## 常见坑与排查

### 1. `CreateProcess error=206`（Windows 命令行过长）

- 原因：JMH fork 把完整 classpath 拼进命令行，IntelliJ Platform 测试类路径过长。
- 解决：
  - 使用 JMH Gradle Plugin 的 `jmh` 任务（通过 fat jar 缩短 classpath）；
  - 或使用独立、精简 classpath 的 `JavaExec` 任务；
  - 不要把 JMH 直接塞进依赖整个 IntelliJ Platform 测试框架的 `Test` 任务。

### 2. 第一次运行结果明显偏慢/偏快

- 原因：JIT 编译、类加载、后台编译/索引、GC、CPU 调度。
- 解决：增加预热迭代；在系统空闲时重跑；使用多次 fork；不要用第一次结果做结论。

### 3. 结果被死代码消除

- 现象：某个基准异常快，甚至接近 `0 ns/op`。
- 解决：返回结果给 JMH，或使用 `Blackhole.consume`。

### 4. 常量折叠

- 现象：输入是编译期常量，JIT 直接算出结果。
- 解决：把输入放到 `@State` 字段；必要时通过 `@Param` 或运行时生成输入。

### 5. Kotlin 类/方法没有 `open`

- 现象：JMH 生成失败，或运行时报无法继承/重写。
- 解决：显式 `open`，或对 `@State` 使用 `allOpen`。

### 6. 缓存类基准混淆了冷/热路径

- 现象：同一实现两次运行差异巨大。
- 解决：明确基准目标；冷路径用 `SingleShotTime` 或每次清理缓存；热路径确保预热已填充缓存，并在报告中注明。

### 7. `@Param` 没有生效

- 原因：Kotlin 属性注解没有落到字段上。
- 解决：使用 `@field:Param`、`@JvmField`，或改为普通字段。

---

## 参考链接

- JMH 官方仓库：<https://github.com/openjdk/jmh>
- JMH 官方示例：<https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples>
- JMH 项目主页：<https://openjdk.org/projects/code-tools/jmh/>
- JMH Gradle Plugin：<https://github.com/melix/jmh-gradle-plugin>
- JMH Gradle Plugin 文档：<https://plugins.gradle.org/plugin/me.champeau.jmh>
- JMH Maven Central 元数据：<https://repo1.maven.org/maven2/org/openjdk/jmh/jmh-core/maven-metadata.xml>
- kotlinx-benchmark：<https://github.com/Kotlin/kotlinx-benchmark>
- JMH Visualizer：<https://jmh.morethan.io/>
