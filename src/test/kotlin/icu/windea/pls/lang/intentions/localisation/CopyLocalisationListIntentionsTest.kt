package icu.windea.pls.lang.intentions.localisation

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.awt.datatransfer.DataFlavor

/**
 * @see CopyLocalisationListWithLocaleIntention
 * @see CopyLocalisationListWithoutLocaleIntention
 */
@RunWith(JUnit4::class)
class CopyLocalisationListIntentionsTest : BasePlatformTestCase() {
    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun checkClipboard(text: String) {
        val contents = CopyPasteManager.getInstance().getContents<String>(DataFlavor.stringFlavor)
        assertEquals(text, contents)
    }

    @Test
    fun testSimple() {
        myFixture.configureByText("test.yml", """
# Comment
<caret>l_english:
 # Comment
 KEY:0 "Some text."
 OTHER_KEY:0 "Some other text."
        """.trimIndent())

        myFixture.launchAction(PlsBundle.message("intention.copyLocalisationListWithLocale"))
        checkClipboard("""
l_english:
 # Comment
 KEY:0 "Some text."
 OTHER_KEY:0 "Some other text."
        """.trimIndent())

        myFixture.launchAction(PlsBundle.message("intention.copyLocalisationListWithoutLocale"))
        checkClipboard("""
# Comment
KEY:0 "Some text."
OTHER_KEY:0 "Some other text."
        """.trimIndent())
    }
}
