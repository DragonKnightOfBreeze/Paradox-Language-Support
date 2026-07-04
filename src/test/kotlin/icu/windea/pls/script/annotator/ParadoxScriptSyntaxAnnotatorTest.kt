package icu.windea.pls.script.annotator

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
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
        val openingTag = ChronicleBundle.message("message.missing.opening.quote").toErrorTag()
        val closingTag = ChronicleBundle.message("message.missing.closing.quote").toErrorTag()

        // 两个标注：value" 缺失开引号；"value 缺失闭引号
        myFixture.configureByText(
            "annotator_missing_quotes.test.txt",
            """
            ${openingTag.start}value"${openingTag.end}
            ${closingTag.start}"value${closingTag.end}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun testOperator() {
        val tag = ChronicleBundle.message("message.leading.blank.unexpected.1").toErrorTag()

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
        myFixture.checkHighlighting(true, true, true)
    }

    @Test
    fun testInlineMathScriptedVariableReference() {
        val tag = ChronicleBundle.message("message.leading.at.unexpected.1").toErrorTag()

        myFixture.configureByText(
            "annotator_inline_math_scripted_variable_reference.test.txt",
            """
            key = @[ v + 1 ]
            key = @[ ${tag.start}@${tag.end}v + 1 ]
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)
    }
}
