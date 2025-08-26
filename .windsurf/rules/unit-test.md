---
trigger: model_decision
description: 如果你需要编辑、生成或执行项目的单元测试
globs: 
---

# 关于单元测试

## 要点

- 项目代码绝大多数都是 Kotlin，因此如无特殊要求，请同样使用 Kotlin 编写单元测试。
- 项目使用 Gradle 作为构建工具，（通常情况下）使用 JUnit4 作为测试框架。
- 项目没有对 `kotlin-test` 的依赖。
- 如果要为其生成单元测试的代码不依赖 Intellij API，请编写 `纯单测`（`PureTest`）。纯测试的类名以 `PureTest` 结尾，对应的 Gradle 任务是 `pureTest`。
- 否则（依赖 Intellij API），请考虑其复杂度与可行性，如果过于复杂或不可行，仅作说明即可。

## 技术信息

- 工具链：Gradle 8.13、Kotlin 2.1.10、IntelliJ 平台插件 2.7.2、JVM target 21。
- 测试框架：主要是 JUnit4 ，目前未使用 `kotlin-test`，另外也有一些测试是 Intellij API 的集成测试
- 已知告警：存在 Gradle 9 兼容性相关弃用告警，不阻塞构建，后续逐步治理。

## 变更历史
- 2025-08-27：初版创建（AI 自动生成）。
- 2025-08-27：修订（大幅）。