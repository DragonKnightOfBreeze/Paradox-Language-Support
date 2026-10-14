# 完善 lexer 的重置逻辑

> - 模块：后端 > 语言基础设施
> - 版本：v3.0.2+
> - 日期：2026-09-06+
> - 状态：部分实施

说明：
- 对于脚本文件的 lexer 以及本地化文本的 lexer，实现中是带有较复杂的上下文信息的（`stateStack` 和 `expectStack`）
- 这些上下文信息可能在 lexer reset 后残留，需要考虑如何正确地重置（清空）。
- 通过完善这些 lexer 的重置逻辑，可以更好地兼容代码高亮的增量更新。

计划（2026-09-06）：
- [x] 在 `ParadoxScript.flex` 和 `ParadoxLocalisation.Text.flex` 中暴露 `clearContext()` 和 ~~`isRestartable()`~~
- [x] 在 `ParadoxScriptLexer` 和 `ParadoxLocalisationTextLexer` 中重载 `start()`
- [x] ~~引入 `HighlightingLexer`，并让 `ParadoxScriptHighlightingLexer` 和 `ParadoxLocalisationHighlightingLexer` 实现 `RestartableLexer`~~ - 不能这样简单地达成目的。

计划（2026-09-06）：
- [x] 不实现 `RestartableLexer`，保持 FlexAdapter 的常规增量重启机制。
- [x] 修改状态机约束，使得只有在上下文信息为空时， JFlex state 才允许为 `YYINITIAL`。
- [x] 如果上下文信息不为空（或者将不为空），不应直接使用 `YYINITIAL`，因为这意味着 lexer 仍然不能（从当前位置）安全地进行增量重新扫描。
- [x] 无论是否实现 `RestartableLexer`，都应当在 `start` 方法中清空上下文信息。

相关文件位置：
- `src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex`
- `src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex`

参见：
- `com.intellij.lexer.Lexer.start(java.lang.CharSequence, int, int, int)`
- `com.intellij.lexer.RestartableLexer`
- `com.intellij.openapi.editor.ex.util.LexerEditorHighlighter.incrementalUpdate`
