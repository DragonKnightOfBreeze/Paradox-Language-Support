package icu.windea.pls.csv.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.csv.ParadoxCsvParserDefinition
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ParadoxCsvMoreSyntaxPsiTest : ParsingTestCase("csv/syntax", "test.csv", ParadoxCsvParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    @Test
    fun advanced_quoted() = doTest(true)
    @Test
    fun empty() = doTest(true)
    @Test
    fun empty_columns() = doTest(true)
    @Test
    fun error_unclosed_quote() = doTest(true)
    @Test
    fun header_and_rows() = doTest(true)
    @Test
    fun header_only() = doTest(true)
    @Test
    fun only_comments() = doTest(true)

    @Test
    fun header_and_rows_crlf() = doTest(true)
    @Test
    fun quoted_with_newline() = doTest(true)
    @Test
    fun header_trailing_separator() = doTest(true)
    @Test
    fun unterminated_quote_no_eol() = doTest(true)
}
