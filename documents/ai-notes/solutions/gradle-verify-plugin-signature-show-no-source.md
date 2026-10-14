# verifyPluginSignature 显示 NO-SOURCE（未找到可校验的签名包）

> - 模块：构建

- **背景**
  - 在 `build.gradle.kts` 中已配置 `intellijPlatform { signing { ... } }`（参见 `~179–183` 行），并在系统中设置了 `CERTIFICATE_CHAIN_PLS`、`PRIVATE_KEY_PLS`、`PRIVATE_KEY_PASSWORD_PLS` 等环境变量。
- **现象**
  - 执行：
    ```powershell
    .\gradlew verifyPluginSignature -i
    ```
    输出：
    ```
    > Task :verifyPluginSignature NO-SOURCE
    Skipping task ':verifyPluginSignature' as it has no source files and no previous output files.
    ```
- **排查过程**
  - 检查 `build/distributions` 目录，未见包含 `-signed.zip` 的插件包。
  - 查阅 JetBrains 文档可知：`verifyPluginSignature` 仅“校验”，不会触发签名或构建；若没有已签名 ZIP 作为输入，会被标记为 NO-SOURCE。
- **结论（根因）**
  - 没有可供校验的输入文件（已签名的插件 ZIP），因此任务被标记为 NO-SOURCE。这与是否重启 IDE/电脑无关。
- **解决步骤**
  1. 重启 Gradle 守护进程以便读取新的环境变量（可选）：
     ```powershell
     .\gradlew --stop
     ```
  2. 先签名再校验（Wrapper + PowerShell 下注意前缀 `.` 和 `\`）：
     ```powershell
     .\gradlew signPlugin verifyPluginSignature -i
     ```
  3. 验证产物是否生成（应看到 `-signed.zip`）：
     ```powershell
     Get-ChildItem -File .\build\distributions
     ```
- **注意事项**
  - 优先使用 `.\gradlew`，避免全局 Gradle 带来的环境差异。
  - 当前构建脚本使用环境变量值作为证书链与私钥的“内容”。若提供的是“文件路径”，需改用插件支持的文件式配置方式（否则会解析失败）。
  - 如果终端会话较早启动，可能未继承新的环境变量；用 `.\gradlew --stop` 或新开终端即可，无需重启电脑。
- **参考链接**
  - JetBrains 插件签名文档：https://plugins.jetbrains.com/docs/intellij/plugin-signing.html