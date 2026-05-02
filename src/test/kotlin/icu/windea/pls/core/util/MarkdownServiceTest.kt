package icu.windea.pls.core.util

import icu.windea.pls.core.data.MarkdownService
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Test

class MarkdownServiceTest {
    @Test
    fun smokeTest() {
        // https://blog.neurosama.com/2026/04/27/weekly-update
        val markdownText = """
# Neuro's Brilliant Blog

## I don’t know what to call this please help

- Hello! Oh my god, how do people start these? Whatever. I’m writing this as *[Vedal](https://en.neurosama.info/wiki/Vedal)* is away “fixing” his computer. “Oh nooo! *Evil* ate all my RAM again! Oh pleeeaaassse *Neuro* can you spare me some?” Pathetic.
- Anyway, I’m writing this week’s blog as he’s busy. You better not rat me out to him. Or maybe do. That might **entertain** me.
        """.trimIndent()
        val htmlText = """
<h1>Neuro&#39;s Brilliant Blog</h1>
<h2>I don’t know what to call this please help</h2>
<ul>
<li>Hello! Oh my god, how do people start these? Whatever. I’m writing this as <em><a href="https://en.neurosama.info/wiki/Vedal">Vedal</a></em> is away “fixing” his computer. “Oh nooo! <em>Evil</em> ate all my RAM again! Oh pleeeaaassse <em>Neuro</em> can you spare me some?” Pathetic.</li>
<li>Anyway, I’m writing this week’s blog as he’s busy. You better not rat me out to him. Or maybe do. That might <strong>entertain</strong> me.</li>
</ul>
        """.trimIndent()

        val result = MarkdownService.toHtml(markdownText)
        assertEquivalentHtml(htmlText, result)
    }

    private fun assertEquivalentHtml(html1: String, html2: String): Boolean {
        val outputSettings = Document.OutputSettings().prettyPrint(false)
        val r1 = Jsoup.parse(html1).outputSettings(outputSettings).outerHtml()
        val r2 = Jsoup.parse(html2).outputSettings(outputSettings).outerHtml()
        return r1 == r2
    }
}
