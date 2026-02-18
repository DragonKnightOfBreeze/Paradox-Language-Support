package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapTestCase
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
        assertUnwrapped(
            """
            root = {
                <caret>@foo = 1
                bar = baz
            }
            """.trimIndent(),
            """
            root = {
                bar = baz
            }
            """.trimIndent()
        )
    }

    @Test
    fun testPropertyRemover() {
        assertUnwrapped(
            """
            root = {
                <caret>foo = bar
                baz = qux
            }
            """.trimIndent(),
            """
            root = {
                baz = qux
            }
            """.trimIndent()
        )
    }

    @Test
    fun testPropertyRemover_nested() {
        assertUnwrapped(
            """
            root = {
                nested = {
                    <caret>foo = bar
                    baz = qux
                }
            }
            """.trimIndent(),
            """
            root = {
                nested = {
                    baz = qux
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testValueRemover() {
        assertUnwrapped(
            """
            <caret>value
            foo = bar
            """.trimIndent(),
            """
            foo = bar
            """.trimIndent()
        )
    }

    @Test
    fun testValueRemover_forPropertyValue_notAllowed() {
        assertUnwrapped(
            """
            key = <caret>value
            foo = bar
            """.trimIndent(),
            """
            key = <caret>value
            """.trimIndent()
        )
    }

    @Test
    fun testValueRemover_forBlock() {
        assertUnwrapped(
            """
            <caret>{ value }
            foo = bar
            """.trimIndent(),
            """
            foo = bar
            """.trimIndent()
        )
    }

    @Test
    fun testParameterConditionRemover() {
        assertUnwrapped(
            """
            root = {
                <caret>[[P]
                    foo = bar
                ]
                baz = qux
            }
            """.trimIndent(),
            """
            root = {
                baz = qux
            }
            """.trimIndent()
        )
    }

    @Test
    fun testInlineParameterConditionRemover() {
        assertUnwrapped(
            """
            root = {
                foo = <caret>[[P] bar ]
            }
            """.trimIndent(),
            """
            root = {
                foo =
            }
            """.trimIndent()
        )
    }

    @Test
    fun testPropertyUnwrapper() {
        assertUnwrapped(
            """
            root = {
                <caret>foo = {
                    bar = baz
                    qux = quux
                }
            }
            """.trimIndent(),
            """
            root = {
                bar = baz
                qux = quux
            }
            """.trimIndent()
        )
    }

    @Test
    fun testPropertyUnwrapper_nested() {
        assertUnwrapped(
            """
            root = {
                outer = {
                    <caret>inner = {
                        foo = bar
                    }
                }
            }
            """.trimIndent(),
            """
            root = {
                outer = {
                    foo = bar
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testBlockUnwrapper() {
        assertUnwrapped(
            """
            root = {
                foo = <caret>{
                    bar = baz
                }
            }
            """.trimIndent(),
            """
            root = {
                bar = baz
            }
            """.trimIndent(),
            1 // 选择第二个选项（BlockUnwrapper）
        )
    }

    @Test
    fun testParameterConditionUnwrapper() {
        assertUnwrapped(
            """
            root = {
                <caret>[[P]
                    foo = bar
                    baz = qux
                ]
            }
            """.trimIndent(),
            """
            root = {
                foo = bar
                baz = qux
            }
            """.trimIndent()
        )
    }

    @Test
    fun testParameterConditionUnwrapper_nested() {
        assertUnwrapped(
            """
            root = {
                outer = {
                    <caret>[[P]
                        foo = bar
                    ]
                }
            }
            """.trimIndent(),
            """
            root = {
                outer = {
                    foo = bar
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testInlineParameterConditionUnwrapper() {
        assertUnwrapped(
            """
            root = {
                foo = <caret>[[P] bar ]
            }
            """.trimIndent(),
            """
            root = {
                foo = bar
            }
            """.trimIndent(),
            1 // 选择第二个选项（InlineParameterConditionUnwrapper）
        )
    }
}
