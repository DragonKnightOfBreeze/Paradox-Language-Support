package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.lang.text.ParadoxScriptTextBuilder.parameter as p

/**
 * Paradox Script Unwrappers 测试。
 *
 * @see ParadoxScriptUnwrapDescriptor
 * @see ParadoxScriptUnwrapper
 * @see ParadoxScriptScriptedVariableRemover
 * @see ParadoxScriptPropertyRemover
 * @see ParadoxScriptValueRemover
 * @see ParadoxScriptConditionalBlockRemover
 * @see ParadoxScriptInlineConditionalBlockRemover
 * @see ParadoxScriptPropertyUnwrapper
 * @see ParadoxScriptBlockUnwrapper
 * @see ParadoxScriptConditionalBlockUnwrapper
 * @see ParadoxScriptInlineConditionalBlockUnwrapper
 */
@RunWith(JUnit4::class)
class ParadoxScriptUnwrappersTest : UnwrapTestCase() {
    override fun getFileNameToCreate() = "test.txt"

    override fun createCode(code: String) = code

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

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
        val option1 = ChronicleBundle.message("script.remove.property", "key")
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

    // region ParadoxScriptConditionalBlockRemover

    @Test
    fun testConditionalBlockRemover() {
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
    fun testConditionalBlockRemover_nested() {
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

    // region ParadoxScriptInlineConditionalBlockRemover

    @Test
    fun testInlineConditionalBlockRemover() {
        val before = "key = prefix_<caret>[[A]a]_suffix"
        val after = "key = prefix__suffix"
        val option1 = ChronicleBundle.message("script.remove.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option2 = ChronicleBundle.message("script.unwrap.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option3 = ChronicleBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after)
    }

    @Test
    fun testInlineConditionalBlockRemover_forNested() {
        val before = "key = prefix_<caret>[[A]a[[B]b]]_suffix"
        val after = "key = prefix__suffix"
        val option1 = ChronicleBundle.message("script.remove.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option2 = ChronicleBundle.message("script.unwrap.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option3 = ChronicleBundle.message("script.remove.property", "key")
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
        val option1 = ChronicleBundle.message("script.remove.property", "foo")
        val option2 = ChronicleBundle.message("script.unwrap.property", "foo")
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
        val option1 = ChronicleBundle.message("script.remove.block")
        val option2 = ChronicleBundle.message("script.unwrap.block")
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

    // region ParadoxScriptConditionalBlockUnwrapper

    @Test
    fun testConditionalBlockUnwrapper() {
        val before = """
            <caret>[[P]
                foo = bar
            ]
            """.trimIndent()
        val after = """
            foo = bar
            """.trimIndent()
        val option1 = ChronicleBundle.message("script.remove.conditionalBlock", ChronicleStrings.conditionalBlockFolder("P"))
        val option2 = ChronicleBundle.message("script.unwrap.conditionalBlock", ChronicleStrings.conditionalBlockFolder("P"))
        assertOptions(before, option1, option2)
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testConditionalBlockUnwrapper_nested() {
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
    fun testConditionalBlockUnwrapper_mixedContent() {
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
    fun testConditionalBlockUnwrapper_emptyBlock() {
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

    // region ParadoxScriptInlineConditionalBlockUnwrapper

    @Test
    fun testInlineConditionalBlockUnwrapper() {
        val before = "key = prefix_<caret>[[A]a]_suffix"
        val after = "key = prefix_a_suffix"
        val option1 = ChronicleBundle.message("script.remove.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option2 = ChronicleBundle.message("script.unwrap.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option3 = ChronicleBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testInlineConditionalBlockUnwrapper_forNested() {
        val before = "key = prefix_<caret>[[A]a[[B]b]]_suffix"
        val after = "key = prefix_a[[B]b]_suffix"
        val option1 = ChronicleBundle.message("script.remove.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option2 = ChronicleBundle.message("script.unwrap.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option3 = ChronicleBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testInlineConditionalBlockUnwrapper_forNested_withParameter() {
        val before = "key = prefix_<caret>[[A]a[[B]b]${p("P")}]_suffix"
        val after = "key = prefix_a[[B]b]${p("P")}_suffix"
        val option1 = ChronicleBundle.message("script.remove.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option2 = ChronicleBundle.message("script.unwrap.inlineConditionalBlock", ChronicleStrings.conditionalBlockFolder("A"))
        val option3 = ChronicleBundle.message("script.remove.property", "key")
        assertOptions(before, option1, option2, option3)
        assertUnwrapped(before, after, 1)
    }

    // endregion
}
