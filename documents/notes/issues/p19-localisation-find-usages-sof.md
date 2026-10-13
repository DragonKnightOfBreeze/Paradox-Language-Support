# P19 与 `ParadoxLocalisationFindUsagesProvider` 有关的 SOF

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 版本：v3.0.2
> - 日期：2026-09-05
> - 状态：已解决

堆栈跟踪：[stack-trace-p19.txt](../assets/stack-trace-p19.txt)

初步结论：
- 直接将 `FindUsagesProvider` 中的相关方法委托给 `ElementDescriptionUtil.getElementDescription` 可能导致意外的 SOF。考虑显式检查或显式实现。
- 相关的分析笔记：[analysis-p19-localisation-find-usages-sof.md](../../ai-notes/analysis/analysis-p19-localisation-find-usages-sof.md)。

修复方向建议（来自 deepseek）：
1. **最稳妥**：`getType`/`getNodeText` 不再无条件委托；对 EP 未覆盖的元素返回自带默认值（如 `TypePresentationService` 结果或固定字符串）。
2. 或：在 `ParadoxLocalisationElementDescriptionProvider` 中为 key/locale/parameter/command_text/concept_name/text/value 等元素增加兜底非空描述。
3. 或：在 `getType`/`getNodeText` 入口加线程局部递归保护（最小改动，但治标）。
4. 其他三个语言的 provider 同步处理。

后续优化：
- [x] 重构和完善各个 `ElementDescriptionProvider` 和 `FindUsagesProvider`，避免意外的 SOF，优化描述。
