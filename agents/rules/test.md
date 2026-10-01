---
trigger: manual
description: 关于如何编写、生成和执行测试的指南
globs: 
---

# 关于测试

## 要点

- 单元测试：适用于组件/工具/扩展等的测试。通常不需要依赖 IntelliJ Platform API 或其他外部集成。
- 集成测试：适用于 PSI/索引/查询器/语义匹配/语义解析/外部集成等的测试。通常需要依赖 IntelliJ Platform API 或其他外部集成。
- 如无特殊要求，使用 Kotlin 编写单元测试和集成测试。
- 如果必要，在编写测试用例时，通过 `region...endregion` 注释对其进行分组。例如同一测试类中的测试用例足够多，或者可以分为多个模块的场合。
- 如果必要，在编写测试用例时，通过额外的单行注释进行额外说明。例如，可以对测试用例的细节、边界情况、预期结果等进行说明。
- 如果预期测试能够通过，应当首先在警告级别（`--warn`）执行测试。未通过时，再改为在更低的日志级别继续尝试执行测试。

## Gradle 执行环境

- 执行构建、测试等可能耗时数秒以上的 Gradle 任务时，优先使用 IDEA Terminal 或 IDEA 的 Gradle Run Configuration，而不是代理内置 shell。
- Windows 上代理 shell 的标准输入/输出可能被重定向，且其进程或输出收集器可能在 Gradle 任务或其子进程结束后仍然等待。因此，代理 shell 超时本身不表示测试失败，也不应立刻归因于项目代码、Gradle 缓存或 daemon。
- 代理内置 shell 仅适用于 `./gradlew help`、`./gradlew --status` 等短查询，并且必须设置合理的硬超时。遇到超时时，使用 IDEA Terminal 或 Run Configuration 运行同一条目标命令进行对照。
- 排查时先检查已有 Gradle/Java 进程，再分别比较默认 daemon 模式与 `--no-daemon`。只清理由本次诊断创建的 daemon；不要擅自终止其他工作进程。
- 代理 shell 中不要直接启动会派生长期后台子进程的命令。若确有需要，避免子进程继承 stdout/stderr，显式重定向输出，并在结束时检查和清理进程。

## 技术信息

- 工具链：Gradle + Kotlin + IntelliJ Platform API
- 测试框架：JUnit4 + IntelliJ Platform 集成测试