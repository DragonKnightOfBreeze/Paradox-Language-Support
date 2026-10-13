# P14 在事件脚本文件中，变更文件内容后，其中特定部分的作用域推断会发生误报

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - 版本：v2.1.10
> - 日期：2026-06-17
> - 状态：已解决

无法稳定复现，需要研究。

用例：
- VIC3 `game/events/election_events/communist_fascist_election_events.txt`

推断：
- 应当是以下位置的异常，导致使用默认值 true，最终导致随机的作用域字段表达式错误地匹配了随机的模板表达式，最终导致匹配失败。
- 除了模板表达式以外，当候选项中存在定义、本地化等时，应当也可能出现这种情况。
- 与这里的代码对并发的处理有关。
- 可能与 P4 P10 P11 P14 有关，修复这个问题可能会（至少在一定程度上）一并修复这些问题。

```
java.lang.ClassCastException: java.lang.Boolean cannot be cast to kotlin.jvm.functions.Function0
	at icu.windea.pls.lang.match.ParadoxMatchResult$LazyMatch.doGetCatching(ParadoxMatchResult.kt:89)
	at icu.windea.pls.lang.match.ParadoxMatchResult$LazyMatch.get(ParadoxMatchResult.kt:68)
```

最终解决方案：修复并优化 `ParadoxMatchResult.LazyMatch` 中的并发处理逻辑。
