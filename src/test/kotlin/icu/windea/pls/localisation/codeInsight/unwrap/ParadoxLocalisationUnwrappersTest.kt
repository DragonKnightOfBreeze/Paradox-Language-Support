package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapTestCase
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.lang.util.builders.ParadoxLocalisationTextBuilder.parameter as p

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
        val before = """
            l_english:
             <caret>text_key:0 "Value"
             another_key:0 "Another"
            """.trimIndent()
        val after = """
            l_english:
             another_key:0 "Another"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testIconRemover() {
        val before = """
            l_english:
             text_key:0 "Icon: <caret>£unity£ text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Icon:  text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testIconRemover_withParameter() {
        val before = """
            l_english:
             text_key:0 "Icon: <caret>£leader_skill|3£ text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Icon:  text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testCommandRemover() {
        val before = """
            l_english:
             text_key:0 "Command: <caret>[Root.Owner.GetName] text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Command:  text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testCommandRemover_scriptedLoc() {
        val before = """
            l_english:
             text_key:0 "Command: <caret>[some_scripted_loc] text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Command:  text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testReferenceRemover() {
        val before = """
            l_english:
             text_key:0 "Parameter: <caret>${p("KEY")} text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Parameter:  text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testReferenceRemover_withFormat() {
        val before = """
            l_english:
             text_key:0 "Parameter: <caret>${p("KEY", "Y")} text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Parameter:  text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testReferenceRemover_scriptedVariable() {
        val before = """
            l_english:
             text_key:0 "Variable: <caret>$@var$ text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Variable:  text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testColorfulTextRemover() {
        val before = """
            l_english:
             text_key:0 "Text: <caret>§RRed text§! more"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Text:  more"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testColorfulTextUnwrapper() {
        val before = """
            l_english:
             text_key:0 "Text: <caret>§RRed text§! more"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Text: Red text more"
            """.trimIndent()
        // 选择第二个选项（ColorfulTextUnwrapper）
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testColorfulTextUnwrapper_nested() {
        val before = """
            l_english:
             text_key:0 "Outer <caret>§ROuter §YInner§! text§! end"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Outer Outer §YInner§! text end"
            """.trimIndent()
        // 选择第二个选项（ColorfulTextUnwrapper）
        assertUnwrapped(before, after, 1)
    }
}
