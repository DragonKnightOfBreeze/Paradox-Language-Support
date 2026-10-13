# P1 索引时报错 `Indexing process should not rely on non-indexed file data.`

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 状态：已解决

堆栈跟踪：[stack-trace-1](../assets/stack-trace-1.txt)

推测：

索引文件时使用到了未索引的数据。即使使用懒加载的数据，仍然可能无法避免。需要持续追踪与优化。
