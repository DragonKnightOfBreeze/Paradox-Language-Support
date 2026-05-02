package icu.windea.pls.core.data

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

object MarkdownService {
    val gfmFlavour by lazy {
        GFMFlavourDescriptor()
    }
    val gfmParser by lazy {
        MarkdownParser(gfmFlavour)
    }

    fun toHtml(markdownText: String): String {
        val flavour = gfmFlavour
        val parser = gfmParser
        val ast = parser.buildMarkdownTreeFromString(markdownText)
        return HtmlGenerator(markdownText, ast, flavour).generateHtml()
    }
}
