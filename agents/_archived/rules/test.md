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

- 首选：直接通过代理**内置 shell** 工具执行 Gradle 任务（测试、构建等），并附带 `--no-daemon --console=plain`，例如：`./gradlew test --tests "<全限定类名>" --no-daemon --console=plain`。这样可以及时返回，并流式输出完整可读的结果。
- **必须带上 `--no-daemon`。** 默认 daemon 模式下，长期存活的 Gradle daemon 会继承代理 shell 的 stdout/stderr，导致任务本身早已结束后、输出收集仍然阻塞——shell 看似卡住（实测超过 10 分钟无输出）。`--console=plain` 则可避免进度控制转义字符混入捕获的输出。
- 每次通过 shell 执行 Gradle 都应设置合理的硬超时。但超时（或长时间无输出）本身**不**表示任务失败，结论前应先核对 `build/test-results/**/*.xml` 与 `build/reports/**`。
- `./gradlew help`、`./gradlew --status` 等短查询遵循同样规则：视情况追加 `--no-daemon`，并设置硬超时。
- **特殊场景**下也可考虑 IDEA Terminal 或 IDEA 的 Gradle Run Configuration，但需注意其取舍：IDEA Terminal 的 MCP 捕获输出可能在中部/末尾被截断（应从报告或返回的 exit code 获取结果）；Gradle Run Configuration 仅返回 exit code，`output` 为空（结果同样从报告获取）。
- 排查疑似卡住时，先检查已有 Gradle/Java 进程，只清理由本次诊断创建的 daemon；不要终止其他正在工作的进程。也不要让长期后台子进程继续占用代理 shell 的 stdout/stderr。

## 技术信息

- 工具链：Gradle + Kotlin + IntelliJ Platform API
- 测试框架：JUnit4 + IntelliJ Platform 集成测试