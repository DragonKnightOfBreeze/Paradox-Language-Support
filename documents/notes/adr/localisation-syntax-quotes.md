# 引号相关

> - 模块：后端（语法支持） > 完善本地化文件的语法支持
> - 版本：v3.0.2
> - 日期：2026-09-02
> - 状态：已实施

计划：
- [x] 为了提供更好的语法支持，需要对双引号使用单独的词元（`LEFT_QUOTE` `RIGHT_QUOTE`）。

相关代码位置：
- [x] `icu.windea.pls.localisation.editor.ParadoxLocalisationSyntaxHighlighter`
- [x] `icu.windea.pls.localisation.editor.ParadoxLocalisationTextSyntaxHighlighter`
- [x] `icu.windea.pls.localisation.codeInsight.editorActions.ParadoxLocalisationQuoteHandler`
- [x] `icu.windea.pls.localisation.codeInsight.highlighting.ParadoxLocalisationBraceMatcher`
- [x] `icu.windea.pls.localisation.annotator.ParadoxLocalisationSyntaxAnnotator`
- [x] `icu.windea.pls.localisation.annotator.ParadoxLocalisationSyntaxAnnotatorTest`
