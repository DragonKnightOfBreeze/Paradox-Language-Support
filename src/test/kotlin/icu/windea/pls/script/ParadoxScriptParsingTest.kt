package icu.windea.pls.script

import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.test.ChronicleTestScope
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
    fun empty() = doTest(true)
    @Test
    fun escapes() = doTest(true)
    @Test
    fun only_comments() = doTest(true)
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
    fun construct_expressions() = doTest(true)
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
    fun error_missing_property_value() = doTest(true)
    @Test
    fun error_unclosed_braces() = doTest(true)
    @Test
    fun error_unclosed_quotes() = doTest(true)
}
