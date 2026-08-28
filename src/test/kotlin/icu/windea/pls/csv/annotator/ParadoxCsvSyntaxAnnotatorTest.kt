package icu.windea.pls.csv.annotator

import com.intellij.lang.annotation.HighlightSeverity
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

    @Test
    fun testMissingQuotes_errors() {
        // NOTE 3.0.2 `# eof` here is required, or the eof error will be ignored
        // for missing closing quotes, not available at middle, but available at end
        myFixture.configureByText("annotator_missing_quotes.test.csv") {
            val m1 = ChronicleBundle.message("annotator.missing.opening.quote.message")
            val m2 = ChronicleBundle.message("annotator.missing.closing.quote.message")
            """
            name;status
            "value";yes
            value;yes
            ${error(m1)}${errorEnd()}value";yes
            "value;yes${error(m2)}${errorEnd()}
            # eof
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_fixes() {
        // NOTE 3.0.2 `# eof` here is required, or the eof error will be ignored
        // for missing closing quotes, not available at middle, but available at end
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.csv", """
                name;status
                <caret>value";yes
                # eof
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.opening.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                name;status
                "value";yes
                # eof
            """.trimIndent())
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.csv", """
                name;status
                "value<caret>;yes
                # eof
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                name;status
                "value;yes"
                # eof
            """.trimIndent())
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.csv", """
                name;status
                "value;yes<caret>
                # eof
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                name;status
                "value;yes"
                # eof
            """.trimIndent())
        }
    }

    @Test
    fun testMissingQuotes_noAvailableFixes() {
        // NOTE 3.0.2 `# eof` here is required, or the eof error will be ignored
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.csv", """
                name;status
                <caret>"value";yes
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.csv", """
                name;status
                "value"<caret>;yes
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.csv", """
                name;status
                <caret>value;yes
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.csv", """
                name;status
                value<caret>;yes
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
    }

    @Test
    fun testValidCsv_noErrors() {
        myFixture.configureByText(
            "annotator_csv_valid.test.csv",
            """
            name;age;desc
            windea;24;dragon_knight
            """.trimIndent()
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.severity == HighlightSeverity.ERROR })
    }
}
