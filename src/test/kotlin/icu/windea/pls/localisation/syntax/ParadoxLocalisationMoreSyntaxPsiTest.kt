package icu.windea.pls.localisation.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.localisation.ParadoxLocalisationParserDefinition

@TestDataPath("/testData")
class ParadoxLocalisationMoreSyntaxPsiTest : ParsingTestCase("localisation/syntax", "test.yml", ParadoxLocalisationParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_advanced_combined() = doTest(true)
    fun test_empty() = doTest(true)
    fun test_error_unclosed_quote() = doTest(true)
    fun test_only_comments() = doTest(true)
    fun test_only_header() = doTest(true)
    fun test_text_formats_ck3() = doTest(true)
    fun test_text_formats_stellaris() = doTest(true)
    fun test_text_icons_vic3() = doTest(true)

    // Added tests for header detection and trailing comment after value
    fun test_only_header_eof() = doTest(true)
    fun test_header_as_key() = doTest(true)
    fun test_value_trailing_comment() = doTest(true)
    fun test_value_multi_quotes_trailing_comment() = doTest(true)

    fun test_multiple_headers() = doTest(true)
    fun test_header_with_trailing_spaces() = doTest(true)
}
