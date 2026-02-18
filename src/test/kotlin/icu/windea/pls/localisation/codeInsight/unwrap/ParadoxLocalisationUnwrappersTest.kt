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
 * @see ParadoxLocalisationParameterRemover
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

    // region ParadoxLocalisationPropertyRemover

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
    fun testPropertyRemover_multipleProperties() {
        val before = """
            l_english:
             <caret>text_key1:0 "Value1"
             text_key2:0 "Value2"
             text_key3:0 "Value3"
            """.trimIndent()
        val after = """
            l_english:
             text_key2:0 "Value2"
             text_key3:0 "Value3"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxLocalisationIconRemover

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
    fun testIconRemover_multipleIcons() {
        val before = """
            l_english:
             text_key:0 "Icon: <caret>£unity£ £food£ text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Icon:  £food£ text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testIconRemover_complexText() {
        val before = """
            l_english:
             text_key:0 "Resource: <caret>£energy£ [Root.GetEnergy] ${p("AMOUNT")}"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Resource:  [Root.GetEnergy] ${p("AMOUNT")}"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxLocalisationCommandRemover

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
    fun testCommandRemover_multipleCommands() {
        val before = """
            l_english:
             text_key:0 "Commands: <caret>[Root.GetName] [This.GetName] text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Commands:  [This.GetName] text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testCommandRemover_withComplexExpression() {
        val before = """
            l_english:
             text_key:0 "Command: <caret>[Root.event_target:some_target.GetName] text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Command:  text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxLocalisationParameterRemover

    @Test
    fun testParameterRemover() {
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
    fun testParameterRemover_withFormat() {
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
    fun testParameterRemover_scriptedVariable() {
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
    fun testParameterRemover_multipleReferences() {
        val before = """
            l_english:
             text_key:0 "Params: <caret>${p("KEY1")} ${p("KEY2")} text"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Params:  ${p("KEY2")} text"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxLocalisationColorfulTextRemover

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
    fun testColorfulTextRemover_multiple() {
        val before = """
            l_english:
             text_key:0 "Text: <caret>§RRed§! §GGreen§! more"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Text:  §GGreen§! more"
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxLocalisationColorfulTextUnwrapper

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

    @Test
    fun testColorfulTextUnwrapper_emptyContent() {
        val before = """
            l_english:
             text_key:0 "Text: <caret>§R§! more"
            """.trimIndent()
        val after = """
            l_english:
             text_key:0 "Text:  more"
            """.trimIndent()
        assertUnwrapped(before, after, 1)
    }

    // endregion
}
