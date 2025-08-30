package icu.windea.pls.script

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.csv.ParadoxCsvParserDefinition
import icu.windea.pls.script.psi.ParadoxScriptFile
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptSyntaxPsiTest  : ParsingTestCase("script/syntax", "syntax.script", ParadoxScriptParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_code_settings() = doTest(true)
}
