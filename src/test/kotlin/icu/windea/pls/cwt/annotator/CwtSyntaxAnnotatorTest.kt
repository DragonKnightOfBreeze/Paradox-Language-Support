package icu.windea.pls.cwt.annotator

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.test.HighlightingTestScope
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see CwtSyntaxAnnotator
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtSyntaxAnnotatorTest : BasePlatformTestCase(), HighlightingTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testMissingQuotes_errors() {
        val openingTag = ChronicleBundle.message("message.missing.opening.quote").toErrorTag()
        val closingTag = ChronicleBundle.message("message.missing.closing.quote").toErrorTag()

        // 两个标注：value" 缺失开引号；"value 缺失闭引号
        myFixture.configureByText(
            "annotator_missing_quotes.test.cwt",
            """
            ${openingTag.start}value"${openingTag.end}
            ${closingTag.start}"value${closingTag.end}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)
    }
}
