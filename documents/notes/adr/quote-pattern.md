# 引号模式与引号处理

> - 模块：后端 > 通用
> - 版本：v3.0.2
> - 日期：2026-08-23
> - 状态：已实施

说明：
- 进一步的对引号处理的相关重构：引入 `QuotePattern`。
- 这是一种“策略模型”类，既包含相关的元数据，也作为对断言和修改逻辑的策略。
- 相关的接口代码与实现代码统一移到 `icu.windea.pls.core.text`。
- 在 `icu.windea.pls.core.TextExtensions.kt` 中提供相关的扩展方法。

作为一个例子，脚本文件（以及CWT规则文件和CSV文件）中的字符串字面量（比如 `k = v` 中的 k 和 v）是可以用双引号括起的，两种格式基本上是等效的。
这就引出了一些微妙的问题。比如在各种地方要用到的那个字符串，可能需要去除双引号，可能需要保留，可能需要仅在必要时保留。
比如插件提供了意向以去除/加上字面量周围的双引号，但是如果包含一些特定字符（不同文件类型之间存在区别），就不能去除，否则会破坏语法。

通用的工具方法，例如 `String.quoteIfNeeded`，应改为可以传入 `quotePattern` 作为参数，并且默认使用 `QuotePattern.Default`。
对于脚本文件内容，因而可以改为直接使用 `String.quoteIfNeeded(QuotePattern.ParadoxScript)`，这是一种更加直观和简洁的写法。

相比 `QuotePattern`，另一种可能的命名方式是 `QuotePolicy` 或 `QuoteStrategy`。
然而，考虑到与已有的 `TextPattern` 的对齐，以及这确实会携带一些只读数据，仍然选择使用 `QuotePattern`。

对于除了 `needQuote` `canQuote` `canUnquote` `quoteIfNeeded` 以外的所有断言和修改方法，实现细节预期是等效的。
因此调用对应的扩展函数时，可以不显式指定参数 `quotePattern`。

相关代码位置：
- [x] `icu.windea.pls.core.text.QuotePattern`
- [x] `icu.windea.pls.core.text.QuotePatterns`
