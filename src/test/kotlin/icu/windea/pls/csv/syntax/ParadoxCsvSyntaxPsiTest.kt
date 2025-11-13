package icu.windea.pls.csv.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.csv.ParadoxCsvParserDefinition
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ParadoxCsvSyntaxPsiTest : ParsingTestCase("csv/syntax", "test.csv", ParadoxCsvParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    @Test
    fun code_settings() = doTest(true)
}
