# P10 表达式 PSI 引用的缓存不一致的问题

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 状态：应当已解决

堆栈跟踪：[stack-trace-8](../assets/stack-trace-8.txt)

最终解决方案：
- 对于 `ParadoxExpressionElement.getReferences()` 的实现，不再直接调用 `ReferenceProvidersRegistry.getReferencesFromProviders(element)`，而是改为调用 `ParadoxExpressionManager.getReferences(element)`。
