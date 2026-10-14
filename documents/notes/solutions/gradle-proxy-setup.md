# 如何为 gradlew 配置代理

> - 模块：前端（参考文档）

#### 方法1：在 `gradle.properties` 中配置（推荐） 

位置：

- 当前项目专用：位于项目根目录中
- 当前用户的所有 Gradle 项目通用：位于 `.gradle` 目录（默认是 `~/.gradle`）中

HTTP / HTTPS 代理配置：

```properties
# HTTP 代理
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890

# HTTPS 代理（Gradle 下载依赖基本都走 HTTPS）
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890

# 排除不需要走代理的地址（可选）
systemProp.http.nonProxyHosts=localhost|127.0.0.1|*.local
systemProp.https.nonProxyHosts=localhost|127.0.0.1|*.local

# 帐号密码（可选）
systemProp.https.proxyUser=你的用户名
systemProp.https.proxyPassword=你的密码
```

SOCKS5 代理配置：

```properties
systemProp.socksProxyHost=127.0.0.1
systemProp.socksProxyPort=7890
```

#### 方法2：通过命令行参数临时指定（无需修改项目文件）

如果不想改动项目的任何配置文件，可以直接在执行 ./gradlew 时通过 -D 参数传递代理配置：

```bash
# Linux / macOS
./gradlew build \
  -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 \
  -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890

# Windows (PowerShell)
.\gradlew build `
  -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 `
  -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890
```

或者在当前终端临时设置环境变量：

```bash
# Linux / macOS
export GRADLE_OPTS="-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890"

# Windows (PowerShell)
$env:GRADLE_OPTS="-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890"
```
