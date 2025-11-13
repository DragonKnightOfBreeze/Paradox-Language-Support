package icu.windea.pls.cwt

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath

@TestDataPath("\$CONTENT_ROOT/testData")
class CwtMoreSyntaxPsiTest : ParsingTestCase("cwt/syntax", "test.cwt", CwtParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_advanced_nested() = doTest(true)
    fun test_empty() = doTest(true)
    fun test_only_comments() = doTest(true)
    fun test_template_expression() = doTest(true)
    fun test_error_unclosed_brace() = doTest(true)

    fun test_error_unclosed_quote() = doTest(true)

    fun test_option_comment_nested_eof() = doTest(true)
    fun test_option_values() = doTest(true)

    fun test_snippet_alias() = doTest(true)
    fun test_snippet_triggers_has_flag() = doTest(true)
}
