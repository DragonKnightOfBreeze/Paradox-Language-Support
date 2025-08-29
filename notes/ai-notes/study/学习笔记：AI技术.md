# AI 技术学习笔记

## 概述

人工智能技术基础，涵盖主流模型、应用场景和开发实践。面向 Java/Kotlin 开发者。

**官方参考链接**：
- [AI 技术全景图](https://www.deeplearning.ai/resources/ai-glossary/)
- [Hugging Face 模型库](https://huggingface.co/models)
- [OpenAI API 文档](https://platform.openai.com/docs/introduction)

**其他参考链接**：
- [AI 模型微调指南](https://huggingface.co/docs/transformers/training)
- [生产环境部署最佳实践](https://mlflow.org/docs/latest/models.html#deployment)

## 核心概念

### 模型类型
- **LLM**：文本生成、问答系统
- **Embedding 模型**：文本向量化
- **多模态模型**：图文理解

### 开发范式
```kotlin
// 典型 AI 应用架构
val aiService = AIService(
    embeddingModel = "text-embedding-ada-002",
    llmModel = "gpt-4-turbo"
)
val result = aiService.generate("解释量子计算")
```

## 最佳实践
1. **提示工程**：清晰明确的指令
2. **温度控制**：调整输出随机性
3. **内容审核**：防止有害输出
