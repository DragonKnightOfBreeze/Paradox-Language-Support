package icu.windea.pls.localisation.annotator

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.configureByText
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
class ParadoxLocalisationSyntaxAnnotatorTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testMissingQuotes_errors() {
        // 两个标注：value" 缺失开引号；"value 缺失闭引号
        myFixture.configureByText("annotator_missing_quotes.test.cwt") {
            val m1 = ChronicleBundle.message("annotator.missing.opening.quote.message")
            val m2 = ChronicleBundle.message("annotator.missing.closing.quote.message")
            """
            l_english:
             key: "value"
             key: ${error(m1)}${errorEnd()}value${error(m2)}${errorEnd()}
             key: ${error(m1)}${errorEnd()}value"
             key: "value${error(m2)}${errorEnd()}

             key: "value value"
             key: ${error(m1)}${errorEnd()}value value${error(m2)}${errorEnd()}
             key: ${error(m1)}${errorEnd()}value value"
             key: "value value${error(m2)}${errorEnd()}
            """.trimIndent()
        }
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun testMissingQuotes_fixes() {
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.yml", """
                l_english:
                 key: <caret>value"
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.opening.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: "value"
            """.trimIndent())
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.yml", """
                l_english:
                 key: "value<caret>
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: "value"
            """.trimIndent())
        }

        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.yml", """
                l_english:
                 key: <caret>value value"
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.opening.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: "value value"
            """.trimIndent())
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.yml", """
                l_english:
                 key: "value value<caret>
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: "value value"
            """.trimIndent())
        }
    }

    @Test
    fun testMissingQuotes_fixes_noAvailable() {
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.yml", """
                l_english:
                 key: <caret>"value value"
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.yml", """
                l_english:
                 key: "value value"<caret>
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
    }

    @Test
    fun testAdjacentIcons_errors() {
        // 两个相邻图标：£a££b£，应在第二个图标上报错
        myFixture.configureByText("annotator_adjacent_icons.test.yml") {
            val m1 = ChronicleBundle.message("annotator.adjacent.icon.unexpected.message")
            """
            l_english:
             KEY1:0 "£a£${error(m1)}£${errorEnd()}b£"
            """.trimIndent()
        }
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun testAdjacentIcons_fixes() {
        // Quick Fix: 插入空格
        myFixture.configureByText("annotator_adjacent_icons.fix.test.yml", """
            l_english:
             KEY1:0 "£a£<caret>£b£"
        """.trimIndent())
        val fixName = ChronicleBundle.message("annotator.adjacent.icon.unexpected.fix")
        val intention = myFixture.findSingleIntention(fixName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""
            l_english:
             KEY1:0 "£a£ £b£"
        """.trimIndent())
    }

    @Test
    fun testAdjacentIcons_fixes_noAvailable() {
        myFixture.configureByText("annotator_adjacent_icons.fix.test.yml", """
            l_english:
             KEY1:0 "£a£ <caret>£b£"
        """.trimIndent())
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == ChronicleBundle.message("annotator.adjacent.icon.unexpected.fix") })
    }
}
