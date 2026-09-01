@file:Suppress("unused")

package icu.windea.pls.core.text

/**
 * @see QuotePattern
 */
object QuotePatterns {
    /**
     * 默认的引号模式使用双引号，并检查空白和引号本身，如果包含这些字符（包括转义后的引号）则要求用引号包围。
     */
    object Default : QuotePattern.Base('"') {
        override fun checkChar(text: String, start: Int, end: Int, index: Int, char: Char) = char.isWhitespace() || char == quoteChar
    }

    /**
     * 用于构建命令行文本。
     *
     * TODO 目前仅出于兼容性的目的提供。实际上，应当总是优先考虑传入精确的命令参数，然后再执行命令。这涉及未来的进一步的重构。
     */
    object CommandLine: QuotePattern.Base('\'') {
        override fun checkChar(text: String, start: Int, end: Int, index: Int, char: Char) = char.isWhitespace() || char == quoteChar
    }
}
