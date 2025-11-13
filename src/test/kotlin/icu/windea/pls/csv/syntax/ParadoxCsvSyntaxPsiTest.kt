package icu.windea.pls.csv.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.csv.ParadoxCsvParserDefinition

@TestDataPath("/testData")
class ParadoxCsvSyntaxPsiTest : ParsingTestCase("csv/syntax", "test.csv", ParadoxCsvParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_code_settings() = doTest(true)
}
