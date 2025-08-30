package icu.windea.pls.csv

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvMoreSyntaxPsiTest : ParsingTestCase("csv/syntax", "syntax.csv", ParadoxCsvParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_advanced_quoted() = doTest(true)
    fun test_empty() = doTest(true)
    fun test_empty_columns() = doTest(true)
    fun test_error_unclosed_quote() = doTest(true)
    fun test_header_and_rows() = doTest(true)
    fun test_header_only() = doTest(true)
    fun test_only_comments() = doTest(true)
}
