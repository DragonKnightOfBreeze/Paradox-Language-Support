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
