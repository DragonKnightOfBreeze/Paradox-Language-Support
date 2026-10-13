# P11 表达式 PSI 引用的缓存不一致的问题 & `Issue142Test.testVarInEffect` 可能无法通过

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 状态：可能已解决

堆栈跟踪：[stack-trace-10](../assets/stack-trace-10.txt)

表达式 PSI 引用的缓存不一致，有时会导致 `Issue142Test.testVarInEffect` 无法通过。

最终解决方案：
- 对于 `ParadoxExpressionElement.getReferences()` 的实现，不再直接调用 `ReferenceProvidersRegistry.getReferencesFromProviders(element)`，而是改为调用 `ParadoxExpressionManager.getReferences(element)`。
