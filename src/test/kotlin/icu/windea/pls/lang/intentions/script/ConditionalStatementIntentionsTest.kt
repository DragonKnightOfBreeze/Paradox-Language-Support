package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.lang.text.ParadoxScriptTextBuilder.parameter as p

/**
 * @see ConditionalStatementToPropertyFormIntention
 * @see ConditionalStatementToBlockFormIntention
 * @see ParadoxConditionalStatementManipulationService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ConditionalStatementIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region conditionalStatementToPropertyForm

    @Test
    fun testConditionalStatementToPropertyForm_basic() {
        val intentionName = PlsBundle.message("intention.conditionalStatementToPropertyForm")
        myFixture.configureByText(
            "conditional_statement_to_property_form_basic.test.txt",
            "k = { [[PARAM] <caret>PARAM = ${p("PARAM")} ] }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { PARAM = ${p("PARAM", "no")} }")
    }

    @Test
    fun testConditionalStatementToPropertyForm_multiline() {
        val intentionName = PlsBundle.message("intention.conditionalStatementToPropertyForm")
        myFixture.configureByText(
            "conditional_statement_to_property_form_multiline.test.txt",
            """
            k = {
                [[PARAM]
                    <caret>PARAM = ${p("PARAM")}
                ]
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            k = {
                PARAM = ${p("PARAM", "no")}
            }
            """.trimIndent()
        )
    }

    @Test
    fun testConditionalStatementToPropertyForm_notAvailableWhenAlreadyPropertyForm() {
        val intentionName = PlsBundle.message("intention.conditionalStatementToPropertyForm")
        myFixture.configureByText(
            "conditional_statement_to_property_form_not_available_property_form.test.txt",
            "k = { <caret>PARAM = ${p("PARAM", "no")}}"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testConditionalStatementToPropertyForm_notAvailableWhenMismatchParameterName() {
        val intentionName = PlsBundle.message("intention.conditionalStatementToPropertyForm")
        myFixture.configureByText(
            "conditional_statement_to_property_form_not_available_mismatch.test.txt",
            "k = { [[PARAM] <caret>OTHER = ${p("PARAM")} ] }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion

    // region conditionalStatementToBlockForm

    @Test
    fun testConditionalStatementToBlockForm_basic() {
        val intentionName = PlsBundle.message("intention.conditionalStatementToBlockForm")
        myFixture.configureByText(
            "conditional_statement_to_block_form_basic.test.txt",
            "k = { <caret>PARAM = ${p("PARAM", "no")}}"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { [[PARAM] PARAM = ${p("PARAM")} ] }")
    }

    @Test
    fun testConditionalStatementToBlockForm_parameterNameWithUnderscore() {
        val intentionName = PlsBundle.message("intention.conditionalStatementToBlockForm")
        myFixture.configureByText(
            "conditional_statement_to_block_form_underscore.test.txt",
            "k = { <caret>PARAM_1 = ${p("PARAM_1", "no")} }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { [[PARAM_1] PARAM_1 = ${p("PARAM_1")} ] }")
    }

    @Test
    fun testConditionalStatementToBlockForm_notAvailableWhenAlreadyBlockForm() {
        val intentionName = PlsBundle.message("intention.conditionalStatementToBlockForm")
        myFixture.configureByText(
            "conditional_statement_to_block_form_not_available_block_form.test.txt",
            "k = { [[PARAM] <caret>PARAM = ${p("PARAM")} ] }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testConditionalStatementToBlockForm_notAvailableWhenMismatchParameterName() {
        val intentionName = PlsBundle.message("intention.conditionalStatementToBlockForm")
        myFixture.configureByText(
            "conditional_statement_to_block_form_not_available_mismatch.test.txt",
            "k = { <caret>PARAM = ${p("OTHER", "no")} }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // endregion
}
