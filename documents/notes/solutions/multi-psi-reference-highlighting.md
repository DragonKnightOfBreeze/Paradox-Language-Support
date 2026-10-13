# 对于对应多个 PSI 引用的脚本表达式（如 `religion:catholic`），光标位置的用法高亮可能不正确

> 所属类别：`测试`
> 上级导航：[问题解决与避坑索引 (Solutions Index)](README.md)

对于以下本地化文件片段：

```
l_simp_chinese:
 job_evosymbiont_plural_with_icon: "[calculator_physicist.GetIcon][calculator_biologist.GetIcon][calculator_engineer.GetIcon]$job_evosymbiont_plural$"
```

- 光标放到 `GetIcon` 上时，可能会错误地高亮前面的作用域节点，如 `calculator_physicist`。
- 光标放到 `GetIcon` 的结尾时，可以正确地高亮 `GetIcon`。
- 可能与 `CwtConfigUsagesSearcher` 和 `ParadoxDefinitionUsagesSearcher` 有关。
- 可能需要深入研究 IDE 源码。

分析：
- 似乎第一次点击了某个 PSI 之后会缓存，之后不会再次调用 `referencesSearch`

```
getUsageRanges:66, HighlightUsagesKt (com.intellij.codeInsight.highlighting)
↖com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer.highlightReferencesAndDeclarations
↖↖com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer#computeRanges
↖↖↖com.intellij.codeInsight.daemon.impl.IdentifierHighlightingManagerImpl.getMarkupData # 缓存是在这里发生的 
↖↖↖↖com.intellij.codeInsight.highlighting.BackgroundHighlighter#updateHighlighted

com.intellij.codeInsight.daemon.impl.IdentifierHighlightingManagerImpl.getMarkupData
# result: IdentifierHighlightingResult(occurrences=[IdentifierOccurrence(range=RangeMarker(53,73) 183004, highlightInfoType=HighlightInfoTypeImpl[severity=ELEMENT_UNDER_CARET, key=IDENTIFIER_UNDER_CARET_ATTRIBUTES])], targets=[RangeMarker(53,81) 183005, RangeMarker(53,73) 183004])
# result 的 occurrences 是 53..73，targets 是 53..81 和 53..73，看起来不对（当前 caretOffset 是77）
# 如果在这里清空缓存，则能得到正确的结果

com.intellij.codeInsight.daemon.impl.IdentifierHighlightingManagerImpl.Companion.containsTargetOffset
# 根据这个方法的逻辑，也许需要缩小 result.targets 的文本范围，仅返回节点的而非整个 PSI 的

com.intellij.codeInsight.daemon.impl.IdentifierHighlightingManagerImpl.getMarkupData L134
-com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer.computeRanges L123 在这里创建 IdentifierHighlightingResult
--L101 collectCodeBlockMarkerRanges(myInfos, myTargets) # myTargets 最终来自这里（53..81）
--L106 highlightReferencesAndDeclarations(myInfos, myTargets) # 以及这里（53，73）

com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer.collectCodeBlockMarkerRanges # 还是来自 contextElement.getTextRange() （COMMAND_TOKEN，53..81）
```

结论：
- 这里也会把 PSI 的文本范围加入 targets，并且只要当前偏移在其中，就会使用缓存
- 也许对于用字符串表示的 `ParadoxExpressionElement`，不能缓存 `IdentifierHighlightingManagerImpl.IDENT_MARKUP`
- 或者，考虑修改 result.targets，对于特定类型的 PSI，取最小范围

最终可以考虑的解决方案：
- `com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer.computeRanges` - 处理返回值

最终解决方案：
- 添加 `icu.windea.pls.inject.injectors.fix.IdentifierHighlightingComputerCodeInjector`
