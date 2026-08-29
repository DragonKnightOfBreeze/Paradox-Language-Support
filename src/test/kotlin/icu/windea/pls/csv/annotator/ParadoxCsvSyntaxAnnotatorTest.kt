package icu.windea.pls.csv.annotator

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
 * @see ParadoxCsvSyntaxAnnotator
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvSyntaxAnnotatorTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region MissingQuotes

    @Test
    fun testMissingQuotes_valid_noErrors() {
        myFixture.configureByText("annotator_missing_quotes.test.csv") {
            """
            name;status
            "value";yes
            value;yes
            # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_valid_noFixes() {
        run {
            myFixture.configureByText("annotator_missing_quotes.test.csv", """
                name;status
                <caret>"value";yes
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.csv", """
                name;status
                "value"<caret>;yes
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.csv", """
                name;status
                <caret>value;yes
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.csv", """
                name;status
                value<caret>;yes
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
    }

    @Test
    fun testMissingQuotes_opening_errors() {
        // for missing closing quotes, not available at middle, but available at end
        myFixture.configureByText("annotator_missing_quotes.test.csv") {
            val m = ChronicleBundle.message("annotator.missing.opening.quote.message")
            """
            name;status
            ${error(m)}${errorEnd()}value";yes
            # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_closing_errors() {
        // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
        // for missing closing quotes, not available at middle, but available at end
        myFixture.configureByText("annotator_missing_quotes.test.csv") {
            val m = ChronicleBundle.message("annotator.missing.closing.quote.message")
            """
            name;status
            "value;yes${error(m)}${errorEnd()}
            # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_opening_fixes() {
        run {
            myFixture.configureByText("annotator_missing_quotes.test.csv", """
                name;status
                <caret>value";yes
                # comment
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.opening.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                name;status
                "value";yes
                # comment
            """.trimIndent())
        }
    }

    @Test
    fun testMissingQuotes_closing_fixes() {
        run {
            // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
            myFixture.configureByText("annotator_missing_quotes.test.csv", """
                name;status
                "value<caret>;yes
                # comment
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                name;status
                "value;yes"
                # comment
            """.trimIndent())
        }
        run {
            // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
            myFixture.configureByText("annotator_missing_quotes.test.csv", """
                name;status
                "value;yes<caret>
                # comment
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                name;status
                "value;yes"
                # comment
            """.trimIndent())
        }
    }

    // endregion
}
