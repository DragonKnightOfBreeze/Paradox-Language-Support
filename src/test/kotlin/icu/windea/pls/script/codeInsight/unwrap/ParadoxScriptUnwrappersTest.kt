package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Paradox Script Unwrappers 测试。
 *
 * @see ParadoxScriptUnwrapDescriptor
 * @see ParadoxScriptUnwrapper
 * @see ParadoxScriptScriptedVariableRemover
 * @see ParadoxScriptPropertyRemover
 * @see ParadoxScriptValueRemover
 * @see ParadoxScriptParameterConditionRemover
 * @see ParadoxScriptInlineParameterConditionRemover
 * @see ParadoxScriptPropertyUnwrapper
 * @see ParadoxScriptBlockUnwrapper
 * @see ParadoxScriptParameterConditionUnwrapper
 * @see ParadoxScriptInlineParameterConditionUnwrapper
 */
@RunWith(JUnit4::class)
class ParadoxScriptUnwrappersTest : UnwrapTestCase() {
    override fun getFileNameToCreate() = "test.txt"

    override fun createCode(code: String) = code

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testScriptedVariableRemover() {
        val before = """
            <caret>@foo = 1
            bar = baz
            """.trimIndent()
        val after = """
            bar = baz
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testPropertyRemover() {
        val before = """
            <caret>foo = bar
            baz = qux
            """.trimIndent()
        val after = """
            baz = qux
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testPropertyRemover_nested() {
        val before = """
            nested = {
                <caret>foo = bar
                baz = qux
            }
            """.trimIndent()
        val after = """
            nested = {
                baz = qux
            }
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testValueRemover() {
        val before = """
            <caret>value
            baz = qux
            """.trimIndent()
        val after = """
            baz = qux
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testValueRemover_forBlock() {
        val before = """
            <caret>{ value }
            foo = bar
            """.trimIndent()
        val after = """
            foo = bar
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testValueRemover_forPropertyValue_notAllowed() {
        val before = """
            key = <caret>value
            baz = qux
            """.trimIndent()
        assertOptions(before, PlsBundle.message("script.remove.property", "key"))
    }

    @Test
    fun testParameterConditionRemover() {
        val before = """
            <caret>[[P]
                foo = bar
            ]
            baz = qux
            """.trimIndent()
        val after = """
            baz = qux
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testInlineParameterConditionRemover() {
        val before = "key = a<caret>[[b]c]d"
        val after = "key = ad"
        val option1 = PlsBundle.message("script.remove.inlineParameterCondition", PlsStrings.parameterConditionFolder("b"))
        val option2 = PlsBundle.message("script.unwrap.inlineParameterCondition", PlsStrings.parameterConditionFolder("b"))
        val option3 = PlsBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after)
    }

    @Test
    fun testPropertyUnwrapper() {
        val before = """
            <caret>foo = {
                bar = baz
                qux = quux
            }
            """.trimIndent()
        val after = """
            bar = baz
            qux = quux
            """.trimIndent()
        val option1 = PlsBundle.message("script.remove.property", "foo")
        val option2 = PlsBundle.message("script.unwrap.property", "foo")
        assertOptions(before, option1, option2)
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testPropertyUnwrapper_nested() {
        val before = """
            outer = {
                <caret>inner = {
                    foo = bar
                }
            }
            """.trimIndent()
        val after = """
            outer = {
                foo = bar
            }
            """.trimIndent()
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testBlockUnwrapper() {
        val before = """
            <caret>{
                bar = baz
            }
            """.trimIndent()
        val after = """
            bar = baz
            """.trimIndent()
        val option1 = PlsBundle.message("script.remove.block")
        val option2 = PlsBundle.message("script.unwrap.block")
        assertOptions(before, option1, option2)
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testParameterConditionUnwrapper() {
        val before = """
            <caret>[[P]
                foo = bar
            ]
            """.trimIndent()
        val after = """
            foo = bar
            """.trimIndent()
        val option1 = PlsBundle.message("script.remove.parameterCondition", PlsStrings.parameterConditionFolder("P"))
        val option2 = PlsBundle.message("script.unwrap.parameterCondition", PlsStrings.parameterConditionFolder("P"))
        assertOptions(before, option1, option2)
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testParameterConditionUnwrapper_nested() {
        val before = """
            root = {
                outer = {
                    <caret>[[P]
                        foo = bar
                    ]
                }
            }
            """.trimIndent()
        val after = """
            root = {
                outer = {
                    foo = bar
                }
            }
            """.trimIndent()
        assertUnwrapped(before, after, 1)
    }

    // TODO 2.1.3 存在问题，与预期不符，待修复
    // @Test
    // fun testInlineParameterConditionUnwrapper() {
    //     val before = "key = a<caret>[[b]c]d"
    //     val after = "key = acd"
    //     val option1 = PlsBundle.message("script.remove.inlineParameterCondition", PlsStrings.parameterConditionFolder("b"))
    //     val option2 = PlsBundle.message("script.unwrap.inlineParameterCondition", PlsStrings.parameterConditionFolder("b"))
    //     val option3 = PlsBundle.message("script.remove.property", "key")
    //     assertOptions(before, option1, option2, option3)
    //     assertUnwrapped(before, after, 1)
    // }
}
