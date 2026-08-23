@file:Suppress("unused")

package icu.windea.pls.core.text

/**
 * @see QuotePattern
 */
object QuotePatterns {
    /**
     * 默认的引号模式使用双引号，并检查空白和引号字符本身，如果包含这些字符则要求用引号包围。
     */
    object Default : QuotePattern.Base('"') {
        override fun checkUnquotedChar(char: Char) = char.isWhitespace() || char == quoteChar
    }

    /**
     * 类似 [Default]，但是使用单引号。
     */
    object SingleQuote: QuotePattern.Base('\'') {
        override fun checkUnquotedChar(char: Char) = char.isWhitespace() || char == quoteChar
    }

    /**
     * 用于构建命令行文本。
     *
     * TODO 目前仅出于兼容性的目的提供。实际上，应当总是优先考虑传入精确的命令参数，然后再执行命令。
     */
    object CommandLine: QuotePattern.Base('\'') {
        override fun checkUnquotedChar(char: Char) = char.isWhitespace() || char == quoteChar
    }
}
