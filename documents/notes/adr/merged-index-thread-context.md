# 为合并索引引入线程本地的上下文

> 所属模块：`后端 > 索引`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.3
> - 日期：2026-09-15+
> - 状态：TODO

说明：
- 对于合并索引（`ParadoxMergedIndex`），除了已引入的普通上下文（`ParadoxMergedIndexContext`）之外，另外引入线程本地的上下文（`ParadoxMergedIndexThreadContext`）。
- 通过直接在线程本地的上下文中保存状态、PSI 堆栈以及各种缓存信息的堆栈，可以避免不必要的对常规缓存的访问（configContext/configs/definitionInfo/等等），以及不必要的需要回溯 PSI 树的计算（memberPath/rootKeys）。
- 需要重构合并索引的实现逻辑，以及调整部分缓存信息的获取逻辑（优先尝试从合并索引的线程本地的上下文获取，其次再尝试从 PSI/规则上下文/规则分组 上的缓存中获取）。
- 需要注意获取缓存数据时，传入的 PsiElement 可能并非构建合并索引时，当前正在遍历的那个 PsiElement，因此需要在上下文对象中另外维护一个 PSI 堆栈。
- 核心思路是“单趟遍历”与“单次获取”。不要在具体的语义解析逻辑中，重新计算某些缓存数据，或者再次回溯 PSI 树以进行某些计算。
- 这项优化方案预计可以显著降低构建合并索引时的耗时（~33%），但也会显著增加代码复杂度，需要经过充分的设计、调试和测试。

相关代码位置：
- [x] `icu.windea.pls.lang.index.ParadoxMergedIndex`
- [x] `icu.windea.pls.lang.index.ParadoxMergedIndexService`
- [x] `icu.windea.pls.lang.index.ParadoxMergedIndexContext`
- [ ] `icu.windea.pls.lang.index.ParadoxMergedIndexThreadContext`
