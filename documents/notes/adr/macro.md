# 区分“语法宏”与“语义宏”

> 所属模块：`后端 > 通用`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.3
> - 日期：2026-09-17
> - 状态：已实施

说明：
- “宏（macro）”作为一类特殊的语言构造，意味着一种运行时的预处理逻辑，例如表达式评估、模板评估、字符串替换。
- “语法宏（syntax macro）”意味着在语法层面就能确定的“宏”，也就是说，通过语法分析（syntax analysis）就能确定。
- “语义宏（semantic macro）”意味着在语义层面才能确定的“宏”，也就是说，在语法层面只能识别为某个（或某些）语句或表达式，进行语义分析（semantic resolution）后才能确定。
- “语法宏”目前主要包括封装变量引用（`@var`）、参数（`$PARAM$`）、本地化参数（`$PARAM$` `[?PARAM]` `<p>`）、本地化命令（`[...]`）、条件化块（`[[PARAM]...]`）。
- “语义宏”目前主要包括内联脚本（inline script）、定义注入（definition injection）。
- 对于“语法宏”，需要为其引入额外的 PSI 接口，主要包括 `ParadoxScriptMacro` `ParadoxLocalisationMacro`。
- 对于“语义宏”，通常是借助对应的宏规则（macro config）来匹配和解析语义的。理想情况下，其语义应与规则绑定，而不是与游戏类型绑定（或者使用更大程度的硬编码）。
- “插值（interpolation）”是一类特殊的“语法宏”，它们可以以内联形式（inline form），在标识符/字面量节点中，与对应的标识符/字面量词元组合使用。如果这些节点支持使用插值，且这些插值受游戏类型支持的话。
- “参数（parameter）”是一类特殊的“插值”，它们可能携带默认值（default value），可能存在对应的传入参数（argument）。
- 脚本文件和本地化文件中使用的参数语法在细节上存在一些不同之处，并且存在另外一些相对不那么常见的变体，因此暂不引入 `ParadoxParameter` 作为共用 PSI 接口。

相关代码位置：
- `icu.windea.pls.script.ParadoxScriptMacro`
- `icu.windea.pls.localisation.ParadoxLocalisationMacro`
- `icu.windea.pls.config.config.delegated.CwtMacroConfig`
