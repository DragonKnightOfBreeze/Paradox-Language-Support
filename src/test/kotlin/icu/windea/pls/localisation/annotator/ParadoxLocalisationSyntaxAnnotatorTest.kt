package icu.windea.pls.localisation.annotator

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.test.HighlightingTestScope
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxLocalisationSyntaxAnnotator
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationSyntaxAnnotatorTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testAdjacentIcons_errorAndFix() {
        val tag = ChronicleBundle.message("message.adjacent.icon.unexpected").toErrorTag()

        // 两个相邻图标：£a££b£，应在第二个图标上报错
        myFixture.configureByText(
            "annotator_adjacent_icons.test.yml",
            """
            l_english:
             KEY1:0 "£a£${tag.start}£b£${tag.end}"
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)

        // Quick Fix: 插入空格
        val fixName = ChronicleBundle.message("fix.adjacent.icon.unexpected")
        myFixture.configureByText(
            "annotator_adjacent_icons_apply.test.yml",
            """
            l_english:
             KEY1:0 "£a£<caret>£b£"
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(fixName)
        myFixture.launchAction(intention)
        assertTrue(myFixture.editor.document.text.contains("\"£a£ £b£\""))
    }
}
