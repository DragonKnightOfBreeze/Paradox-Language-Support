# 报告_#2_补充collections与coroutines单测

## 概述（2025-08-27 02:32）
- 目标：为 `icu.windea.pls.core.collections` 与 `icu.windea.pls.core.coroutines` 编写/补充纯 Kotlin 单测，并通过 Gradle 执行。
- 约定：使用 `*PureTest` 命名，执行 `pureTest` 任务；避免依赖 IntelliJ API；如需测试 Flow，限测试作用域引入 coroutines 依赖。
- 范围：ArrayExtensions、CollectionBuilders、MapExtensions、SequenceExtensions、FlowExtensions。

