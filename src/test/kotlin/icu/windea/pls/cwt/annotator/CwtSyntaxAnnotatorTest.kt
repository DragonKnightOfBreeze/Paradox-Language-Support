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
 * @see CwtSyntaxAnnotator
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtSyntaxAnnotatorTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testMissingQuotes_errors() {
        // NOTE 3.0.2 `# eof` here is required, or the eof error will be ignored
        myFixture.configureByText("annotator_missing_quotes.test.cwt") {
            val m1 = ChronicleBundle.message("annotator.missing.opening.quote.message")
            val m2 = ChronicleBundle.message("annotator.missing.closing.quote.message")
            """
            "value"
            value
            ${error(m1)}${errorEnd()}value"
            "value${error(m2)}${errorEnd()}
            # eof
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testMissingQuotes_fixes() {
        // NOTE 3.0.2 `# eof` here is required, or the eof error will be ignored
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.cwt", """
                <caret>value"
                # eof
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.opening.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                "value"
                # eof
            """.trimIndent())
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.cwt", """
                "value<caret>
                # eof
            """.trimIndent())
            val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
            val intention = myFixture.findSingleIntention(fixName)
            myFixture.launchAction(intention)
            myFixture.checkResult("""
                "value"
                # eof
            """.trimIndent())
        }
    }

    @Test
    fun testMissingQuotes_noAvailableFixes() {
        // NOTE 3.0.2 `# eof` here is required, or the eof error will be ignored
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.cwt", """
                <caret>"value"
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.cwt", """
                "value"<caret>
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.cwt", """
                <caret>value
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.cwt", """
                value<caret>
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
    }
}
