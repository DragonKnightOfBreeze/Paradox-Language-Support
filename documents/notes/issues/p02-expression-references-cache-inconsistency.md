# P2 `ParadoxExpressionManager.getExpressionReferences` 中使用的缓存可能不一致的问题

> - 状态：不再复现

堆栈跟踪：[stack-trace-2](../assets/stack-trace-2.txt)

推测：

不好复现，可能是因为正在索引，不同线程中得到的来自索引的数据不同，因而最终解析得到的引用（`PsiReference`）列表不一致。
