package icu.windea.pls.core.data

import org.junit.Test
import kotlin.test.assertTrue

/**
 * @see HtmlService
 */
class HtmlServiceTest {
    @Test
    fun smokeTest() {
        val html1 = "<p>first line</p><p>second line</p>"
        val html2 = "<p>first line</p>\n<p>second line</p>"
        assertTrue(HtmlService.areEquivalent(html1, html2))
    }
}
