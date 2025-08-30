package icu.windea.pls.cwt.editor

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle

@TestDataPath("\$CONTENT_ROOT/testData")
class CwtBasicAnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testAdjacentLiterals_errorAndFix() {
        val errorMsg = PlsBundle.message("neighboring.literal.not.supported")
        val openingMsg = PlsBundle.message("missing.opening.quote")
        myFixture.configureByText(
            "t_annotator_adjacent_literals.test.cwt",
            // a"b  -> two annotations expected:
            // 1) Missing opening quote on the first token 'a"' (unquoted string ending with a quote)
            // 2) Neighboring literal on the second token (content part only) 'b'
            """
            <error descr="$openingMsg">a"</error><error descr="$errorMsg">b</error>
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)

        // Apply quick-fix on a fresh file without markup
        val fixName = PlsBundle.message("neighboring.literal.not.supported.fix")
        myFixture.configureByText("t_annotator_adjacent_literals_apply.test.cwt", "a\"<caret>b")
        val intention = myFixture.findSingleIntention(fixName)
        myFixture.launchAction(intention)
        assertEquals("a\" b", myFixture.editor.document.text)
    }

    fun testMissingQuotes_errors() {
        val openingMsg = PlsBundle.message("missing.opening.quote")
        val closingMsg = PlsBundle.message("missing.closing.quote")
        myFixture.configureByText(
            "t_annotator_missing_quotes.test.cwt",
            // Two separate values to trigger missing opening / closing quote annotations
            // 1) value"  -> missing opening quote
            // 2) "value  -> missing closing quote
            """
            <error descr="$openingMsg">value"</error>
            <error descr="$closingMsg">"value</error>
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)
    }
}
