package icu.windea.pls.cwt.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.cwt.CwtParserDefinition
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("/testData")
class CwtMoreSyntaxPsiTest : ParsingTestCase("cwt/syntax", "test.cwt", CwtParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    @Test
    fun advanced_nested() = doTest(true)
    @Test
    fun empty() = doTest(true)
    @Test
    fun only_comments() = doTest(true)
    @Test
    fun template_expression() = doTest(true)
    @Test
    fun error_unclosed_brace() = doTest(true)
    @Test
    fun error_unclosed_quote() = doTest(true)
    @Test
    fun option_comment_nested_eof() = doTest(true)
    @Test
    fun option_values() = doTest(true)

    @Test
    fun snippet_alias() = doTest(true)
    @Test
    fun snippet_triggers_has_flag() = doTest(true)
}
