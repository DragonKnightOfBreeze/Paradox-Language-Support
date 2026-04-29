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
    private val intentionName1 = PlsBundle.message("intention.copyLocalisationListWithLocale")
    private val intentionName2 = PlsBundle.message("intention.copyLocalisationListWithoutLocale")

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

        myFixture.launchAction(intentionName1)
        checkClipboard("""
l_english:
 # Comment
 KEY:0 "Some text."
 OTHER_KEY:0 "Some other text."
        """.trimIndent())

        myFixture.launchAction(intentionName2)
        checkClipboard("""
# Comment
KEY:0 "Some text."
OTHER_KEY:0 "Some other text."
        """.trimIndent())
    }
}
