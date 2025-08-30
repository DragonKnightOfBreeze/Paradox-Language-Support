package icu.windea.pls.script

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptMoreSyntaxPsiTest : ParsingTestCase("script/syntax", "syntax.script", ParadoxScriptParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_advanced_nested() = doTest(true)
    fun test_attached_comments() = doTest(true)
    fun test_empty() = doTest(true)
    fun test_only_comments() = doTest(true)
    fun test_unclosed_brace() = doTest(true)
}
