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
 * @see icu.windea.pls.localisation.codeInsight.ParadoxLocalisationSyntaxAnnotator
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationSyntaxAnnotatorTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region testMissingQuotes

    @Test
    fun testMissingQuotes_valid_noErrors() {
        myFixture.configureByText("annotator_missing_quotes.test.yml") {
            """
            l_english:
             key: <caret>"value"
             # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_valid_noFixes() {
        run {
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: <caret>"value"
                 # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: "value"<caret>
                 # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: <caret>"some text"
                 # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: "some text"<caret>
                 # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
    }

    @Test
    fun testMissingQuotes_opening_errors() {
        myFixture.configureByText("annotator_missing_quotes.test.yml") {
            val m1 = ChronicleBundle.message("annotator.missing.opening.quote.message")
            val m2 = ChronicleBundle.message("annotator.missing.closing.quote.message")
            """
            l_english:
             key: ${error(m1)}${errorEnd()}value${error(m2)}${errorEnd()}
             key: ${error(m1)}${errorEnd()}value"
             # comment
             key: ${error(m1)}${errorEnd()}some text${error(m2)}${errorEnd()}
             key: ${error(m1)}${errorEnd()}some text"
             # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_closing_errors() {
        // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
        myFixture.configureByText("annotator_missing_quotes.test.yml") {
            val m1 = ChronicleBundle.message("annotator.missing.opening.quote.message")
            val m2 = ChronicleBundle.message("annotator.missing.closing.quote.message")
            """
            l_english:
             key: ${error(m1)}${errorEnd()}value${error(m2)}${errorEnd()}
             key: "value${error(m2)}${errorEnd()}
             # comment
             key: ${error(m1)}${errorEnd()}some text${error(m2)}${errorEnd()}
             key: "some text${error(m2)}${errorEnd()}
             # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_opening_fixes() {
        run {
            // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: <caret>value
                 # comment
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.opening.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: "value
                 # comment
            """.trimIndent())
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: <caret>value"
                 # comment
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.opening.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: "value"
                 # comment
            """.trimIndent())
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: <caret>some text"
                 # comment
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.opening.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: "some text"
                 # comment
            """.trimIndent())
        }
    }

    @Test
    fun testMissingQuotes_closing_fixes() {
        run {
            // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: value<caret>
                 # comment
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: value"
                 # comment
            """.trimIndent())
        }
        run {
            // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: "value<caret>
                 # comment
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: "value"
                 # comment
            """.trimIndent())
        }
        run {
            // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
            myFixture.configureByText("annotator_missing_quotes.test.yml", """
                l_english:
                 key: "some text<caret>
                 # comment
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                l_english:
                 key: "some text"
                 # comment
            """.trimIndent())
        }
    }

    // endregion

    // region testAdjacentIcons

    @Test
    fun testAdjacentIcons_valid_noErrors() {
        myFixture.configureByText("annotator_adjacent_icons.test.yml") {
            """
            l_english:
             KEY1:0 "£a£ £b£"
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testAdjacentIcons_valid_noFixes() {
        myFixture.configureByText("annotator_adjacent_icons.test.yml", """
            l_english:
             KEY1:0 "£a£ <caret>£b£"
        """.trimIndent())
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == ChronicleBundle.message("annotator.adjacent.icon.unexpected.fix") })
    }

    @Test
    fun testAdjacentIcons_errors() {
        // 两个相邻图标：£a££b£，应在第二个图标上报错
        myFixture.configureByText("annotator_adjacent_icons.test.yml") {
            val m = ChronicleBundle.message("annotator.adjacent.icon.unexpected.message")
            """
            l_english:
             KEY1:0 "£a£${error(m)}£${errorEnd()}b£"
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testAdjacentIcons_fixes() {
        // Quick Fix: 插入空格
        myFixture.configureByText("annotator_adjacent_icons.test.yml", """
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

    // endregion
}
