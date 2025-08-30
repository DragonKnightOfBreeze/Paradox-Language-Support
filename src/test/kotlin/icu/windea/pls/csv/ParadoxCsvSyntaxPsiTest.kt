package icu.windea.pls.csv

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvSyntaxPsiTest : ParsingTestCase("csv/syntax", "syntax.csv", ParadoxCsvParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_code_settings() = doTest(true)
}
