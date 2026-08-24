package icu.windea.pls.core.text

import org.junit.Assert
import org.junit.Test

/**
 * @see HtmlBuilder
 */
class HtmlBuilderTest {
    @Test
    fun empty() {
        val b = buildHtml()
        Assert.assertTrue(b.isEmpty())
        Assert.assertEquals("", b.toString())
    }

    @Test
    fun append_string() {
        val b = buildHtml()
        Assert.assertSame(b, b.append("abc"))
        Assert.assertEquals("abc", b.toString())
        Assert.assertFalse(b.isEmpty())
    }

    @Test
    fun append_value() {
        Assert.assertEquals("123", buildHtml().append(123).toString())
        Assert.assertEquals("true", buildHtml().append(true).toString())
        Assert.assertEquals("null", buildHtml().append(null).toString())
    }

    @Test
    fun indent() {
        Assert.assertEquals("&nbsp;&nbsp;&nbsp;&nbsp;", buildHtml().indent().toString())
    }

    @Test
    fun br() {
        Assert.assertEquals("<br/>", buildHtml().br().toString())
    }

    @Test
    fun link() {
        Assert.assertEquals("<a href=\"http://x\">label</a>", buildHtml().link("http://x", "label").toString())
    }

    @Test
    fun link_escapeXml() {
        Assert.assertEquals("<a href=\"a&amp;b\">c&lt;d</a>", buildHtml().link("a&b", "c<d").toString())
    }

    @Test
    fun link_escapeLabelFalse() {
        Assert.assertEquals("<a href=\"a&amp;b\">c<d</a>", buildHtml().link("a&b", "c<d", escapeLabel = false).toString())
    }

    @Test
    fun image_localFalse() {
        Assert.assertEquals("<img src=\"http://x/a.png\"/>", buildHtml().image("http://x/a.png", local = false).toString())
    }

    @Test
    fun image_size_localFalse() {
        Assert.assertEquals(
            "<img src=\"http://x/a.png\" style=\"width:100px; height:50px;\"/>",
            buildHtml().image("http://x/a.png", 100, 50, local = false).toString(),
        )
    }

    @Test
    fun image_localTrue() {
        val result = buildHtml().image("foo/bar.png", local = true).toString()
        Assert.assertTrue(result.startsWith("<img src=\"file:"))
        Assert.assertTrue(result.contains("foo/bar.png"))
        Assert.assertTrue(result.endsWith("\"/>"))
    }

    @Test
    fun buildHtml_dsl() {
        val b = buildHtml {
            append("a")
            br()
            append("b")
        }
        Assert.assertEquals("a<br/>b", b.toString())
    }
}
