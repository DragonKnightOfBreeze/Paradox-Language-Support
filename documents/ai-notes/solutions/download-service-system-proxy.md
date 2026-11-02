# 本地下载测试受系统代理影响

> - 模块：测试 > 网络下载

## 现象

- `DownloadServiceTest` 使用本地 `HttpServer`（`127.0.0.1`）验证下载、哈希和失败清理。
- 在配置了系统代理（例如 Clash、公司代理或安全软件）的环境中，本地请求可能被代理接管，导致：
  - 连接失败或超时；
  - 本地服务器收到不到请求；
  - 测试结果与代码逻辑无关。
- 外部工具下载测试也可能受代理、证书和网络策略影响。

## 根因

- `DownloadService` 默认通过 `ProxyService.resolve(uri)` 解析代理，优先级包括：
  - IDE 静态代理；
  - 环境变量（`HTTPS_PROXY` / `HTTP_PROXY` / `ALL_PROXY` / `NO_PROXY`）；
  - JVM 默认 `ProxySelector`；
  - JVM 系统属性。
- 本地测试不应假设系统代理一定存在，也不应假设它一定不存在。
- 部分代理软件会替换 TLS 证书；如果 Gradle/JVM 不信任其根证书，还会出现 `PKIX path building failed`，参见 `gradle-download-ssl-handshake-failure.md`。

## 解决方式

- 测试层面：
  - `DownloadServiceTest` 在建立本地 URI 后检测 `ProxyService.resolve(uri)`；
  - 如果检测到系统代理，使用 JUnit `Assume` 跳过本地下载测试，而不是误报为代码失败；
  - 外部工具相关测试继续使用 `ChronicleAssume.includeExternalTools()` 控制。
- 生产层面：
  - `DownloadService` 保持代理自动解析，并支持通过显式配置覆盖；
  - 代理问题应通过环境变量、IDE 代理设置或 JVM trustStore 处理，而不是在下载逻辑中硬编码。
- 测试示例环境：
  - 关闭系统代理后运行 `DownloadServiceTest`；
  - 或通过测试配置显式提供不代理本地地址的环境。

## 注意

- 不要把“本地环境一定有系统代理”作为测试前提。
- 也不要把“本地环境一定没有系统代理”作为测试前提。
- 对外部工具下载的失败应先区分网络/代理/证书问题与工具本身问题。
