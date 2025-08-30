package icu.windea.pls.script.editor

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptBasicAnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    // TODO 2.0.2-dev 澄清：由于 ParadoxScriptLexer 中会对 STRING_TOKEN 等进行合并，这里的代码并不能起效（计划以后重构，目前不视为语法性错误）
    // fun testAdjacentLiterals_errorAndFix() {
    //     val errorMsg = PlsBundle.message("neighboring.literal.not.supported")
    //     val openingMsg = PlsBundle.message("missing.opening.quote")
    //     myFixture.configureByText(
    //         "annotator_adjacent_literals.test.txt",
    //         // a"b  -> 两个标注：1) a" 缺失开引号；2) b 紧邻字面量
    //         """
    //         <error descr="$openingMsg">a"</error><error descr="$errorMsg">b</error>
    //         """.trimIndent()
    //     )
    //     myFixture.checkHighlighting(true, true, true)
    //
    //     // Quick Fix: 插入空格
    //     val fixName = PlsBundle.message("neighboring.literal.not.supported.fix")
    //     myFixture.configureByText("annotator_adjacent_literals_apply.test.txt", "a\"<caret>b")
    //     val intention = myFixture.findSingleIntention(fixName)
    //     myFixture.launchAction(intention)
    //     assertEquals("a\" b", myFixture.editor.document.text)
    // }

    fun testMissingQuotes_errors() {
        val openingMsg = PlsBundle.message("missing.opening.quote")
        val closingMsg = PlsBundle.message("missing.closing.quote")
        myFixture.configureByText(
            "annotator_missing_quotes.test.txt",
            // 两个标注：value" 缺失开引号；"value 缺失闭引号
            """
            <error descr="$openingMsg">value"</error>
            <error descr="$closingMsg">"value</error>
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, true, true)
    }
}
