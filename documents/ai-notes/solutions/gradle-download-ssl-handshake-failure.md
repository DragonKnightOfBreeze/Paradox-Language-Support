# Gradle Download 插件下载失败：SSLHandshakeException / PKIX path building failed

> - 模块：构建

- **[背景]** 在 `build.gradle.kts` 中使用 `de.undercouch.download` 插件，通过 HTTPS 从 GitHub 下载 CWT 规则 zip；本机存在代理/AdGuard 或自签根证书。
- **[现象]** 执行 `downloadCwtConfig_*`（例如 `downloadCwtConfig_ck2`）失败：
  ```
  javax.net.ssl.SSLHandshakeException: PKIX path building failed
  sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
  ```
- **[结论（根因）]** Gradle 守护进程所用 JVM 的信任库未信任代理/拦截软件的根证书，TLS 握手校验失败。
- **[排查要点]**
  - 确认 Gradle 实际使用的 JVM：
    ```powershell
    .\gradlew -version
    ```
  - IDEA 的 JBR 与 Gradle Daemon 可能不同，证书需导入“Gradle 使用的 JVM”。
- **[解决步骤 A·临时兜底]** 仅限受限网络/排障时使用（只影响下载插件的请求）：
  ```powershell
  .\gradlew downloadCwtConfig_ck2 -Ppls.cwt.acceptAnyCertificate=true --stacktrace
  .\gradlew clean buildPlugin -Ppls.cwt.acceptAnyCertificate=true
  ```
- **[解决步骤 B·推荐做法]** 给 Gradle JVM 配置可信根证书：
  1) 找到 `JVM:` 路径（来自 `gradlew -version`）。
  2) 导入根证书到该 JVM 的默认 trustStore：
     ```powershell
     C:\path\to\jdk\bin\keytool.exe -importcert -alias adguard ^
       -file D:\Documents\杂项\证书\cert.cer ^
       -keystore "C:\path\to\jdk\lib\security\cacerts" -storepass changeit
     ```
     或者使用自定义 trustStore，并在构建时指定：
     ```powershell
     .\gradlew downloadCwtConfig_ck2 ^
       -Djavax.net.ssl.trustStore="D:\Documents\杂项\证书\cacerts_adguard" ^
       -Djavax.net.ssl.trustStorePassword=changeit
     ```
  3) 重启 Gradle 守护进程并重试：
     ```powershell
     .\gradlew --stop
     .\gradlew downloadCwtConfig_ck2 --stacktrace
     .\gradlew clean buildPlugin
     ```
- **[注意事项]**
  - 不建议长期开启 `-Ppls.cwt.acceptAnyCertificate=true`；应尽快改为正确的 trustStore 配置。
  - 若启用系统代理/AdGuard，确保其根证书被“Gradle 使用的 JVM”信任。
  - PowerShell 下执行 Gradle 推荐前缀 `.\\gradlew`，路径含空格需加引号。
- **[参考链接]**
  - de.undercouch.download 插件：https://github.com/michel-kraemer/gradle-download-task
