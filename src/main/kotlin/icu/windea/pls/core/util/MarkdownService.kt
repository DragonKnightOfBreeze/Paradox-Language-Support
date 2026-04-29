package icu.windea.pls.core.util

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

object MarkdownService {
    val flavour by lazy {
        GFMFlavourDescriptor()
    }
    val parser by lazy {
        MarkdownParser(flavour)
    }

    fun toHtml(markdownText: String): String {
        val flavour = flavour
        val parser = parser
        val ast = parser.buildMarkdownTreeFromString(markdownText)
        return HtmlGenerator(markdownText, ast, flavour).generateHtml()
    }
}
