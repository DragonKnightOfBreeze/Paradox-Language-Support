# signPlugin 抛出 NPE：PrivateKeyUtils.loadKeyPair（密钥/证书内容格式不正确）

> - 模块：构建

- **背景**
  - 已按 `build.gradle.kts` 中的 `intellijPlatform { signing { ... } }` 进行签名配置：
    - `certificateChain = envVars("CERTIFICATE_CHAIN_PLS")`
    - `privateKey = envVars("PRIVATE_KEY_PLS")`
    - `password = envVars("PRIVATE_KEY_PASSWORD_PLS")`
- **现象**
  - 执行：
    ```powershell
    .\gradlew signPlugin verifyPluginSignature -i
    ```
  - 失败并抛出 NPE，日志摘录（详见 `notes/ai-notes/logs/sign-verify-latest.log`）：
    ```text
    > Task :signPlugin FAILED
    ... org.jetbrains.zip.signer.ZipSigningTool sign ... -key <long-base64> -cert <long-base64> -key-pass ********
    Exception in thread "main" java.lang.NullPointerException
      at org.jetbrains.zip.signer.signer.PrivateKeyUtils.loadKeyPair(PrivateKeyUtils.kt:62)
    ```
- **诊断**
  - 从命令行回显可见：`-key` 与 `-cert` 以“单行 Base64”形式传入，缺少 PEM 头尾与换行（如 `-----BEGIN PRIVATE KEY-----`/`-----END PRIVATE KEY-----`、`-----BEGIN CERTIFICATE-----`/`-----END CERTIFICATE-----`）。
  - `marketplace-zip-signer` 解析私钥时未能识别有效内容，最终在 `PrivateKeyUtils.loadKeyPair` 处触发 NPE。
  - 另一个常见踩坑：提供的是 PKCS#1（`BEGIN RSA PRIVATE KEY`）而非 PKCS#8（`BEGIN PRIVATE KEY` 或 `BEGIN ENCRYPTED PRIVATE KEY`）。
- **结论（根因）**
  - 环境变量中的签名材料内容格式不符合签名工具期望：
    - 丢失 PEM 头尾与换行（仅有 Base64 主体）。
    - 或私钥为 PKCS#1，签名器期望 PKCS#8。
- **解决步骤**
  1. 准备“正确格式”的 PEM 内容：
    - 私钥：PKCS#8（未加密：`BEGIN PRIVATE KEY`；加密：`BEGIN ENCRYPTED PRIVATE KEY`）。
    - 证书链：一个或多个 `BEGIN CERTIFICATE`/`END CERTIFICATE` 块，按链顺序拼接。
  2. 将 PEM 原样（含头尾与换行）写入环境变量（示例，当前会话）：
     ```powershell
     $env:PRIVATE_KEY_PLS        = Get-Content -Raw -LiteralPath 'D:\secrets\pls\private_key_pkcs8.pem'
     $env:CERTIFICATE_CHAIN_PLS  = Get-Content -Raw -LiteralPath 'D:\secrets\pls\cert_chain.pem'
     $env:PRIVATE_KEY_PASSWORD_PLS = '你的私钥口令'
     ```
    - 若变量里存放的是“转义换行”（如 `\n`），需反转义为真实换行。
  3. 若你的私钥是 PKCS#1，请先转换为 PKCS#8（示例命令，需安装 OpenSSL）：
    - 未加密：
      ```bash
      openssl pkcs8 -topk8 -inform PEM -in private_rsa.pem -out private_pkcs8.pem -nocrypt
      ```
    - 加密：
      ```bash
      openssl pkcs8 -topk8 -inform PEM -in private_rsa.pem -out private_pkcs8.pem -v2 aes-256-cbc \
        -passin pass:原口令 -passout pass:新口令
      ```
  4. 重新执行：
     ```powershell
     .\gradlew --stop
     .\gradlew signPlugin verifyPluginSignature -i
     ```
    - 预期：`build/distributions` 下生成 `*-signed.zip`，随后 `verifyPluginSignature` 正常校验。
- **注意事项**
  - 不要将密钥/证书文件提交到仓库；建议放在本机安全路径，并通过环境变量注入内容。
  - 若在 CI（如 GitHub Actions）中注入多行 PEM，注意换行保真；必要时可在流水线中将 `\n` 还原为真实换行。
  - 如需提升健壮性，可在 `build.gradle.kts` 中实现“环境变量既可为 PEM 内容，也可为本地文件路径，并自动将 `\n` 还原”的处理逻辑（可按需实施）。
- **参考链接**
  - JetBrains 插件签名文档：https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
  - marketplace-zip-signer（CLI 实际执行器）：https://github.com/JetBrains/marketplace-zip-signer