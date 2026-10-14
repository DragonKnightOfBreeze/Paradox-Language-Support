# 整理部分语言功能的入口，移动到更合适的位置

> - 模块：后端 > 语言功能支持
> - 版本：v3.0.2
> - 日期：2026-09-05
> - 状态：已实施

计划：
- [x] `HighlightingAnnotator` -> `highlighting` - 涉及基础语言高亮（语法/语义）。
- [x] `SyntaxAnnotator` -> `codeInsight` - 涉及通过注解器报告的语法错误（无法禁用）。
- [x] `ColorSettingsPage` -> `highlighting` - 涉及基础语言高亮（语法/语义）。
- [x] `HighlighterColors` -> `highlighting` - 涉及基础语言高亮（语法/语义）。
- [x] `HighlighterColorSets` -> `highlighting` - 涉及基础语言高亮（语法/语义）。
- [x] `SpellcheckingStrategy` -> `spellchecker` - 由于传入了 `PsiElement`，不应选择 `text`。
- [x] `SyntaxHighlighter` ->  -> `highlighting` - 涉及基础语言高亮（语法/语义）。
- [x] `SyntaxHighlighterFactory` -> `highlighting` - 涉及基础语言高亮（语法/语义）。
- [x] `ExtendWordSelectionHandler` -> `editor.wordSelection` - 包含多个相关的 EP 实现。
- [x] `codeInsight.editorActions.*` -> `editor` - 两者都有参考用到，出于缩短 fqName 的考虑选择后者。
