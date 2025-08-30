package icu.windea.pls.cwt

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.csv.ParadoxCsvParserDefinition
import icu.windea.pls.cwt.psi.CwtFile
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class CwtSyntaxPsiTest : ParsingTestCase("cwt/syntax", "syntax.cwt", CwtParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_code_settings() = doTest(true)
}
