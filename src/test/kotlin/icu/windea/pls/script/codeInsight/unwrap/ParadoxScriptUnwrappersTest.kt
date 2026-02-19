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
import icu.windea.pls.lang.util.builders.ParadoxScriptTextBuilder.parameter as p

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

    // region ParadoxScriptScriptedVariableRemover

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
    fun testScriptedVariableRemover_nested() {
        val before = """
            root = {
                <caret>@foo = 1
                bar = baz
            }
            """.trimIndent()
        val after = """
            root = {
                bar = baz
            }
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxScriptPropertyRemover

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
    fun testPropertyRemover_withComment() {
        val before = """
            # comment
            <caret>foo = bar
            baz = qux
            """.trimIndent()
        val after = """
            # comment
            baz = qux
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxScriptValueRemover

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
        val option1 = PlsBundle.message("script.remove.property", "key")
        assertOptions(before, option1)
    }

    @Test
    fun testValueRemover_multipleValues() {
        val before = """
            <caret>value1
            value2
            baz = qux
            """.trimIndent()
        val after = """
            value2
            baz = qux
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxScriptParameterConditionRemover

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
    fun testParameterConditionRemover_nested() {
        val before = """
            root = {
                <caret>[[P]
                    foo = bar
                ]
                baz = qux
            }
            """.trimIndent()
        val after = """
            root = {
                baz = qux
            }
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxScriptInlineParameterConditionRemover

    @Test
    fun testInlineParameterConditionRemover() {
        val before = "key = prefix_<caret>[[A]a]_suffix"
        val after = "key = prefix__suffix"
        val option1 = PlsBundle.message("script.remove.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option2 = PlsBundle.message("script.unwrap.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option3 = PlsBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after)
    }

    @Test
    fun testInlineParameterConditionRemover_forNested() {
        val before = "key = prefix_<caret>[[A]a[[B]b]]_suffix"
        val after = "key = prefix__suffix"
        val option1 = PlsBundle.message("script.remove.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option2 = PlsBundle.message("script.unwrap.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option3 = PlsBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after)
    }

    // endregion

    // region ParadoxScriptPropertyUnwrapper

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
    fun testPropertyUnwrapper_emptyBlock() {
        val before = """
            <caret>foo = {
            }
            bar = baz
            """.trimIndent()
        val after = """
            bar = baz
            """.trimIndent()
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testPropertyUnwrapper_mixedContent() {
        val before = """
            <caret>foo = {
                # comment
                @var = 1
                bar = baz
                value
            }
            """.trimIndent()
        val after = """
            # comment
            @var = 1
            bar = baz
            value
            """.trimIndent()
        assertUnwrapped(before, after, 1)
    }

    // endregion

    // region ParadoxScriptBlockUnwrapper

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
    fun testBlockUnwrapper_withComment() {
        val before = """
            <caret>{
                # comment
                bar = baz
            }
            """.trimIndent()
        val after = """
            # comment
            bar = baz
            """.trimIndent()
        assertUnwrapped(before, after, 1)
    }

    // endregion

    // region ParadoxScriptParameterConditionUnwrapper

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

    @Test
    fun testParameterConditionUnwrapper_mixedContent() {
        val before = """
            <caret>[[P]
                # comment
                foo = bar
                value
            ]
            """.trimIndent()
        val after = """
            # comment
            foo = bar
            value
            """.trimIndent()
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testParameterConditionUnwrapper_emptyBlock() {
        val before = """
            <caret>[[P]
            ]
            foo = bar
            """.trimIndent()
        val after = """
            foo = bar
            """.trimIndent()
        assertUnwrapped(before, after, 1)
    }

    // endregion

    //region ParadoxScriptInlineParameterConditionUnwrapper

    @Test
    fun testInlineParameterConditionUnwrapper() {
        val before = "key = prefix_<caret>[[A]a]_suffix"
        val after = "key = prefix_a_suffix"
        val option1 = PlsBundle.message("script.remove.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option2 = PlsBundle.message("script.unwrap.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option3 = PlsBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testInlineParameterConditionUnwrapper_forNested() {
        val before = "key = prefix_<caret>[[A]a[[B]b]]_suffix"
        val after = "key = prefix_a[[B]b]_suffix"
        val option1 = PlsBundle.message("script.remove.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option2 = PlsBundle.message("script.unwrap.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option3 = PlsBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testInlineParameterConditionUnwrapper_forNested_withParameter() {
        val before = "key = prefix_<caret>[[A]a[[B]b]${p("P")}]_suffix"
        val after = "key = prefix_a[[B]b]${p("P")}_suffix"
        val option1 = PlsBundle.message("script.remove.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option2 = PlsBundle.message("script.unwrap.inlineParameterCondition", PlsStrings.parameterConditionFolder("A"))
        val option3 = PlsBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after, 1)
    }

    //endregion
}
