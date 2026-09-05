package icu.windea.pls.script

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.test.ChronicleTestScope
import org.jetbrains.kotlin.nj2k.downToExpression
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxScriptParserDefinition
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptParsingTest : ParsingTestCase("script/syntax", "test.txt", ParadoxScriptParserDefinition()), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    override fun includeRanges() = true

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun example() = doTest(true)
    @Test
    fun escapes() = doTest(true)
    @Test
    fun property_separators() = doTest(true)
    @Test
    fun mixed_members() = doTest(true)
    @Test
    fun nested() = doTest(true)
    @Test
    fun attached_comments() = doTest(true)

    @Test
    fun construct_boolean_literals() = doTest(true)
    @Test
    fun construct_number_literals() = doTest(true)
    @Test
    fun construct_properties() = doTest(true)
    @Test
    fun construct_values() = doTest(true)
    @Test
    fun construct_scripted_variables() = doTest(true)
    @Test
    fun construct_colors() = doTest(true)
    @Test
    fun construct_inline_maths() = doTest(true)
    @Test
    fun construct_parameters() = doTest(true)
    @Test
    fun construct_conditional_blocks() = doTest(true)
    @Test
    fun construct_inline_conditional_blocks() = doTest(true)
    @Test
    fun construct_interpolations() = doTest(true) // 3.0.2
    @Test
    fun construct_complex_interpolations() = doTest(true) // 3.0.2

    @Test
    fun edge_empty() = doTest(true)
    @Test
    fun edge_only_comments() = doTest(true)
    @Test
    fun edge_one_comment() = doTest(true)
    @Test
    fun edge_one_property() = doTest(true)
    @Test
    fun edge_one_value() = doTest(true)
    @Test
    fun edge_one_block() = doTest(true)
    @Test
    fun edge_one_comment_and_one_member() = doTest(true)
    @Test
    fun edge_one_scripted_variable() = doTest(true)
    @Test
    fun edge_one_color() = doTest(true)
    @Test
    fun edge_one_inline_math() = doTest(true)
    @Test
    fun edge_one_parameter() = doTest(true)
    @Test
    fun edge_one_conditional_block() = doTest(true)
    @Test
    fun edge_one_complex_conditional_block() = doTest(true)
    @Test
    fun edge_chars_of_scripted_variables() = doTest(true)
    @Test
    fun edge_chars_of_scripted_variable_references() = doTest(true)
    @Test
    fun edge_chars_of_inline_math_scripted_variable_references() = doTest(true)
    @Test
    fun edge_chars_of_parameters() = doTest(true)
    @Test
    fun edge_chars_of_inline_math_parameters() = doTest(true)

    @Test
    fun error_dangling_at_sign() = doTest(true)
    @Test
    fun error_missing_property_value() = doTest(true)
    @Test
    fun error_unclosed_quotes() = doTest(true)
    @Test
    fun error_unclosed_braces() = doTest(true)
    @Test
    fun error_unclosed_braces_eof() = doTest(true)
}
