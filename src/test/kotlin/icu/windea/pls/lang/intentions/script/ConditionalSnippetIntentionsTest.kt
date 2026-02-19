package icu.windea.pls.lang.intentions.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.lang.util.builders.ParadoxScriptTextBuilder.parameter as p

/**
 * @see ConditionalSnippetToPropertyFormatIntention
 * @see ConditionalSnippetToBlockFormatIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ConditionalSnippetIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testConditionalSnippetToPropertyFormat_basic() {
        val intentionName = PlsBundle.message("intention.conditionalSnippetToPropertyFormat")
        myFixture.configureByText(
            "conditional_snippet_to_property_format_basic.test.txt",
            "k = { [[PARAM] <caret>PARAM = ${p("PARAM")} ] }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { PARAM = ${p("PARAM", "no")} }")
    }

    @Test
    fun testConditionalSnippetToPropertyFormat_multiline() {
        val intentionName = PlsBundle.message("intention.conditionalSnippetToPropertyFormat")
        myFixture.configureByText(
            "conditional_snippet_to_property_format_multiline.test.txt",
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
    fun testConditionalSnippetToPropertyFormat_notAvailableWhenAlreadyPropertyFormat() {
        val intentionName = PlsBundle.message("intention.conditionalSnippetToPropertyFormat")
        myFixture.configureByText(
            "conditional_snippet_to_property_format_not_available_property_format.test.txt",
            "k = { <caret>PARAM = ${p("PARAM", "no")}}"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testConditionalSnippetToPropertyFormat_notAvailableWhenMismatchParameterName() {
        val intentionName = PlsBundle.message("intention.conditionalSnippetToPropertyFormat")
        myFixture.configureByText(
            "conditional_snippet_to_property_format_not_available_mismatch.test.txt",
            "k = { [[PARAM] <caret>OTHER = ${p("PARAM")} ] }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testConditionalSnippetToBlockFormat_basic() {
        val intentionName = PlsBundle.message("intention.conditionalSnippetToBlockFormat")
        myFixture.configureByText(
            "conditional_snippet_to_block_format_basic.test.txt",
            "k = { <caret>PARAM = ${p("PARAM", "no")}}"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { [[PARAM] PARAM = ${p("PARAM")} ] }")
    }

    @Test
    fun testConditionalSnippetToBlockFormat_parameterNameWithUnderscore() {
        val intentionName = PlsBundle.message("intention.conditionalSnippetToBlockFormat")
        myFixture.configureByText(
            "conditional_snippet_to_block_format_underscore.test.txt",
            "k = { <caret>PARAM_1 = ${p("PARAM_1", "no")} }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { [[PARAM_1] PARAM_1 = ${p("PARAM_1")} ] }")
    }

    @Test
    fun testConditionalSnippetToBlockFormat_notAvailableWhenAlreadyBlockFormat() {
        val intentionName = PlsBundle.message("intention.conditionalSnippetToBlockFormat")
        myFixture.configureByText(
            "conditional_snippet_to_block_format_not_available_block_format.test.txt",
            "k = { [[PARAM] <caret>PARAM = ${p("PARAM")} ] }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testConditionalSnippetToBlockFormat_notAvailableWhenMismatchParameterName() {
        val intentionName = PlsBundle.message("intention.conditionalSnippetToBlockFormat")
        myFixture.configureByText(
            "conditional_snippet_to_block_format_not_available_mismatch.test.txt",
            "k = { <caret>PARAM = ${p("OTHER", "no")} }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }
}
