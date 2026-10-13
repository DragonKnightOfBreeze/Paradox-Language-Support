# P6 来自的 `ParadoxTextBasedTargetSearcher` 和 fastutil 的索引越界异常

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 状态：已解决

堆栈跟踪：[stack-trace-5](../assets/stack-trace-5.txt)

结论：

需要使用并发集合，避免在 IntelliJ 并发检索过程中发生非线程安全的 rehash 异常。
