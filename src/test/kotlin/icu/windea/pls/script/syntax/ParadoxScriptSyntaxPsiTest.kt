package icu.windea.pls.script.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.script.ParadoxScriptParserDefinition
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ParadoxScriptSyntaxPsiTest  : ParsingTestCase("script/syntax", "test.txt", ParadoxScriptParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    @Test
    fun code_settings() = doTest(true)
}
