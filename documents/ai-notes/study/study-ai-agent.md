# 学习笔记：AI Agent（AI 代理）

> 目标：强调与具体语言无关的通用概念与范式，聚焦任务分解、工具调用与闭环评测；示例以伪代码或 LangChain4J + Kotlin 作为参考。

---

## 核心概念

- **Agent = 目标导向 + 决策 + 工具使用**：围绕目标（Goal）在环境中采取行动（Action），根据反馈（Observation）迭代直到完成。
- **关键组成**：
  - 记忆（Memory）：短期（对话/上下文窗口）、长期（向量库/数据库）、工作记忆（临时状态）。
  - 工具（Tools）：检索、代码执行、外部 API、文件/系统操作等。
  - 规划（Planning）：ReAct、Plan-and-Execute、树/图搜索（ToT、MCTS）。
  - 监督（Control）：约束与安全策略、成本与延迟预算、回退/降级路径。
- **闭环（LoOP）**：Think -> Act -> Observe -> Reflect -> Next.

---

## 常见范式与架构

- **ReAct**：将推理（Reasoning）与行动（Acting）交替进行，显式记录思考痕迹与工具调用。
- **Plan-and-Execute**：先形成计划（分解子任务），再逐步执行，适合中长流程任务。
- **多代理（Multi-Agent）**：角色分工（Planner/Executor/Reviewer），或按能力协作（检索/代码/评测）。
- **函数/工具调用（Tool-Use）**：以 JSON Schema 描述工具签名，模型生成结构化参数，系统负责执行与回传。

---

## 基本循环（伪代码）

```pseudo
state = { goal, memory, tools, budget }
loop until done or budget_exhausted:
  thought = LLM.think(state)
  if thought.needs_tool:
    tool_call = parse_tool(thought)
    result = execute(tool_call)
    state.memory.append(observation(result))
  else:
    state.memory.append(thought)
  if success_criteria_met(state): break
return summarize(state)
```

---

## LangChain4J + Kotlin（示意）

```kotlin
// 伪代码，仅示意接口形态
interface Tool { val name: String; fun invoke(args: Map<String, Any?>): String }
class SearchTool: Tool { /* ... */ }
class CalcTool: Tool { /* ... */ }

val llm = OpenAiChatModel.builder().build()
val tools = listOf(SearchTool(), CalcTool())

fun agent(goal: String): String {
  val memory = mutableListOf<String>()
  repeat(8) {
    val prompt = buildPrompt(goal, memory, tools)
    val resp = llm.generate(prompt)
    val call = tryParseToolCall(resp)
    if (call != null) {
      val tool = tools.firstOrNull { it.name == call.name }
      val out = tool?.invoke(call.args).orEmpty()
      memory += "OBSERVE: ${out.take(500)}"
    } else {
      memory += "THINK: ${resp.take(500)}"
      if (isDone(resp)) return finalize(goal, memory)
    }
  }
  return finalize(goal, memory)
}
```

---

## 评测与保障

- **目标达成率**：任务成功/失败、重试次数、平均步骤数。
- **安全与合规**：工具白名单、参数校验、权限分级、脱敏与越权拦截。
- **成本与延迟**：思考步数上限、流式与背压、缓存与复用。
- **回放与分析**：思考/工具调用/观测链路全量日志，失败样本归因与修复闭环。

---

## 与本项目的关联（Paradox Language Support）

- **规则解释与修复建议**：对检查结果生成解释/修复步骤，并提供结构化的 QuickFix 建议。
- **知识检索工具**：将 CWT 与脚本文档作为检索工具，Agent 选择并引用来源，减少幻觉。
- **IDE 工具桥接**：以工具形式暴露“跳转/重命名/模板应用”等 IDE 能力，由 Agent 编排。
- **离线/可控**：优先使用本地/企业模型与工具，明确成本与权限边界。

---

## 可扩展内容

- **计划质量**：计划评审（LLM-as-Judge）、自一致性多路采样与投票。
- **长期记忆**：人物卡/项目卡与会话检索、记忆压缩与衰减。
- **层级代理**：高层目标拆解，底层技能执行；失败回滚与重规划。
- **观测性与指标**：标准化日志 schema、Span/Trace 关联、红队与越权测试集。

---

## 参考链接

- **ReAct 论文**：https://arxiv.org/abs/2210.03629
- **Toolformer**：https://arxiv.org/abs/2302.04761
- **LangChain Agents**：https://python.langchain.com/docs/modules/agents/
- **AutoGen**：https://github.com/microsoft/autogen
