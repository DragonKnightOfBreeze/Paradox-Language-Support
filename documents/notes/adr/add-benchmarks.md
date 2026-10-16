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

## 任务

- [ ] P4 完善基准测试
  - [ ] 改为使用 `ChronicleAssume.includeBenchmark()` + JMH
  - [ ] 输出带时间戳的测试报告到 `documents/reports/benchmarks/{timestamp}/`（并编写相关的 python 脚本）
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
