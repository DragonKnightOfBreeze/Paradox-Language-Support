package icu.windea.pls.script

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptMoreSyntaxPsiTest : ParsingTestCase("script/syntax", "test.txt", ParadoxScriptParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_advanced_nested() = doTest(true)
    fun test_attached_comments() = doTest(true)
    fun test_empty() = doTest(true)
    fun test_only_comments() = doTest(true)
    fun test_error_unclosed_brace() = doTest(true)

    fun test_property_separators() = doTest(true)
    fun test_inline_math_mismatch() = doTest(true)
    fun test_parameter_condition_nested() = doTest(true)
    fun test_unterminated_quoted_string_value() = doTest(true)
}
