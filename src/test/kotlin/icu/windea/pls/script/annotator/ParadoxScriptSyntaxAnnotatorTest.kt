package icu.windea.pls.script.annotator

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.test.HighlightingTestScope
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
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
class ParadoxScriptSyntaxAnnotatorTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testMissingQuotes_errors() {
        val openingMsg = PlsBundle.message("message.missing.opening.quote")
        val closingMsg = PlsBundle.message("message.missing.closing.quote")
        val openingMsgTag = openingMsg.toErrorTag()
        val closingMsgTag = closingMsg.toErrorTag()

        // 两个标注：value" 缺失开引号；"value 缺失闭引号
        myFixture.configureByText(
            "annotator_missing_quotes.test.txt",
            """
            ${openingMsgTag.start}value"${openingMsgTag.end}
            ${closingMsgTag.start}"value${closingMsgTag.end}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun testOperator() {
        val msg = PlsBundle.message("message.leading.blank.unexpected.1")
        val msgTag = msg.toErrorTag()

        myFixture.configureByText(
            "annotator_operator.test.txt",
            """
            key? =value
            key${msgTag.start} ${msgTag.end}? =value
            key? = value
            key${msgTag.start} ${msgTag.end}? = value
            key?=value
            key ?=value
            key?= value
            key ?= value
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun testInlineMathScriptedVariableReference() {
        val msg = PlsBundle.message("message.leading.at.unexpected.1")
        val msgTag = msg.toErrorTag()

        myFixture.configureByText(
            "annotator_inline_math_scripted_variable_reference.test.txt",
            """
            key = @[ v + 1 ]
            key = @[ ${msgTag.start}@${msgTag.end}v + 1 ]
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)
    }
}
