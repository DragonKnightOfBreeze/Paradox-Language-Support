# 完善基准测试

> - 模块：测试 > 基准测试
> - 版本：v3.0.5
> - 日期：2026-10-16
> - 状态：WIP

## 说明

为了更加准确、直观和规范地评估后续的一些有关性能优化的任务，需要完善基础测试（benchmarks）。
这包括调整基准测试的相关开发规范、完善已有的基准测试，以及另外编写一些必要的基准测试。

基准测试适用于一些性能敏感，并且需要考虑评估具体性能的代码（组件类、策略类、扩展方法等）。

例如：
- 匹配器（如 `AntMatcher`）。
- 注有 `@Fast` 的扩展方法（如 `mapFast`）。
- 性能敏感的查询操作（期望拥有更少的时间复杂度和空间复杂度，并且期望可以按需中断）。

## 笔记

### 概述

执行机制：
- 基准类放 `src/jmh/kotlin`，由 JMH Gradle Plugin 编译
- 启动器放测试源集，调用 `ChronicleAssume.includeBenchmark()` （如 `icu.windea.pls.core.match.MatchersBenchmarkLauncher`）
- 使用独立的 `benchmark` JavaExec 任务（而非 IntelliJ `Test` 任务）

报告输出约定：
- 当前先写入 `build/reports/benchmarks/<Class>.txt`，后续脚本再复制/汇总到 `documents/reports/benchmarks/{timestamp}/`，并附带 JDK、CPU、命令、JMH 参数等元数据。

### 关于 `allopen` Gradle 插件

基准测试的类（如 `AntMatcherBenchmarkTest`）和方法（如 `baseline`）必须是开放的（`open ...`），因此可以考虑搭配使用 `allopen` Gradle 插件。

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

## 任务

- [ ] P4 完善基准测试
  - [x] 改为使用 `ChronicleAssume.includeBenchmark()` + JMH
  - [ ] 输出带时间戳的测试报告到 `documents/reports/benchmarks/{timestamp}/`（并编写相关的 python 脚本）
  - [x] 编写学习笔记 `documents/notes/adr/add-benchmarks.md`
  - [x] 新增 `benchmark` 任务与启动器
- [ ] P4 完善已有的基准测试
  - [ ] `icu.windea.pls.core.match.MatchersBenchmarkTest` -> `AntMatcherBenchmarkTest` + `AntFromRegexBenchmarkTest`
  - [ ] `icu.windea.pls.core.match.similarity.SimilarityMatchServiceBenchmarkTest`
- [ ] P4 编写基准测试，以验证标有 `@Fast` 的扩展方法相比常规版本的性能表现，并输出报告文档
  - [ ] `icu.windea.pls.core.StdlibFastExtensionsBenchmarkTest`
  - [ ] `icu.windea.pls.core.StdlibFsstExtraExtensionsBenchmarkTest`
  - [ ] `icu.windea.pls.core.collections.CollectionFastExtensionsBenchmarkTest`
  - [ ] `icu.windea.pls.core.collections.CollectionFastExtraExtensionsBenchmarkTest`
- [ ] P4 编写基准测试，以验证查询/遍历时的性能表现，报告文档并输出
  - 目前包括：List/Stream/Sequence/Query/PushStream
  - 关于这里使用的 Query/PushStream：直接在测试代码中实现并使用

## 参见

- `documents/notes/adr/add-benchmarks.md`
- 
