# 分析：代理内置 shell 执行 Gradle 命令的行为（Windows）

> 修订时间：2026-10-05（初版 2026-10-02 的结论有误，本版直接修订，不保留原结论）
> 范围：通过内置 shell、IDEA Terminal、IDEA Run Configuration 三种方式执行 `./gradlew test`
> 结论：**首选使用代理内置 shell 执行 Gradle 任务，并始终附带 `--no-daemon --console=plain`。** 省略 `--no-daemon` 时，默认模式下的 Gradle daemon 会继承代理 shell 的 stdout/stderr，工具需等待管道 EOF，导致任务结束后仍长时间无输出、看似卡住。

## 一、结论

- **首选**：通过代理内置 shell 执行 Gradle 任务（测试、构建等），并附带 `--no-daemon --console=plain`。这样能及时返回，并流式输出完整可读的结果。
- **必须带 `--no-daemon`**：默认 daemon 模式下，长生命周期的 Gradle daemon 继承代理 shell 的 stdout/stderr，输出收集在任务结束后仍会阻塞——实测超过 10 分钟无输出（构建实际早已完成）。`--console=plain` 则避免进度控制转义字符混入捕获输出。
- 每次调用都应设置合理的硬超时；但超时（或长时间无输出）本身**不**表示任务失败，结论前应先核对 `build/test-results/**/*.xml` 与 `build/reports/**`。
- **特殊场景**可改用 IDEA Terminal 或 IDEA Run Configuration，但需注意其取舍（见第二节）。

## 二、验证与结果（2026-10-05）

对同一目标（`SnippetMatchTest` + `TemplateInfoTest`，共 15 个用例、1 个失败）分别用三种方式执行。除内置 shell 的“不加 `--no-daemon`”例外项外，每种方式各执行 2 次复核，结果完全一致。

| 执行方式 | 命令 / 配置 | 返回 | 结果 | 备注 |
| --- | --- | --- | --- | --- |
| 内置 shell（带 `--no-daemon --console=plain`） | `./gradlew test --tests ... --no-daemon --console=plain` | 退出码 `1` | 15 tests, 1 failed | 输出完整可读；冷启动约 3 分钟，预热后约 45 秒 |
| 内置 shell（不带 `--no-daemon`） | 同上但省略 `--no-daemon` | 无返回 | — | 超过 10 分钟无输出，判定为卡住 |
| IDEA Terminal | 相同命令（`.\gradlew.bat`） | 退出码 `1` | 15 tests, 1 failed | MCP 捕获输出在约 70KB 处被截断，末尾摘要丢失；结果需从报告或退出码读取 |
| IDEA Run Configuration | 命名配置 `SnippetMatchTest`；由 `TemplateInfoTest.kt` 运行点生成的临时配置 | `1` / `0` | 8/1、7/0 | `output` 为空；结果从报告读取 |

补充观察：

- IDEA Terminal 首次以 `./gradlew` 调用报 `CreateProcess error=2`；在 IDEA 的 PowerShell 终端中应使用 `.\gradlew.bat`。
- 每次 `:test --tests <单个类>` 会重建 `build/test-results/test`，故单独运行只保留最后一次报告；如需合并结果，需在同一命令/配置中带上多个 `--tests`。

## 三、原因

- **默认 daemon**：daemon 是长生命周期的子进程，会继承代理 shell 的 stdout/stderr。工具需要等待输出管道 EOF；只要 daemon 仍存活，即使测试进程已结束，管道也不会关闭，调用便表现为“卡住”。
- **`--no-daemon`**：任务使用一次性进程，结束后退出并释放管道，工具及时返回。
- 早前的 2026-10-02 初版曾将 `--no-daemon` 也判为超时，并据此归因为 OpenCode shell 层的固有问题；本次在同一环境多次复现均无法重现该结论，实际原因是**超时阈值过短/环境差异**，故本版不再保留原结论。

## 四、操作规范

与 `AGENTS.md`、`agents/rules/test.md` 保持一致：

- 构建、测试等 Gradle 任务一律通过内置 shell 执行，并附加 `--no-daemon --console=plain`。
- 短查询（`./gradlew help`、`./gradlew --status`）同样追加 `--no-daemon` 并设置硬超时。
- 特殊场景才使用 IDEA Terminal 或 Run Configuration，并按第二节的取舍读取结果。
- 诊断疑似卡住时，先检查已有 Gradle/Java 进程，只清理由本次诊断创建的 daemon；不要终止其他工作进程。

## 五、参考

- [OpenCode #25038](https://github.com/anomalyco/opencode/issues/25038)：Windows 11 + PowerShell 下，长时间 Gradle 构建即使已经显示 `BUILD SUCCESSFUL` 仍不返回。该 issue 已关闭，并关联 PR #29831 和 #42756；本地未核对当前运行时版本是否包含对应修复，不能据此假定问题已消失。
- [OpenCode #29822](https://github.com/anomalyco/opencode/issues/29822)：Windows shell 在命令进程退出后仍可能等待，未将结果返回给模型。该 issue 已以 `not planned` 关闭。
- [OpenCode #32504](https://github.com/anomalyco/opencode/issues/32504)：Windows shell 会等待 stdout/stderr 管道 EOF；若存在继承管道的子进程，工具会一直等到超时。这与“默认 daemon 导致任务结束后仍不返回”的现象一致。

未检索到 Paseo 自身直接执行 shell 子进程、且与此现象匹配的公开 issue。Paseo 的相关超时 issue 主要涉及代理启动或连接，因此当前应将问题归因限定为 OpenCode shell 层，Paseo 仅是承载环境。
