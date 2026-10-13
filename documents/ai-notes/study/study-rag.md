# 学习笔记：RAG（检索增强生成）

> 目标：聚焦与具体语言无关的通用概念与范式，提供可落地的工程要点与评测思路；代码示例以伪代码或 LangChain4J + Kotlin 为辅。

---

## 核心概念与典型架构

- **动机**：降低幻觉、提升事实性与可追溯性；以外部知识增强模型能力。
- **基本流程**：
  - Query 预处理（归一化/扩展）
  - 检索（向量/关键词/混合）
  - 重排（Cross-Encoder）
  - 上下文组装（拼接/摘要/去重）
  - 生成（带引用/防幻觉指令）
  - 反馈与评测（日志/指标）
- **能力边界**：RAG 非“万能问答”，对时效性、领域性与源质量高度敏感。

---

## 分块与向量化

- **分块策略**：固定窗口、递归分割、按结构（标题/段落/代码块），带重叠以保留上下文。
- **元数据**：来源、段落/章节标识、时间戳、语言、标签；便于过滤与引用。
- **嵌入模型**：BGE、E5、text-embedding-3 等；注意语言覆盖与维度；存储成本与延迟权衡。
- **向量库**：pgvector、Elasticsearch（dense-vector）、Milvus、Qdrant；度量（cosine/dot/ip）。

---

## 检索与重排

- **检索**：向量检索、BM25、混合检索（加权或并联融合）。
- **重排**：Cross-Encoder 进行 rerank，减少语义噪音；注意延迟与成本。
- **查询扩展**：同义词扩展、生成式扩展（GQE），或多查询（multi-query）策略。

---

## 上下文组装与防幻觉

- **拼接策略**：按主题归并、去重、上限截断；优先高置信段落。
- **防幻觉提示**：要求“仅基于给定上下文作答，无法回答时明确说明”；强制引用来源。
- **可追溯性**：为每段输出附带 source 链接/定位信息。

---

## 评测与观测

- **指标**：Faithfulness、Answer Relevance、Context Precision/Recall；任务级成功率与用户满意度。
- **数据集**：构造问答对 + 金标准；覆盖常见/困难/异常样例。
- **自动化**：RAGAS 等框架；离线评测 + 在线 A/B；日志采样回放。

---

## 伪代码（最小 RAG 流水线）

```pseudo
retrieve(query):
  q = normalize(query)
  V = vector_search(q, k=50)
  B = bm25_search(q, k=20)
  C = hybrid_merge(V, B)
  R = cross_encoder_rerank(q, C)
  ctx = assemble(R.top(k=6), max_tokens=2048)
  return ctx

answer(query):
  ctx = retrieve(query)
  prompt = render({query, ctx, guidelines: no_hallucination, cite: true})
  return LLM.generate(prompt)
```

---

## LangChain4J + Kotlin（示意）

```kotlin
// 伪代码：省略具体依赖与实现
val store = VectorStore.pgvector(/* ... */)
val embedder = EmbeddingModel(/* ... */)
val retriever = store.asRetriever(embedder, k = 6)
val llm = OpenAiChatModel.builder().build()

fun rag(query: String): String {
  val ctx = retriever.retrieve(query) // List<Document> 含 metadata.source
  val prompt = buildString {
    appendLine("仅基于上下文回答；无法回答请直接说明。不允许编造。")
    ctx.forEachIndexed { i, d -> appendLine("[${i+1}] ${d.text}\n(${d.metadata["source"]})") }
    appendLine("问题：$query")
  }
  return llm.generate(prompt)
}
```

---

## 与本项目的关联（Paradox Language Support）

- **知识库**：以 CWT 规则与 Paradox 脚本文档为知识源，面向“规则解释/示例/最佳实践”的检索问答。
- **IDE 协助**：在编辑器侧提供“选中文段 -> 解释/引用来源”的面板，输出结构化引用。
- **评测闭环**：采集用户反馈更新数据集，迭代分块策略与检索权重。

---

## 可扩展内容

- **混合策略**：多向量表示（标题/正文/代码）、多路召回与层级融合。
- **结构化知识**：结合知识图谱或规则引擎，处理强约束问题。
- **缓存与性价比**：Embedding 缓存、热点文档优先、检索/重排降级路径。
- **安全与权限**：私有知识权限控制、来源可信度与水印检测。

---

## 参考链接

- **RAG Best Practices（OpenAI）**：https://platform.openai.com/docs/guides/retrieval
- **RAGAS**：https://github.com/explodinggradients/ragas
- **pgvector**：https://github.com/pgvector/pgvector
- **Qdrant**：https://qdrant.tech/
- **Milvus**：https://milvus.io/
- **Elasticsearch Vector**：https://www.elastic.co/guide/en/elasticsearch/reference/current/dense-vector.html
