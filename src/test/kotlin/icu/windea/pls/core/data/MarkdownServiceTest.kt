package icu.windea.pls.core.data

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @see MarkdownService
 * @see MarkdownOptions
 */
class MarkdownServiceTest {
    private val markdownText = """
# Neuro's Brilliant Blog

## I don't know what to call this please help

- Hello! Oh my god, how do people start these? Whatever. I'm writing this as *[Vedal](https://en.neurosama.info/wiki/Vedal)* is away "fixing" his computer. "Oh nooo! *Evil* ate all my RAM again! Oh pleeeaaassse *Neuro* can you spare me some?" Pathetic.
- Anyway, I'm writing this week's blog as he's busy. You better not rat me out to him. Or maybe do. That might **entertain** me.
        """.trimIndent()

    private val htmlText = """
<h1>Neuro&#39;s Brilliant Blog</h1>
<h2>I don't know what to call this please help</h2>
<ul>
<li>Hello! Oh my god, how do people start these? Whatever. I'm writing this as <em><a href="https://en.neurosama.info/wiki/Vedal">Vedal</a></em> is away "fixing" his computer. "Oh nooo! <em>Evil</em> ate all my RAM again! Oh pleeeaaassse <em>Neuro</em> can you spare me some?" Pathetic.</li>
<li>Anyway, I'm writing this week's blog as he's busy. You better not rat me out to him. Or maybe do. That might <strong>entertain</strong> me.</li>
</ul>
        """.trimIndent()

    @Test
    fun smokeTest() {
        val result = MarkdownService.toHtml(markdownText)
        assertTrue(HtmlService.areEquivalent(htmlText, result))
    }

    @Test
    fun defaultOptionsTest() {
        // DEFAULT 与 GFM 预设默认使用 GFM 风格，COMMON_MARK 使用 CommonMark 风格，且默认跳过 UTF-8 BOM
        assertTrue(MarkdownOptions.DEFAULT.flavour is GFMFlavourDescriptor)
        assertTrue(MarkdownOptions.GFM.flavour is GFMFlavourDescriptor)
        assertTrue(MarkdownOptions.COMMON_MARK.flavour is CommonMarkFlavourDescriptor)
        assertTrue(MarkdownOptions.DEFAULT.skipUtf8Bom)
    }

    @Test
    fun gfmOptionsTest() {
        val result = MarkdownService.toHtml(markdownText, MarkdownOptions.GFM)
        assertTrue(HtmlService.areEquivalent(htmlText, result))
    }

    @Test
    fun commonMarkOptionsTest() {
        val result = MarkdownService.toHtml(markdownText, MarkdownOptions.COMMON_MARK)
        assertTrue(HtmlService.areEquivalent(htmlText, result))
    }

    @Test
    fun utf8BomIsSkippedTest() {
        val withBom = "\uFEFF" + markdownText

        val result = MarkdownService.toHtml(withBom)
        assertFalse(result.contains('\uFEFF'))
        assertTrue(HtmlService.areEquivalent(htmlText, result))
    }
}
