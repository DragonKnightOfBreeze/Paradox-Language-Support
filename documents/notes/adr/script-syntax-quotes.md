# 引号相关

> 所属模块：`后端（语法支持） > 脚本文件`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.2
> - 日期：2026-09-02
> - 状态：已实施

计划：
- [x] ~~为了提供更好的语法支持，需要对双引号使用单独的词元（`LEFT_QUOTE` `RIGHT_QUOTE`）。这是因为，比如，用引号括起的标识符/字面量中也能照常使用插值语法。~~
  - ~~尽量在语法级别兼容缺失的情况，通过 syntaxAnnotator 报告错误。~~
  - 回退：这会带来不必要的额外的复杂性，并且可能会降低扫描时的性能。仍然直接识别为普通的字面量词元。

相关代码位置：
- [x] `icu.windea.pls.script.editor.ParadoxScriptSyntaxHighlighter`
- [x] `icu.windea.pls.script.codeInsight.editorActions.ParadoxScriptQuoteHandler`
- [x] `icu.windea.pls.script.codeInsight.highlighting.ParadoxScriptBraceMatcher`
- [x] `icu.windea.pls.script.annotator.ParadoxScriptSyntaxAnnotator`
- [x] `icu.windea.pls.script.annotator.ParadoxScriptSyntaxAnnotatorTest`
