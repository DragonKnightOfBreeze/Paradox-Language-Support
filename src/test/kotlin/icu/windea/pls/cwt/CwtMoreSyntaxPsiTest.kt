package icu.windea.pls.cwt

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath

@TestDataPath("\$CONTENT_ROOT/testData")
class CwtMoreSyntaxPsiTest : ParsingTestCase("cwt/syntax", "syntax.cwt", CwtParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_advanced_nested() = doTest(true)
    fun test_empty() = doTest(true)
    fun test_extended_configs() = doTest(true)
    fun test_only_comments() = doTest(true)
    fun test_template_expression() = doTest(true)
    fun test_unclosed_brace() = doTest(true)
}
