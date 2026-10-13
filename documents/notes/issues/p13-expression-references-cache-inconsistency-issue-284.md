# P13 表达式 PSI 引用的缓存不一致的问题 & `Issue284Test.testScopeLinkInEffect` 可能无法通过

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 版本：v2.1.8
> - 日期：2026-05-06
> - 状态：可能已解决

堆栈跟踪：[stack-trace-12](../assets/stack-trace-12.txt)

无法稳定复现，需要研究。

推测：
- 在 `SoftConcurrentHashMap` 上实际使用的是 `MutableMap<K, V>.getOrPut`，这是非原子性的。
- 正确的方案是改为使用 `ConcurrentMap<K, V>.getOrPut` 或者 `ConcurrentMap<K, V>.computeIfAbsent`
- ~~需要优化 `SoftMutableMap` 和 `SoftConcurrentHashMap` 的实现，补充缺失的委托方法。~~ 改为实现并应用更通用的 `SoftValue`。
