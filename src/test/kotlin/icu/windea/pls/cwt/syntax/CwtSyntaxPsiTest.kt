package icu.windea.pls.cwt.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.cwt.CwtParserDefinition

@TestDataPath("/testData")
class CwtSyntaxPsiTest : ParsingTestCase("cwt/syntax", "test.cwt", CwtParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_code_settings() = doTest(true)
}
