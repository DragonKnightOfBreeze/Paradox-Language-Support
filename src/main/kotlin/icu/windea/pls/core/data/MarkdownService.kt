package icu.windea.pls.core.data

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.util.concurrent.ConcurrentHashMap

/**
 * 用于数据处理的 Markdown 服务。
 *
 * 基于 [intellij-markdown](https://github.com/JetBrains/markdown)。
 *
 * 默认使用 [MarkdownOptions.GFM] 风格，可通过选项参数 `options` 选择其他方言。
 */
object MarkdownService {
    private val parsers = ConcurrentHashMap<MarkdownFlavourDescriptor, MarkdownParser>()

    private fun MarkdownOptions.resolveParser(): MarkdownParser {
        return parsers.computeIfAbsent(flavour) { MarkdownParser(it) }
    }

    fun toHtml(markdownText: String, options: MarkdownOptions = MarkdownOptions.DEFAULT): String {
        val text = if (options.skipUtf8Bom) markdownText.withoutUtf8Bom() else markdownText
        val flavour = options.flavour
        val parser = options.resolveParser()
        val ast = parser.buildMarkdownTreeFromString(text)
        return HtmlGenerator(text, ast, flavour).generateHtml()
    }

    private fun String.withoutUtf8Bom(): String {
        return if (startsWith('\uFEFF')) substring(1) else this
    }
}
