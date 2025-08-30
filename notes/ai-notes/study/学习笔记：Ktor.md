# Ktor 学习笔记

## 概述

- Ktor 是基于 Kotlin 协程的异步 Web 框架，提供 Server 与 Client 两端能力，适合构建高性能 API、WebSocket、流式通信等。
- 支持灵活的插件体系（ContentNegotiation、Authentication、CORS、WebSockets 等），与 Kotlinx Serialization、Jackson 等序列化框架良好集成。

**参考链接**
- 文档：https://ktor.io/
- 示例：https://github.com/ktorio/ktor-samples
- API： https://api.ktor.io/

## 快速开始（Server）

- Gradle（Kotlin DSL）：
```kotlin
plugins {
  application
  kotlin("jvm") version "<kotlin-version>"
}

repositories { mavenCentral() }

dependencies {
  implementation("io.ktor:ktor-server-core:<version>")
  implementation("io.ktor:ktor-server-netty:<version>")
  implementation("io.ktor:ktor-server-content-negotiation:<version>")
  implementation("io.ktor:ktor-serialization-kotlinx-json:<version>")
  implementation("io.ktor:ktor-server-call-logging:<version>")
  runtimeOnly("ch.qos.logback:logback-classic:1.5.6")
}

application { mainClass.set("com.example.ApplicationKt") }
```

- 最小可行 Server（Kotlin）：
```kotlin
package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Message(val text: String)

fun main() {
  embeddedServer(Netty, port = 8080) {
    install(CallLogging)
    install(ContentNegotiation) {
      json(Json { ignoreUnknownKeys = true; prettyPrint = false })
    }
    routing {
      get("/ping") { call.respondText("pong") }
      post("/echo") {
        val body = call.receive<Message>()
        call.respond(Message("echo: ${body.text}"))
      }
    }
  }.start(wait = true)
}
```

- 运行（Windows PowerShell）：
  - `.\gradlew run`
  - 测试：`curl http://localhost:8080/ping`
  - 测试：`curl -X POST http://localhost:8080/echo -H "Content-Type: application/json" -d '{"text":"hello"}'`

## Client（HTTP 客户端）

- 依赖：
```kotlin
dependencies {
  implementation("io.ktor:ktor-client-core:<version>")
  implementation("io.ktor:ktor-client-cio:<version>")
  implementation("io.ktor:ktor-client-content-negotiation:<version>")
  implementation("io.ktor:ktor-serialization-kotlinx-json:<version>")
}
```

- 示例（GET + JSON）：
```kotlin
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Repo(val id: Long, val name: String)

suspend fun fetch(): List<Repo> {
  val client = HttpClient(CIO) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
  }
  return client.get("https://api.github.com/orgs/jetbrains/repos").body()
}
```

## 常用插件（Server）

- **ContentNegotiation**：序列化/反序列化（Kotlinx/Jackson）。
- **CallLogging**：日志记录。
- **CORS**：跨域访问控制。
- **Compression**：Gzip/Brotli 压缩。

```kotlin
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.compression.*

install(CORS) {
  anyHost() // 生产建议精确到域名
  allowHeader("Authorization")
}
install(Compression) { gzip() }
```

## 认证与授权（Bearer 简例）

- 依赖：`io.ktor:ktor-server-auth:<version>`
```kotlin
import io.ktor.server.auth.*

install(Authentication) {
  bearer("auth-bearer") {
    authenticate { tokenCredential ->
      val expected = System.getenv("API_TOKEN").orEmpty()
      if (tokenCredential.token == expected && expected.isNotBlank()) UserIdPrincipal("user") else null
    }
  }
}

routing {
  authenticate("auth-bearer") {
    get("/secure") { call.respondText("ok") }
  }
}
```

## WebSocket（回声示例）

- 依赖：`io.ktor:ktor-server-websockets:<version>`
```kotlin
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

install(WebSockets)

routing {
  webSocket("/ws") {
    incoming.consumeEach { frame ->
      if (frame is Frame.Text) send("echo: ${frame.readText()}")
    }
  }
}
```

## 测试（Server）

- 依赖：`io.ktor:ktor-server-test-host:<version>`、`org.jetbrains.kotlin:kotlin-test`（或 JUnit/Kotest）。
```kotlin
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTest {
  @Test
  fun ping() = testApplication {
    application { /* 安装你的 module() */ }
    val res = client.get("/ping")
    assertEquals(HttpStatusCode.OK, res.status)
    assertEquals("pong", res.bodyAsText())
  }
}
```

## 部署与打包

- 本地运行：`.\gradlew run`
- 生成可执行分发（installDist）：`.\gradlew installDist`，输出在 `build/install/<app>/bin/`。
- Fat Jar（可选，Shadow 插件）：
```kotlin
plugins { id("com.github.johnrengelman.shadow") version "8.1.1" }
// 运行： .\gradlew shadowJar ；产物： build/libs/*-all.jar
```

## 常见问题与排错

- **端口占用**：修改端口或释放 8080。
- **序列化错误**：检查 Content-Type 与 Kotlinx/Jackson 的配置（`ignoreUnknownKeys`）。
- **CORS 失败**：对齐前端源域名、方法与头。
- **Windows 运行脚本**：使用 `.\gradlew` 前缀（PowerShell）。

## 参考链接

- [Ktor 文档](https://ktor.io/docs/)
- [Ktor Client 指南](https://ktor.io/docs/client-create-new-application.html)
- [Content Negotiation](https://ktor.io/docs/serialization.html)
- [WebSockets](https://ktor.io/docs/servers-features-websockets.html)
