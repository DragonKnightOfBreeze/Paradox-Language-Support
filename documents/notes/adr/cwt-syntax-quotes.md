# 引号相关

> 所属模块：`后端（语法支持） > 完善 CWT 文件的语法支持`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.2
> - 日期：2026-09-02
> - 状态：已实施

计划：
- [x] ~~为了提供更好的语法支持，需要对双引号使用单独的词元（`LEFT_QUOTE` `RIGHT_QUOTE`）。~~
  - ~~尽量在语法级别兼容缺失的情况，通过 syntaxAnnotator 报告错误。~~
  - 回退：这会带来不必要的额外的复杂性，并且可能会降低扫描时的性能。仍然直接识别为普通的字面量词元。

相关代码位置：
- [x] `icu.windea.pls.cwt.editor.CwtSyntaxHighlighter`
- [x] `icu.windea.pls.cwt.codeInsight.editorActions.CwtQuoteHandler`
- [x] `icu.windea.pls.cwt.codeInsight.highlighting.CwtBraceMatcher`
- [x] `icu.windea.pls.cwt.annotator.CwtSyntaxAnnotator`
- [x] `icu.windea.pls.cwt.annotator.CwtSyntaxAnnotatorTest`
