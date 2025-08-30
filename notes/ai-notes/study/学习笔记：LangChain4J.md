# LangChain4J 学习笔记

## 概述

LangChain4J 是 Java/Kotlin 的 AI 应用框架，提供链式组件构建 AI 应用。支持本地和云模型。

**官方参考链接**：
- [GitHub 仓库](https://github.com/langchain4j/langchain4j)
- [示例项目](https://github.com/langchain4j/langchain4j-examples)
- [API 文档](https://javadoc.io/doc/dev.langchain4j/langchain4j)

**其他参考链接**：
- [Spring Boot 集成指南](https://github.com/langchain4j/langchain4j-spring-boot-starter)
- [高级检索增强生成](https://github.com/langchain4j/langchain4j/blob/main/langchain4j/src/main/java/dev/langchain4j/chain/ConversationalRetrievalChain.java)

## 核心组件

### 模型抽象层
```java
ChatLanguageModel model = OpenAiChatModel.builder()
    .apiKey("demo")
    .modelName("gpt-4-turbo")
    .build();
```

### 记忆管理
```kotlin
val memory = MessageWindowChatMemory.builder()
    .maxMessages(10)
    .build()
```

### 工具调用
```java
interface Calculator {
    @Tool("计算两个数字之和")
    double add(double a, double b);
}
```

## 最佳实践
1. **链式组合**：复用预定义链
2. **本地模型集成**：通过 Ollama
3. **流式响应**：提升用户体验

## 与 Spring Boot 集成

- **依赖（Gradle Kotlin DSL）**：
```kotlin
dependencies {
    implementation("dev.langchain4j:langchain4j-spring-boot-starter:<version>")
    // 可选：OpenAI、Ollama、Azure OpenAI 等模型实现
    implementation("dev.langchain4j:langchain4j-open-ai:<version>")
}
```

- **application.yml（示例：OpenAI 与代理）**：
```yaml
langchain4j:
  open-ai:
    api-key: ${OPENAI_API_KEY:}
    model-name: gpt-4o-mini
    timeout: 30s
    base-url: https://api.openai.com/v1
    # 代理（如需）
    proxy:
      host: 127.0.0.1
      port: 7890
```

- **注入与使用**：
```kotlin
import dev.langchain4j.model.chat.ChatLanguageModel
import org.springframework.stereotype.Service

@Service
class ChatService(private val model: ChatLanguageModel) {
    fun ask(userInput: String): String = model.generate(userInput)
}
```


## 与 Ktor 集成

- 目标：在 Ktor Server 中调用 LangChain4J 的 `ChatLanguageModel`，提供一个最小可运行的 HTTP 接口。

### 依赖（Gradle Kotlin DSL）
```kotlin
plugins {
  application
  kotlin("jvm") version "<kotlin-version>"
}

repositories { mavenCentral() }

dependencies {
  // Ktor Server
  implementation("io.ktor:ktor-server-core:<version>")
  implementation("io.ktor:ktor-server-netty:<version>")
  runtimeOnly("ch.qos.logback:logback-classic:1.5.6")

  // LangChain4J（以 OpenAI 为例，可替换为 Ollama 等实现）
  implementation("dev.langchain4j:langchain4j-open-ai:<version>")
}

application { mainClass.set("com.example.ApplicationKt") }
```

### 最小可行 Demo（Kotlin）
```kotlin
package com.example

import dev.langchain4j.model.openai.OpenAiChatModel
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

object Ai {
  val model = OpenAiChatModel.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .modelName("gpt-4o-mini")
    .build()
}

fun main() {
  embeddedServer(Netty, port = 8080) {
    routing {
      // GET http://localhost:8080/chat?q=用一句话解释Ktor
      get("/chat") {
        val q = call.request.queryParameters["q"].orEmpty().ifBlank { "你好，请简单自我介绍。" }
        val ans = Ai.model.generate(q)
        call.respondText(ans)
      }
    }
  }.start(wait = true)
}
```

### 运行

- Windows PowerShell：
  - 设置环境变量：`$env:OPENAI_API_KEY = "<your-key>"`
  - 启动：`.\gradlew run`
- 浏览器访问：`http://localhost:8080/chat?q=Ktor%20是什么`。

说明：若使用本地模型（如 Ollama），请改用 `langchain4j-ollama` 依赖，并按其 base-url 与 model-name 初始化对应的模型构造器。

## 与 Ollama 本地模型集成

- **依赖**：
```kotlin
dependencies { implementation("dev.langchain4j:langchain4j-ollama:<version>") }
```
- **application.yml**：
```yaml
langchain4j:
  ollama:
    base-url: http://localhost:11434
    model-name: llama3
```
- **说明**：确保本地已安装并启动 Ollama，且模型已 pull 完成。

## 流式响应（Streaming）

```kotlin
import dev.langchain4j.model.chat.StreamingChatLanguageModel

fun streamDemo(model: StreamingChatLanguageModel) {
    val sb = StringBuilder()
    model.generate("用 3 点解释 JVM 内存结构") { token ->
        print(token)
        sb.append(token)
    }
    println("\nFull: ${sb}")
}
```

## 简易 RAG（检索增强生成）

```kotlin
// 伪代码示例，演示链式拼装思路
// 1) 构建/加载向量索引（EmbeddingStore）
// 2) 根据问题检索相似片段（Retriever）
// 3) 将检索上下文 + 问题一并交给 Chat 模型

/*
val embeddingModel = OpenAiEmbeddingModel(...)
val store = InMemoryEmbeddingStore<TextSegment>()
indexDocuments(store, embeddingModel, documents)
val retriever = EmbeddingStoreRetriever.from(store, embeddingModel)

val chain = ConversationalRetrievalChain.builder()
    .chatLanguageModel(chatModel)
    .retriever(retriever)
    .build()

val answer = chain.execute("根据给定文档解释 Gradle 配置缓存的限制")
*/
```

## 常见问题与排错

- **认证失败**：检查 API Key 是否正确注入；在容器/CI 中留意环境变量与换行/编码。
- **超时/连接问题**：设置超时与重试；必要时配置代理。
- **长上下文报错**：裁剪历史、启用摘要记忆（MessageWindowChatMemory）、或提升模型的上下文长度。
- **非确定性**：降低 `temperature`；必要时固定随机种子（模型支持有限）。
- **RAG 召回差**：优化分段策略、调整召回数量、选择合适的向量模型与度量方式。

## 可扩展内容

- **模型与供应商**：OpenAI/Azure/Ollama/本地模型切换；Provider 抽象与降级/回退策略。
- **流式与背压**：`StreamingChatLanguageModel` 在 Ktor/SSE/WebSocket 集成与背压处理。
- **记忆与多轮**：`MessageWindowChatMemory`/摘要记忆，持久化对话状态（DB/缓存）。
- **RAG 体系**：分段与嵌入策略、向量库（pgvector/Elasticsearch/Milvus）集成与评测基线。
- **工具与 Agent**：Tool/Function-Calling、外部服务编排、超时/重试/幂等性。
- **观测与测试**：结构化日志、指标与追踪；回放测试与确定性用例。
- **部署与安全**：配置分环境注入、API Key 管理、限流与熔断。
- **与 Ktor/Spring 集成**：统一异常映射、依赖注入配置与启动生命周期管理。

## 参考链接

- [LangChain4J](https://github.com/langchain4j/langchain4j)
- [Spring Boot Starter](https://github.com/langchain4j/langchain4j-spring-boot-starter)
- [Ollama](https://ollama.ai/)
- [Conversational Retrieval Chain 示例](https://github.com/langchain4j/langchain4j/blob/main/langchain4j/src/main/java/dev/langchain4j/chain/ConversationalRetrievalChain.java)
