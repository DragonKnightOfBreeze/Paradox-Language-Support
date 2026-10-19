package icu.windea.pls.gradle.markdown

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser

/**
 * Markdown 转 HTML 的构建期工具。
 */
object MarkdownSupport {
    /**
     * 使用 IntelliJ Markdown 解析器将 [markdown] 转换为 HTML。
     */
    fun toHtml(markdown: String): String {
        // 该 Markdown 库目前仅完整支持 LF，因此先归一化换行符
        val text = markdown.normalizeLineSeparator()
        val flavour = GFMFlavourDescriptor()
        val parser = MarkdownParser(flavour, cancellationToken = CancellationToken.NonCancellable)
        val ast = parser.buildMarkdownTreeFromString(text as CharSequence)
        return HtmlGenerator(text, ast, flavour).generateHtml().normalizeLineSeparator()
    }

    private fun String.normalizeLineSeparator() = replace("\\R".toRegex(), "\n")
}
