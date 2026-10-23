# 完善基准测试

> - 模块：测试 > 基准测试
> - 版本：v3.0.5
> - 日期：2026-10-16
> - 状态：WIP

## 说明

为了更加准确、直观和规范地评估后续的一些有关性能优化的任务，需要完善基准测试（benchmarks）。
这包括调整基准测试的相关开发规范、提供基础设施、完善已有的基准测试，以及额外编写一些必要的基准测试。
另外，还需要更新相关的文档和笔记。

基准测试适用于一些性能敏感，并且需要考虑评估具体性能的代码（组件类、策略类、扩展方法等）。

例如：
- 匹配器（如 `AntMatcher`）。
- 注有 `@Fast` 的扩展方法（如 `mapFast`）。
- 性能敏感的查询操作（期望拥有更少的时间复杂度和空间复杂度，并且期望可以按需中断）。

对于存在常规实现版本的基准测试，通常需要对比项，以更加准确地比较和反映性能差异。
两者应使用同样的数据、状态和操作数（`@OperationsPerInvocation`），并且语义上应尽量等价（然而，有时可能确实不完全等价）。
与此同时，命名上也应可匹配（如 `matchesDateField` 和 `matchesDateFieldWithPrecheck`），且区分各种典型场景（如不同特征的输入数据）。

## 笔记

### 概述

- 基于 JMH 的基准测试类需要以 `Benchmark` 结尾，并位于 `src/jmh/kotlin` 目录下（而非常规的 `src/test/kotlin`）。
- 用于验证基准测试相关行为（如缓存命中、淘汰、容量边界）的 JUnit 单元测试需要以 `BenchmarkTest` 结尾，并位于 `src/test/kotlin` 目录下。
- 出于准确性等方面的考虑，基准测试需要基于 JMH（而非直接基于 `measureTimeMillis` 等来自标准库的方法）。

执行方式：
- 通过 `./gradlew benchmark` 执行（作为单独的 Gradle 任务）。
- 可通过 `./gradlew benchmark --benchmarks=<pattern>` 按需指定要执行的基准测试；该参数可以重复，其参数值中也可以使用通配符 `?` `*`。
- 无匹配项时任务会失败，并列出可用的基准测试。
- 通过 `./gradlew benchmarkReport` 运行基准测试并输出带时间戳的报告；也可以先运行 `./gradlew benchmark`，再运行 `./gradlew benchmarkReport -x benchmark`。

报告输出：
- 通过 `./gradlew benchmarkReport` 输出报告（包括原始报告文件，以及从中提取的总结、元数据信息等）。
- 输出到 `documents/reports/benchmarks/{timestamp}/` 目录下。

### 关于基准测试的启动器 `ChronicleBenchmarkLauncher`

说明：
- 位于以下目录中：`src/jmh/kotlin`
- 完整的类路径：`icu.windea.pls.test.ChronicleBenchmarkLauncher`
- 封装了包括参数解析、类路径扫描在内的一些通用逻辑，作为基准测试的基础设施。

### 关于 `allopen` Gradle 插件

基准测试的类（如 `AntMatcherBenchmark`）和方法（如 `baseline`）必须是开放的（`open ...`），因此可以考虑搭配使用 `allopen` Gradle 插件。

在 `build.gradle.kts` 中：

```kts
// build.gradle.kts
plugins {
    kotlin("plugin.allopen") version "2.1.2" // same with kotlin version
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}
```

### 关于 `kotlinx-benchmark`

