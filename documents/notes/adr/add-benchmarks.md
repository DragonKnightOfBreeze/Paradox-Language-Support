# 完善基准测试

> - 模块：测试 > 基准测试
> - 版本：v3.0.5
> - 日期：2026-10-16
> - 状态：WIP

## 说明

为了更加准确、直观和规范地评估后续的一些有关性能优化的任务，需要完善基础测试（benchmarks）。
这包括调整基准测试的相关开发规范、提供基础设施、完善已有的基准测试，以及额外编写一些必要的基准测试。
另外，还需要更新相关的文档和笔记。

基准测试适用于一些性能敏感，并且需要考虑评估具体性能的代码（组件类、策略类、扩展方法等）。

例如：
- 匹配器（如 `AntMatcher`）。
- 注有 `@Fast` 的扩展方法（如 `mapFast`）。
- 性能敏感的查询操作（期望拥有更少的时间复杂度和空间复杂度，并且期望可以按需中断）。

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

## 关于基准测试的启动器 `ChronicleBenchmarkLauncher`

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

## 任务

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
- [ ] P4 编写基准测试，以验证标有 `@Fast` 的扩展方法相比常规版本的性能表现
  - [ ] `icu.windea.pls.core.StdlibFastExtensionsBenchmark`
  - [ ] `icu.windea.pls.core.StdlibFsstExtraExtensionsBenchmark`
  - [ ] `icu.windea.pls.core.collections.CollectionFastExtensionsBenchmark`
  - [ ] `icu.windea.pls.core.collections.CollectionFastExtraExtensionsBenchmark`
- [ ] P4 编写基准测试，以验证查询/遍历时的性能表现
  - 目前包括：List/Stream/Sequence/Query/PushStream
  - 关于这里使用的 Query/PushStream：直接在测试代码中实现并使用
- [ ] P4 执行已有的所有基准测试，输出报告，并编写分析笔记
- [ ] P4 更新相关的文档和笔记
  - [x] 编写学习笔记 `documents/ai-notes/study/study-jmh.md`
  - [ ] 编写分析笔记 `documents/ai-notes/analysis/benchmarks-3.0.5.md`
  - [ ] 更新 `documents/notes/guidelines.md`
  - [ ] 更新 `documents/notes/adr.md` + 补充 `documents/notes/adr/add-benchmarks.md`

待评估：
- [ ] P3 考虑优化 `icu.windea.pls.core.match.TextMatcher.matchesDateField` - 通过加入预检查来达成快速返回
  - 由于参数 `pattern` 是允许自定义的，需要一些额外的处理。

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
