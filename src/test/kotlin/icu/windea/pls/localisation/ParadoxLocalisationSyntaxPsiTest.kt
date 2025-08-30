package icu.windea.pls.localisation

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.csv.ParadoxCsvParserDefinition
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationSyntaxPsiTest : ParsingTestCase("yml/syntax", "syntax.yml", ParadoxLocalisationParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_code_settings() = doTest(true)
}