官方的 [kotlinx-benchmark](https://github.com/Kotlin/kotlinx-benchmark) 提供了更加标准的方式，以进行 Kotlin 的基准测试。

特点：
- 适用于 Kotlin Multiplatform，不仅限于 Kotlin JVM。
- 可以避免手动创建 `benchmark` Gradle 任务以及启动器

然而，官方给出的最低 Kotlin 版本是 2.2.0，而我们的插件目前兼容的最低 Kotlin 版本（`sinceBuild`）是 2.1.20。
如果要改为直接使用 kotlinx-benchmark，需要考虑更改 `sinceBuild`。

另外，`kotlinx-benchmark` 目前的最新版本为 0.5.0，仍然处于 alpha 状态。

参见：
- [Kotlin/kotlinx-benchmark: Kotlin multiplatform benchmarking toolkit](https://github.com/Kotlin/kotlinx-benchmark)
- [Setting Up Kotlin/JVM and Java Projects for Benchmarking](https://github.com/Kotlin/kotlinx-benchmark/blob/master/docs/kotlin-jvm-project-setup.md)

## 分析

### 主要

- [ ] `SimilarityMatchServiceBenchmarkTest` 中的断言可能会在 CI 中失败（当前的全部4个单测方法）
  - 参见：https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/actions/runs/39277543449/job/118149061308
- [ ] 考虑优化 `icu.windea.pls.core.match.TextMatcher.matchesDateField` - 通过加入预检查来达成快速返回
  - 混合输入约 **414 ns/op**。
  - 有效日期约 **155 ns/op**，无效日期约 **697 ns/op**，无效路径的异常/解析失败成本显著。
  - 加入轻量格式预检查后，混合输入降到约 **190 ns/op**，约提升 **2.2 倍**。
  - 由于参数 `pattern` 是允许自定义的，需要一些额外的处理。
- [ ] `String?.equalsFast` 根据当前的基准测试，相比惯用写法反而通常拥有更差的性能。需要进一步分析。
  - `equalsFast` vs `equals`（ASCII） - 5.326 ns VS 3.048 ns - `@Fast` 反而慢约 75%
  - `equalsFast` vs `equals`（ASCII，`ignoreCase`）-  5.615 ns VS 5.764 ns - 基本持平
  - `equalsFast` vs `equals`（非 ASCII） - 5.528 ns VS 3.367 ns - `@Fast` 反而慢约 64%
  - 标准库的 `String.equals` 很可能使用了高度优化的 intrinsic/向量化实现，而 `equalsFast` 的手写逐字符循环在短字符串上没有优势。
  - 不过本轮只测试了短字符串和非空值。`equalsFast` 仍可能在长 ASCII 字符串、大量 `ignoreCase` 比较或 null 较多的场景有不同表现，建议后续补充长字符串和 null 场景后再决定是否调整使用策略。
- [ ] `buildImmutableSet` 根据当前的基准测试，相比标准的 `buildSet` 显著更慢
  - 初步结论：1 元素月 0.43 倍，4 元素约 7 倍，64 元素约 8.7 倍
  - 这可能是结果不可变性带来的固定成本，或是 expectedSize/构建器策略不合适。
  - 考虑到其突出性，以及此方法目前并未在生产代码中使用，较好的处理方案可能是：将此方法标为 `@Deprecated`（级别：`ERROR` 或 `HIDDEN`）。
- [ ] `List.forEachFast` 根据当前的基准测试，没有额外的性能优势（耗时 + 性能）
  - 基于迭代器的标准 `Iterable.forEach` 在当前 JVM 环境下并不存在 24 B/op 的迭代器分配，这意味着迭代器很可能被 JIT 的逃逸分析/标量替换相处了。
  - 这意味着 `forEachFast` 的主要优势实际上主要来自索引访问（而这并不明显，并不是那么值得），而不是减少临时内存。
  - 这也适用于各种衍生方法，例如 `allFast` `anyFast` `noneFast`。
- [x] `List.removePrefixOrNull` / `List.removeSuffixOrNull`：将 `wildcard != null` 提取到循环外

## 任务

### 主要

- [x] P4 完善基准测试（准备阶段）
  - [x] 改为使用 `ChronicleAssume.includeBenchmark()` + JMH
  - [x] 新增 `benchmark` 任务，以执行基准测试
  - [x] 新增 `benchmarkReport` 任务，以输出带时间戳的测试报告到 `documents/reports/benchmarks/{timestamp}/`
  - [x] 新增 `icu.windea.pls.core.ChronicleBenchmarkLauncher`，作为基准测试的启动器类。
- [x] P4 完善已有的基准测试
  - [x] `icu.windea.pls.core.match.MatchersBenchmark` -> `AntMatcherBenchmark` + `AntFromRegexBenchmark`
  - [x] `icu.windea.pls.core.match.similarity.SimilarityMatchServiceBenchmark`
- [x] P4 编写基准测试，以验证通用基础设施中的部分匹配器组件的性能表现
  - [x] `icu.windea.pls.core.match.GlobMatcherBenchmark`
  - [x] `icu.windea.pls.core.match.PathMatcherBenchmark`
  - [x] `icu.windea.pls.core.match.TextMatcherBenchmark`
- [x] P4 编写基准测试，以验证标有 `@Fast` 的扩展方法相比常规版本的性能表现
  - [x] `icu.windea.pls.core.StdlibFastExtensionsBenchmark` + 衍生项
  - [x] `icu.windea.pls.core.StdlibFastExtraExtensionsBenchmark` + 衍生项
  - [x] `icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark` + 衍生项
  - [x] `icu.windea.pls.core.collections.CollectionFastExtraExtensionsBenchmark` + 衍生项
  - [x] `icu.windea.pls.core.collections.CollectionFastBulderesBenchmark` + 衍生项
- [ ] P4 编写基准测试，以验证查询/遍历时的性能表现
  - 目前包括：List/Stream/Sequence/Query/PushStream
  - 关于这里使用的 Query/PushStream：直接在测试代码中实现并使用
- [ ] P4 执行已有的所有基准测试，输出报告，并编写分析笔记
  - [ ] 输出报告到 `documents/reports/benchmark/`，并且保留在仓库中，作为归档 
  - [ ] 编写分析笔记 `documents/ai-notes/analysis/benchmarks-3.0.5.md`
- [ ] P4 更新相关的文档和笔记
  - [x] 编写学习笔记 `documents/ai-notes/study/study-jmh.md`
  - [ ] 更新 `documents/notes/guidelines.md`
  - [ ] 更新 `documents/notes/adr.md` + 补充 `documents/notes/adr/add-benchmarks.md`

## 重构 & 整理

- [ ] P4 根据基准测试结果和分析结论，重构和优化相关代码，以消除一些显著的性能热点/性能回归
  - [ ] `TextMatcher.matchesDateField`
  - [ ] `String?.equalsFast`
  - [ ] `buildImmutableSet`
- [ ] P4 整理和修订已编写的基准测试代码
  - [ ] 清理文档注释
  - [ ] 应用 `region...endregion` 分组
- [ ] P4 将 `ChronicleBenchmarkLauncher` 中引入的所有系统属性全部移到 `ChronicleCapabilities.Benchmark`，并补充文档注释
  - 包括 `ChronicleBenchmarkLauncher` 自身的文档注释，以及各个相关的能力项的文档注释。

## 参见

关键代码位置：
- [ ] `icu.windea.pls.core.ChronicleBenchmarkLauncher` - 位于 `src/jmh/kotlin` 目录下

相关笔记：
- [ ] `documents/ai-notes/analysis/benchmarks-3.0.5.md`
- [ ] `documents/ai-notes/study/study-jmh.md`
- [ ] `documents/notes/adr/add-benchmarks.md`
- [ ] `documents/notes/guidelines.md` - 其中的相关章节
- [ ] `AGENTS.md` - 受限测试的相关章节 & 补充基准测试的相关章节

相关链接：
- JMH 官方仓库：<https://github.com/openjdk/jmh>
- JMH 项目主页：<https://openjdk.org/projects/code-tools/jmh/>
- JMH Gradle Plugin：<https://github.com/melix/jmh-gradle-plugin>
- kotlinx-benchmark：<https://github.com/Kotlin/kotlinx-benchmark>
- JMH Visualizer：<https://jmh.morethan.io/>
