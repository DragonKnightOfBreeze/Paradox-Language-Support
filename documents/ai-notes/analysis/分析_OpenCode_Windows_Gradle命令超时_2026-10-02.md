# 分析：OpenCode Windows shell 执行 Gradle 测试超时

> 时间戳：2026-10-02T04:19:19+08:00
> 范围：`icu.windea.pls.core.data.JsonServiceTest`
> 结论：OpenCode 的 Windows shell 执行路径问题；未发现项目测试、Gradle daemon 或 Paseo 的直接证据。

## 一、现象与复现

目标测试是无外部依赖的序列化单元测试。开始排查时没有运行中的 Gradle 或 Java 进程。

| 执行路径 | 命令或配置 | 结果 |
| --- | --- | --- |
| OpenCode 内置 shell | `gradlew test --tests "icu.windea.pls.core.data.JsonServiceTest" --no-daemon --console=plain --warn` | 120 秒、60 秒两次均超时 |
| IDEA Terminal | 相同命令 | 退出码 `0`，28.8 秒完成 |
| IDEA Run Configuration | `JsonServiceTest` | 30 秒限制内退出码 `0` |
| OpenCode 内置 shell | 去掉 `--no-daemon` 后的相同测试 | 30 秒超时 |
| OpenCode 内置 shell | `gradlew help --no-daemon --console=plain --warn` | 正常完成 |

OpenCode 内置 shell 中，`[Console]::IsOutputRedirected` 与 `[Console]::IsInputRedirected` 均为 `True`。超时后的进程检查没有发现由该次执行遗留的 Gradle/Java 测试子进程。

## 二、排除项

- 测试代码：IDEA Terminal 和 Run Configuration 均能稳定完成，测试本身不包含等待或网络操作。
- 冷缓存：IDEA 成功执行并预热后，OpenCode 内置 shell 仍然超时。
- Gradle daemon：显式 `--no-daemon` 与默认 daemon 两种模式都超时；排查后已使用 `gradlew --stop` 清理本次产生的 daemon。
- 普通 Gradle 配置：`gradlew help` 能在 OpenCode 内置 shell 中完成。

因此，故障与长时间 Gradle 测试任务在 OpenCode Windows shell 的受重定向标准输入/输出环境中的交互有关，而非本项目的构建逻辑。

## 三、公开 Issue 对照

- [OpenCode #25038](https://github.com/anomalyco/opencode/issues/25038)：Windows 11 + PowerShell 下，长时间 Gradle 构建即使已经显示 `BUILD SUCCESSFUL` 仍不返回。该 issue 已关闭，并关联 PR #29831 和 #42756；本地未核对当前运行时版本是否包含对应修复，不能据此假定问题已消失。
- [OpenCode #29822](https://github.com/anomalyco/opencode/issues/29822)：Windows shell 在命令进程退出后仍可能等待，未将结果返回给模型。该 issue 已以 `not planned` 关闭。
- [OpenCode #32504](https://github.com/anomalyco/opencode/issues/32504)：说明 Windows shell 会等待 stdout/stderr 管道 EOF；若子/孙进程持有继承的管道，工具会一直等到超时。此 issue 的持久后台子进程场景不等同于本次 Gradle 测试，但与本次重定向 I/O 的风险模型相符。

未检索到 Paseo 自身直接执行 shell 子进程、且与此现象匹配的公开 issue。Paseo 的相关超时 issue 主要涉及代理启动或连接，因此当前应将问题归因限定为 OpenCode shell 层，Paseo 仅是承载环境。

## 四、后续操作规范

- 构建、测试、`runIde` 等 Gradle 长任务优先通过 IDEA Terminal 或 IDEA Run Configuration 执行。
- 代理内置 shell 仅用于短命令，例如 `gradlew help`、`gradlew --status`，并始终设置合理的硬超时。
- shell 超时不应直接认定为构建或测试失败；应使用 IDE 路径对照验证。
- 若确需由代理 shell 启动会派生后台进程的命令，避免让后台进程继承 shell 的 stdout/stderr；应显式重定向输出并在完成后检查、清理进程。
- 诊断时分别比较 `--no-daemon` 与默认模式，并在结束后检查 `gradlew --status`；仅清理由本次诊断创建的 daemon。
