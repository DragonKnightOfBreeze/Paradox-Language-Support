package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapTestCase
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Paradox Localisation Unwrappers 测试。
 *
 * @see ParadoxLocalisationUnwrapDescriptor
 * @see ParadoxLocalisationUnwrapper
 * @see ParadoxLocalisationPropertyRemover
 * @see ParadoxLocalisationIconRemover
 * @see ParadoxLocalisationCommandRemover
 * @see ParadoxLocalisationReferenceRemover
 * @see ParadoxLocalisationColorfulTextRemover
 * @see ParadoxLocalisationColorfulTextUnwrapper
 */
@RunWith(JUnit4::class)
class ParadoxLocalisationUnwrappersTest : UnwrapTestCase() {
    override fun getFileNameToCreate() = "test.yml"

    override fun createCode(code: String) = code

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testPropertyRemover() {
        assertUnwrapped(
            """
            l_english:
             <caret>text_key:0 "Value"
             another_key:0 "Another"
            """.trimIndent(),
            """
            l_english:
             another_key:0 "Another"
            """.trimIndent()
        )
    }

    @Test
    fun testIconRemover() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Icon: <caret>£unity£ text"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Icon:  text"
            """.trimIndent()
        )
    }

    @Test
    fun testIconRemover_withParameter() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Icon: <caret>£leader_skill|3£ text"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Icon:  text"
            """.trimIndent()
        )
    }

    @Test
    fun testCommandRemover() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Command: <caret>[Root.Owner.GetName] text"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Command:  text"
            """.trimIndent()
        )
    }

    @Test
    fun testCommandRemover_scriptedLoc() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Command: <caret>[some_scripted_loc] text"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Command:  text"
            """.trimIndent()
        )
    }

    @Test
    fun testReferenceRemover() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Parameter: <caret>${'$'}KEY${'$'} text"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Parameter:  text"
            """.trimIndent()
        )
    }

    @Test
    fun testReferenceRemover_withFormat() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Parameter: <caret>${'$'}KEY|Y${'$'} text"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Parameter:  text"
            """.trimIndent()
        )
    }

    @Test
    fun testReferenceRemover_scriptedVariable() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Variable: <caret>${'$'}@var${'$'} text"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Variable:  text"
            """.trimIndent()
        )
    }

    @Test
    fun testColorfulTextRemover() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Text: <caret>§RRed text§! more"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Text:  more"
            """.trimIndent()
        )
    }

    @Test
    fun testColorfulTextUnwrapper() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Text: <caret>§RRed text§! more"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Text: Red text more"
            """.trimIndent(),
            1 // 选择第二个选项（ColorfulTextUnwrapper）
        )
    }

    @Test
    fun testColorfulTextUnwrapper_nested() {
        assertUnwrapped(
            """
            l_english:
             text_key:0 "Outer <caret>§ROuter §YInner§! text§! end"
            """.trimIndent(),
            """
            l_english:
             text_key:0 "Outer Outer §YInner§! text end"
            """.trimIndent(),
            1 // 选择第二个选项（ColorfulTextUnwrapper）
        )
    }
}
