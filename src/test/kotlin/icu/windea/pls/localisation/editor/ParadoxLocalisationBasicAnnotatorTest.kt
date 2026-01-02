package icu.windea.pls.localisation.editor

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.test.markIntegrationTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationBasicAnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @Test
    fun testAdjacentIcons_errorAndFix() {
        val errorMsg = PlsBundle.message("localisation.annotator.neighboringIcon")
        myFixture.configureByText(
            "annotator_adjacent_icons.test.yml",
            // 两个相邻图标：£a££b£，应在第二个图标上报错
            """
            l_english:
             KEY1:0 "£a£<error descr="$errorMsg">£b£</error>"
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)

        // Quick Fix: 插入空格
        val fixName = PlsBundle.message("localisation.annotator.neighboringIcon.fix")
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
