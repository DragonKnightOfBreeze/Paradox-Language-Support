package icu.windea.pls.localisation.syntax

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.localisation.ParadoxLocalisationParserDefinition
import icu.windea.pls.test.markIntegrationTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("/testData")
class ParadoxLocalisationSyntaxPsiTest : ParsingTestCase("localisation/syntax", "test.yml", ParadoxLocalisationParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    @Before
    fun setup() = markIntegrationTest()

    @Test
    fun example() = doTest(true)

    @Test
    fun advanced_combined() = doTest(true)
    @Test
    fun empty() = doTest(true)
    @Test
    fun escapes() = doTest(true)
    @Test
    fun error_unclosed_quote() = doTest(true)
    @Test
    fun only_comments() = doTest(true)
    @Test
    fun only_header() = doTest(true)
    @Test
    fun text_formats_ck3() = doTest(true)
    @Test
    fun text_formats_stellaris() = doTest(true)
    @Test
    fun text_icons_vic3() = doTest(true)

    // Added tests for header detection and trailing comment after value
    @Test
    fun only_header_eof() = doTest(true)
    @Test
    fun header_as_key() = doTest(true)
    @Test
    fun value_trailing_comment() = doTest(true)
    @Test
    fun value_multi_quotes_trailing_comment() = doTest(true)
    @Test
    fun multiple_headers() = doTest(true)
    @Test
    fun header_with_trailing_spaces() = doTest(true)
}
