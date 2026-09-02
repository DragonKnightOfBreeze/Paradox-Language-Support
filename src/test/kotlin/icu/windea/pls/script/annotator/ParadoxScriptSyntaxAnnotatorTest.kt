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
 * @see icu.windea.pls.script.codeInsight.ParadoxScriptSyntaxAnnotator
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptSyntaxAnnotatorTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region testMissingQuotes

    @Test
    fun testMissingQuotes_valid_noErrors() {
        myFixture.configureByText("annotator_missing_quotes.test.txt") {
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
            myFixture.configureByText("annotator_missing_quotes.test.txt", """
                <caret>"value"
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.txt", """
                "value"<caret>
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.txt", """
                <caret>value
                # comment
            """.trimIndent())
            val available = myFixture.availableIntentions
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.opening.quote.fix") })
            assertFalse(available.any { it.text == ChronicleBundle.message("annotator.missing.closing.quote.fix") })
        }
        run {
            myFixture.configureByText("annotator_missing_quotes.test.txt", """
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
        myFixture.configureByText("annotator_missing_quotes.test.txt") {
            val m = ChronicleBundle.message("annotator.missing.opening.quote.message")
            """
            ${error(m)}${errorEnd()}value"
            # comment
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    // NOTE 3.0.2 unavailable: no recovery before eof since strings can be multiline
    // @Test
    // fun testMissingQuotes_closing_errors() {
    //     // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
    //     myFixture.configureByText("annotator_missing_quotes.test.txt") {
    //         val m = ChronicleBundle.message("annotator.missing.closing.quote.message")
    //         """
    //         "value${error(m)}${errorEnd()}
    //         # comment
    //         """.trimIndent()
    //     }
    //     myFixture.checkHighlighting()
    // }

    @Test
    fun testMissingQuotes_opening_fixes() {
        myFixture.configureByText("annotator_missing_quotes.test.txt", """
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

    // NOTE 3.0.2 unavailable: no recovery before eof since strings can be multiline
    // @Test
    // fun testMissingQuotes_closing_fixes() {
    //     // NOTE 3.0.2 `# comment` here is required, or the eof error will be ignored
    //     myFixture.configureByText("annotator_missing_quotes.test.txt", """
    //         "value<caret>
    //         # comment
    //     """.trimIndent())
    //     val fixName = ChronicleBundle.message("annotator.missing.closing.quote.fix")
    //     val intention = myFixture.findSingleIntention(fixName)
    //     myFixture.launchAction(intention)
    //     myFixture.checkResult("""
    //         "value"
    //         # comment
    //     """.trimIndent())
    // }

    // endregion

    // region testOperator

    @Test
    fun testOperator_valid_noErrors() {
        myFixture.configureByText("annotator_operator.test.txt") {
            """
            key? =value
            key? = value
            key?=value
            key?= value
            key ?=value
            key ?= value
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testOperator_errors() {
        myFixture.configureByText("annotator_operator.test.txt") {
            val m = ChronicleBundle.message("annotator.leading.blank.unexpected.message.1")
            """
            key ${error(m)}? =${errorEnd()}value
            key ${error(m)}? =${errorEnd()} value
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testOperator_fixes() {
        myFixture.configureByText("annotator_operator.test.txt", """
            key <caret>? = value
        """.trimIndent())
        val fixName = ChronicleBundle.message("annotator.leading.blank.unexpected.fix")
        val intention = myFixture.findSingleIntention(fixName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""
            key? = value
        """.trimIndent())
    }

    // endregion

    // region testInlineMathScriptedVariableReference

    @Test
    fun testInlineMathScriptedVariableReference_valid_noErrors() {
        myFixture.configureByText("annotator_inline_math_scripted_variable_reference.test.txt") {
            """
            key = @[ v + 1 ]
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testInlineMathScriptedVariableReference_errors() {
        myFixture.configureByText("annotator_inline_math_scripted_variable_reference.test.txt") {
            val m = ChronicleBundle.message("annotator.leading.at.unexpected.message.1")
            """
            key = @[ ${error(m)}@${errorEnd()}v + 1 ]
            """.trimIndent()
        }
        myFixture.checkHighlighting()
    }

    @Test
    fun testInlineMathScriptedVariableReference_fixes() {
        myFixture.configureByText("annotator_inline_math_scripted_variable_reference.test.txt", """
            key = @[ @v + 1 ]
        """.trimIndent())
        val fixName = ChronicleBundle.message("annotator.leading.at.unexpected.fix")
        val intention = myFixture.findSingleIntention(fixName)
        myFixture.launchAction(intention)
        myFixture.checkResult("""
            key = @[ v + 1 ]
        """.trimIndent())
    }

    // endregion
}
