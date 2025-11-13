package icu.windea.pls.csv.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.csv.ParadoxCsvParserDefinition

@TestDataPath("/testData")
class ParadoxCsvMoreSyntaxPsiTest : ParsingTestCase("csv/syntax", "test.csv", ParadoxCsvParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_advanced_quoted() = doTest(true)
    fun test_empty() = doTest(true)
    fun test_empty_columns() = doTest(true)
    fun test_error_unclosed_quote() = doTest(true)
    fun test_header_and_rows() = doTest(true)
    fun test_header_only() = doTest(true)
    fun test_only_comments() = doTest(true)

    fun test_header_and_rows_crlf() = doTest(true)
    fun test_quoted_with_newline() = doTest(true)
    fun test_header_trailing_separator() = doTest(true)
    fun test_unterminated_quote_no_eol() = doTest(true)
}
