package icu.windea.pls.script.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.script.ParadoxScriptParserDefinition
import icu.windea.pls.test.markIntegrationTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ParadoxScriptSyntaxPsiTest  : ParsingTestCase("script/syntax", "test.txt", ParadoxScriptParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    @Before
    fun setup() = markIntegrationTest()

    @Test
    fun code_settings() = doTest(true)

    @Test
    fun advanced_nested() = doTest(true)
    @Test
    fun attached_comments() = doTest(true)
    @Test
    fun empty() = doTest(true)
    @Test
    fun escapes() = doTest(true)
    @Test
    fun only_comments() = doTest(true)
    @Test
    fun mixed_members() = doTest(true)
    @Test
    fun error_missing_property_value() = doTest(true)
    @Test
    fun error_unclosed_brace() = doTest(true)

    @Test
    fun property_separators() = doTest(true)
    @Test
    fun inline_math_mismatch() = doTest(true)
    @Test
    fun parameter_condition_nested() = doTest(true)
    @Test
    fun unterminated_quoted_string_value() = doTest(true)
}
