package icu.windea.pls.localisation

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationMoreSyntaxPsiTest : ParsingTestCase("yml/syntax", "syntax.yml", ParadoxLocalisationParserDefinition()) {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    fun test_advanced_combined() = doTest(true)
    fun test_empty() = doTest(true)
    fun test_error_unclosed_quote() = doTest(true)
    fun test_only_comments() = doTest(true)
    fun test_only_header() = doTest(true)
    fun test_text_formats_ck3() = doTest(true)
    fun test_text_formats_stellaris() = doTest(true)
    fun test_text_icon_vic3() = doTest(true)
}
