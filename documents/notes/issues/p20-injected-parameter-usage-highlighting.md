# P20 对于适用代码注入的参数值，光标位置的用法高亮可以不正确

> - IDE 版本：IDEA 2025.2
> - 版本：v3.0.3
> - 日期：2026-09-16
> - 状态：待验证

对于以下脚本文件片段：

```
# stellaris, events/astral_planes_events.txt

r_celestial_tear = {
	inline_script = {
		script = astral_rift/unlock_from_pool_if_relic_not_owned
    RELIC = r_celestial_tear
	}
}
r_ever_spinning_top = {
	inline_script = {
		script = astral_rift/unlock_from_pool_if_relic_not_owned
		RELIC = r_ever_spinning_top
	}
}
```

- 光标放到 `r_ever_spinning_top` 上时，除非位于其末尾，否则可能会错误地高亮之前的参数值，如 `r_celestial_tear`。
- 通过调试发现，`com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer.computeRanges` 返回的 `IdentifierHighlightingResult` 的 `myTargets` 的值似乎是错误的。
- 通过调试发现，`myCaretOffset` 为 `10` 时，`myTargets` 可能是 `(0,14)` + `(7910,7924)`，第一个文本区间是相对于注入的文件的，第二个则相对于顶级文件。
- 通过调试发现，`com.intellij.codeInsight.daemon.impl.IdentifierHighlightingManagerImpl.getMarkupData` 在这种情况下会使用错误的缓存数据。即使是不同的注入的 `editor` 和 `document`，`marker` 和 `data` 仍然会被复用。

分析：
- 应当是 IDE 自身的 BUG（未考虑到同一 hostFile 存在多个 injectedFile，并且这些 injectedFile 存在不同的上下文的边界情况），暂不考虑处理。
- 可能已在之后的 IDE 版本中被修复，需要进一步验证。

参见：
- `com.intellij.codeInsight.daemon.impl.IdentifierHighlightingManagerImpl.getMarkupData`
- `com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer.computeRanges`
