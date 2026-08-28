package icu.windea.pls.script.annotator

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
 * @see ParadoxScriptSyntaxAnnotator
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptSyntaxAnnotatorTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testMissingQuotes_errors() {
        // NOTE 3.0.2 `# eof` here is required, or the eof error will be ignored
        myFixture.configureByText("annotator_missing_quotes.test.txt") {
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
            myFixture.configureByText("annotator_adjacent_icons.fix.test.txt", """
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
            myFixture.configureByText("annotator_adjacent_icons.fix.test.txt", """
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
            myFixture.configureByText("annotator_adjacent_icons.fix.test.txt", """
                <caret>"value"
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.txt", """
                "value"<caret>
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.txt", """
                <caret>value
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_adjacent_icons.fix.test.txt", """
                value<caret>
                # eof
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
    }

    @Test
    fun testOperator_errors() {
        val tag = ChronicleBundle.message("annotator.leading.blank.unexpected.message.1").toErrorTag()

        myFixture.configureByText(
            "annotator_operator.test.txt",
            """
            key? =value
            key ${tag.start}? =${tag.end}value
            key? = value
            key ${tag.start}? =${tag.end} value
            key?=value
            key ?=value
            key?= value
            key ?= value
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun testInlineMathScriptedVariableReference_errors() {
        val tag = ChronicleBundle.message("annotator.leading.at.unexpected.message.1").toErrorTag()

        myFixture.configureByText(
            "annotator_inline_math_scripted_variable_reference.test.txt",
            """
            key = @[ v + 1 ]
            key = @[ ${tag.start}@${tag.end}v + 1 ]
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}
