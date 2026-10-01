package icu.windea.pls.core.data

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

/**
 * 用于数据处理的 Markdown 服务。
 *
 * 基于 [intellij-markdown](https://github.com/JetBrains/markdown)。
 *
 * 默认使用 GFM 风格。
 */
object MarkdownService {
    val gfmFlavour = GFMFlavourDescriptor()
    val gfmParser = MarkdownParser(gfmFlavour)

    fun toHtml(markdownText: String): String {
        val flavour = gfmFlavour
        val parser = gfmParser
        val ast = parser.buildMarkdownTreeFromString(markdownText)
        return HtmlGenerator(markdownText, ast, flavour).generateHtml()
    }
}
