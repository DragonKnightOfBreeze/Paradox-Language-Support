# 模板表达式与模式匹配的相关重构

> - 模块：后端 > 规则系统
> - 版本：v3.0.3
> - 日期：2026-09-23
> - 状态：已实施

将“模式感知/patternAware”的数据类型，重命名为“模式/pattern”数据类型分类。

考虑将“模板/template”数据类型移出“模式/pattern”数据类型分类。
因为需要与之对齐的是“片段/snippet”数据类型以及与复杂表达式相关的数据类型，而非“正则/regex”这样的带模式的数据类型。

与此同时，模式匹配（参见 `ParadoxPatternMatcher`）也不再支持“模版/template”数据类型。
因为模板数据类型的数据表达式实际上是其他多种常规数据类型的数据表达式的组合，而这在模式匹配时是不必要的，通常使用“正则/regex”数据类型就已经足够。
如果不移出，当模板表达式仅包含一个片段时，由于这在目前会回退为空的模板表达式，事情会变得有些微妙……

相关代码位置：
- [x] `icu.windea.pls.config.CwtDataTypes.getTemplate`
- [x] `icu.windea.pls.ep.match.ParadoxPatternMatcher`
