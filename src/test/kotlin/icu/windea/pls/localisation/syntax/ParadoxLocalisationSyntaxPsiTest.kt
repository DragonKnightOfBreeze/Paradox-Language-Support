package icu.windea.pls.localisation.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.localisation.ParadoxLocalisationParserDefinition
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ParadoxLocalisationSyntaxPsiTest : ParsingTestCase("localisation/syntax", "test.yml", ParadoxLocalisationParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    @Test
    fun code_settings() = doTest(true)
}
