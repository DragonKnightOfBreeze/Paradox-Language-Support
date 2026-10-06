package icu.windea.pls.lang.util

import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxParameterContextManager
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxParameterContextManagerTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region getArgumentTupleList

    @Test
    fun getArgumentTupleList_basic() {
        run {
            myFixture.configureByText("test.txt", """
                some_scripted_trigger = {
                    PARAM_1 = foo
                    PARAM_2 = <caret>123
                    PARAM_3 = 123.456
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val expected = listOf("PARAM_1" to "foo", "PARAM_2" to "123", "PARAM_3" to "123.456")
            val args = ParadoxParameterContextManager.getArguments(block)
            assertEquals(expected, args)
        }

        run {
            myFixture.configureByText("test.txt", """
                some_scripted_effect = {
                    VAR = @var
                    PARAM = ${"$"}PARAM$
                    NUM = @[ 1 + <caret>1 ]
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val expected = listOf("VAR" to "@var", "PARAM" to "\$PARAM$", "NUM" to "@[ 1 + 1 ]")
            val args = ParadoxParameterContextManager.getArguments(block)
            assertEquals(expected, args)
        }

        run {
            // Keep quotes of argument values
            myFixture.configureByText("test.txt", """
                inline_script = {
                    script = test/script
                    P1 = ${"$"}PARAM$
                    P2 = "${"$"}OTHER_PARAM$"
                    P3 = bar
                    P4 = <caret>yes
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val expected = listOf("P1" to "\$PARAM$", "P2" to "\"\$OTHER_PARAM$\"", "P3" to "bar", "P4" to "yes")
            val args = ParadoxParameterContextManager.getArguments(block, "script")
            assertEquals(expected, args)
        }

        run {
            // Accept only valid identifier characters (leading numbers are allowed)
            myFixture.configureByText("test.txt", """
                inline_script = {
                    script = test/other_script
                    NOT.VALID = v
                    "INVALID PARAM" = v
                    SKIP-IT = v
                    VALID_IDENTIFIER = <caret>v
                    00_INVALID_IDENTIFIER = v
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val expected = listOf("VALID_IDENTIFIER" to "v", "00_INVALID_IDENTIFIER" to "v")
            val args = ParadoxParameterContextManager.getArguments(block, "script")
            assertEquals(expected, args)
        }
    }

    // endregion
}
