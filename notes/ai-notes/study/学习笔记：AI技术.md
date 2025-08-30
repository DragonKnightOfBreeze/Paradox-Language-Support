# AI 技术学习笔记

## 概述

人工智能技术基础，涵盖主流模型、应用场景和开发实践。面向 Java/Kotlin 开发者。

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

## 实用提示模板

```text
[System]
You are a senior Kotlin/Java engineer. Answer concisely with runnable examples when possible.

[User]
Task: ${task}
Constraints:
- Output language: Chinese (简体中文)
- Provide Kotlin examples if code is needed
```

小贴士：明确角色、任务、约束与输出格式；必要时加入 few-shot 示例与拒答边界。

## 评测与验收（轻量流程）

- **数据集**：准备 CSV/YAML（columns: input, expected）。
- **指标**：
  - 结构化：exact match / 字段级匹配
  - 文本：BLEU/ROUGE/semantic similarity（嵌入余弦相似度）
  - LLM-as-judge：慎用，加入对齐与偏差控制
- **流程**：
  1. 批量推理 → 记录输入、输出、延迟
  2. 计算指标 → 生成报告（通过率、均值、p95）
  3. 回归基线 → 比对历史版本

伪代码：
```kotlin
data class Case(val input: String, val expected: String)

fun evaluate(cases: List<Case>, infer: (String) -> String): Double {
  var pass = 0
  for (c in cases) {
    val out = infer(c.input)
    if (out.trim() == c.expected.trim()) pass++
  }
  return pass.toDouble() / cases.size
}
```

## RAG 基础与向量检索

- **流程**：切分文档 → 嵌入（embedding）→ 存储 → 检索 Top-K → 重写查询（可选）→ 重排序（可选）→ 组装提示 → 生成。
- **切分**：按语义/段落，保留少量重叠（overlap）以减碎片化。
- **嵌入模型**：开源（bge/e5/gte）或云（text-embedding-3-large/small）。
- **向量库**：本地（FAISS/Lucene-hnswlib）或云（pgvector/Elastic/OpenSearch）。

简化余弦相似度示例：
```kotlin
data class Doc(val id: String, val text: String, val vec: DoubleArray)

fun cosine(a: DoubleArray, b: DoubleArray): Double {
  var dot = 0.0; var na = 0.0; var nb = 0.0
  for (i in a.indices) { dot += a[i]*b[i]; na += a[i]*a[i]; nb += b[i]*b[i] }
  return dot / (kotlin.math.sqrt(na) * kotlin.math.sqrt(nb) + 1e-9)
}

fun retrieve(query: String, docs: List<Doc>, embed: (String)->DoubleArray, k: Int = 5): List<Doc> {
  val qv = embed(query)
  return docs.asSequence()
    .map { it to cosine(qv, it.vec) }
    .sortedByDescending { it.second }
    .take(k).map { it.first }.toList()
}
```

提示拼装：
```text
问题：${query}
参考材料（可能不完整或含噪）：
1) ${doc1}
2) ${doc2}
…
请仅依据参考材料作答，无法回答时说明“资料不足”。
```

## Kotlin HTTP 调用示例（OkHttp）

Gradle 依赖（Kotlin DSL）：
```kotlin
dependencies { implementation("com.squareup.okhttp3:okhttp:4.12.0") }
```

### 调用 OpenAI Chat Completions
```kotlin
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

val client = OkHttpClient()
val media = "application/json; charset=utf-8".toMediaType()
val body = """
{
  "model": "gpt-4o-mini",
  "messages": [{"role":"user","content":"用 1 句话解释RAG"}]
}
""".trimIndent().toRequestBody(media)

val req = Request.Builder()
  .url("https://api.openai.com/v1/chat/completions")
  .addHeader("Authorization", "Bearer ${'$'}{System.getenv("OPENAI_API_KEY")}")
  .post(body)
  .build()

client.newCall(req).execute().use { resp ->
  println(resp.body!!.string())
}
```

### 流式响应（SSE-like）
```kotlin
val streamBody = """
{ "model": "gpt-4o-mini", "stream": true,
  "messages": [{"role":"user","content":"逐步解释Kotlin协程"}]
}
""".trimIndent().toRequestBody(media)

val streamReq = req.newBuilder().post(streamBody).build()
client.newCall(streamReq).execute().use { resp ->
  val source = resp.body!!.source()
  while (!source.exhausted()) {
    val line = source.readUtf8Line() ?: break
    if (line.startsWith("data: ")) {
      val payload = line.removePrefix("data: ")
      if (payload == "[DONE]") break
      println(payload)
    }
  }
}
```

### 调用本地 Ollama（可选）
```kotlin
val ollamaReq = Request.Builder()
  .url("http://localhost:11434/api/generate")
  .post("""{"model":"qwen2.5:3b","prompt":"写一个Kotlin扩展函数示例"}"""
    .toRequestBody(media))
  .build()
client.newCall(ollamaReq).execute().use { println(it.body!!.string()) }
```

## 安全与合规

- **机密管理**：使用环境变量/Secret 管理；切勿写入日志与仓库。
- **最小化数据**：仅发送必要字段；对 PII 做脱敏/加密。
- **拒答策略**：对违法/高风险请求显式拒绝并提供安全替代方案。
- **资源控制**：并发与速率限制；设定超时与重试指数回退。

## 常见问题与排错

- **429/速率限制**：退避重试（指数回退），请求合并与缓存。
- **SSL/证书问题（Windows）**：更新根证书或禁用拦截代理；校验时钟同步。
- **流式打印乱码**：确认以行为单位处理 `data:`，并按 UTF-8 解码。
- **上下文过长**：压缩检索结果、分段对话、模型切换至 128k+ 上下文。

## 参考链接

- [AI 技术全景图](https://www.deeplearning.ai/resources/ai-glossary/)
- [HuggingFace Models](https://huggingface.co/models)
- [OpenAI API](https://platform.openai.com/docs)
- [LangChain4j](https://github.com/langchain4j/langchain4j)
- [Ollama](https://github.com/ollama/ollama)
- [AI 模型微调指南](https://huggingface.co/docs/transformers/training)
- [生产环境部署最佳实践](https://mlflow.org/docs/latest/models.html#deployment)
