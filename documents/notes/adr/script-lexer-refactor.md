# 完善脚本文件的 lexer

> - 模块：后端 > 语言基础设施
> - 版本：v3.0.2
> - 日期：2026-08-27
> - 状态：已实施（告一段落）

说明：
- 需要在内部维护堆栈（`stateStack`），以记录上下文状态（退出时需要回退到的状态）。例如，在 `key_$PARAM$ = value` 中，从 `IN_PARAMETER` 回退到 `IN_KEY`。
- 需要在内部维护堆栈（`expectStack`），以记录当前期望的构造类型（回退上下文状态时，类型需要匹配）。例如，如果遇到 `{`，则期望的构造类型应设置为 `EXPECT_BLOCK`。
- 需要在处理时，按需允许和跳过空白词元（`TokenType.WHITE_SPACE`）。
- 通过 `enterState(state, expect)` 和 `exitState(expect)` 进行入栈和出栈。
- 如果下游状态中遇到了无法直接匹配的字符（例如，某个上游状态的边界，如 `}`），应逐步回退到上游状态（通过 `[^] { exitState(); yypushback(yylength()); }`），交由那个上游状态处理，从而简化代码。

目的：
- 为了达成更准确的词法分析：更好的对可选空白和高级插值语法（如 `a_$p$_[[p]b]`）的支持，更完善的错误恢复策略。
- 为了便于后续重构：仍然存在更多的细节和边界情况，需要进一步的研究、设计和调整。

相关文件位置：
- `src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex`
- `src/main/kotlin/icu/windea/pls/script/ParadoxScript.InlineMath.flex`
