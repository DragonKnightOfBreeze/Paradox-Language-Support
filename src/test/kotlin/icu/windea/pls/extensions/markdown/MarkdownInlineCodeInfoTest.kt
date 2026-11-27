@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown

import com.intellij.openapi.util.TextRange
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownInlineCodeInfo
 */
@RunWith(JUnit4::class)
class MarkdownInlineCodeInfoTest : MarkdownTestBase() {
    // region from

    @Test
    fun testFrom_plainName() {
        configureMarkdown("`foo`")

        val info = MarkdownInlineCodeInfo.from(findCodeSpan())
        assertNotNull(info)
        assertEquals("", info!!.prefix)
        assertEquals("foo", info.name)
        assertEquals(TextRange(1, 4), info.rangeInElement)
    }

    @Test
    fun testFrom_scriptedVariablePrefix() {
        configureMarkdown("`@greeting`")

        val info = MarkdownInlineCodeInfo.from(findCodeSpan())
        assertNotNull(info)
        assertEquals("@", info!!.prefix)
        assertEquals("greeting", info.name)
        assertEquals(TextRange(2, 10), info.rangeInElement)
    }

    @Test
    fun testFrom_doubleBackticks() {
        configureMarkdown("``foo``")

        val info = MarkdownInlineCodeInfo.from(findCodeSpan())
        assertNotNull(info)
        assertEquals("", info!!.prefix)
        assertEquals("foo", info.name)
        assertEquals(TextRange(2, 5), info.rangeInElement)
    }

    @Test
    fun testFrom_blankContent() {
        configureMarkdown("` `")

        assertNull(MarkdownInlineCodeInfo.from(findCodeSpan()))
    }

    @Test
    fun testFrom_nameWithBlank() {
        configureMarkdown("`foo bar`")

        assertNull(MarkdownInlineCodeInfo.from(findCodeSpan()))
    }

    @Test
    fun testFrom_notCodeSpan() {
        configureMarkdown("plain text")

        assertNull(MarkdownInlineCodeInfo.from(myFixture.file))
    }

    @Test
    fun testFrom_matchesManagerResolveInfo() {
        configureMarkdown("`@greeting`")

        val codeSpan = findCodeSpan()
        assertEquals(
            MarkdownExtensionManager.resolveInfoFromInlineCode(codeSpan),
            MarkdownInlineCodeInfo.from(codeSpan)
        )
    }

    // endregion
}
