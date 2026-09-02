package icu.windea.pls.cwt.annotator

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
 * @see icu.windea.pls.cwt.codeInsight.CwtSyntaxAnnotator
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtSyntaxAnnotatorTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region testMissingQuotes

    @Test
    fun testMissingQuotes_valid_noErrors() {
        myFixture.configureByText("annotator_missing_quotes.test.cwt") {
            """
            <caret>"value"
            # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_valid_noFixes() {
        run {
            myFixture.configureByText("annotator_missing_quotes.test.cwt", """
                <caret>"value"
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.cwt", """
                "value"<caret>
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.cwt", """
                <caret>value
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.cwt", """
                value<caret>
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
    }

    @Test
    fun testMissingQuotes_opening_errors() {
        myFixture.configureByText("annotator_missing_quotes.test.cwt") {
            val m = ChronicleBundle.message("annotator.missing.opening.quote.message")
            """
            ${error(m)}${errorEnd()}value"
            # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_closing_errors() {
        // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
        myFixture.configureByText("annotator_missing_quotes.test.cwt") {
            val m = ChronicleBundle.message("annotator.missing.closing.quote.message")
            """
            "value${error(m)}${errorEnd()}
            # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_opening_fixes() {
        myFixture.configureByText("annotator_missing_quotes.test.cwt", """
            <caret>value"
            # comment
        """.trimIndent())
        val fixName = ChronicleBundle.message("annotator.missing.opening.quote.fix")
        val intention = myFixture.findSingleIntention(fixName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""
            "value"
            # comment
        """.trimIndent())
    }

    @Test
    fun testMissingQuotes_closing_fixes() {
        // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
        myFixture.configureByText("annotator_missing_quotes.test.cwt", """
            "value<caret>
            # comment
        """.trimIndent())
        val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
        val intention = myFixture.findSingleIntention(fixName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""
            "value"
            # comment
        """.trimIndent())
    }

    // endregion
}
