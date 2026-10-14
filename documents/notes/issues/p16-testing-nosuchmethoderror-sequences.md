# P16 测试过程中的 `java.lang.NoSuchMethodError: 'kotlin.sequences.Sequence kotlin.sequences.SequencesKt.sequenceOf(java.lang.Object)'`

`com.intellij.util.indexing.UnindexedFilesScannerExecutorImpl` L160 可能会抛出异常：[stack-trace-202607132250.txt](../assets/stack-trace-202607132250.txt)

> - IDE 版本：IDEA 2025.2
> - 版本：v3.0.0
> - 日期：2026-07-13
> - 状态：可忽略

分析：
- `kotlin.sequences.Sequence kotlin.sequences.SequencesKt.sequenceOf(java.lang.Object)` 是 kotlin 2.2 才存在的，而我们的IDE最低兼容版本是2025.2，因此使用的 kotlin 版本是 2.1.20……
- 这个问题应当可以忽略。但是因为会输出到测试日志中，又不太能忽略……
- 参见：https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#bundled-stdlib-versions
