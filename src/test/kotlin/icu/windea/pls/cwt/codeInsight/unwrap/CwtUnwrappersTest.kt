package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapTestCase
import icu.windea.pls.PlsBundle
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * CWT Unwrappers 测试。
 *
 * @see CwtUnwrapDescriptor
 * @see CwtUnwrapper
 * @see CwtPropertyRemover
 * @see CwtValueRemover
 * @see CwtPropertyUnwrapper
 * @see CwtBlockUnwrapper
 */
@RunWith(JUnit4::class)
class CwtUnwrappersTest : UnwrapTestCase() {
    override fun getFileNameToCreate() = "test.cwt"

    override fun createCode(code: String) = code

    @Test
    fun testPropertyRemover() {
        val before = """
            root = {
                <caret>foo = bar
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

    @Test
    fun testPropertyRemover_nested() {
        val before = """
            root = {
                nested = {
                    <caret>foo = bar
                    baz = qux
                }
            }
            """.trimIndent()
        val after = """
            root = {
                nested = {
                    baz = qux
                }
            }
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testValueRemover() {
        val before = """
            <caret>value
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
            foo = bar
            """.trimIndent()
        assertOptions(before, PlsBundle.message("cwt.unwrap.block"))
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
        assertOptions(before, PlsBundle.message("cwt.remove.block"), PlsBundle.message("cwt.unwrap.block"))
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testPropertyUnwrapper() {
        val before = """
            root = {
                <caret>foo = {
                    bar = baz
                    qux = quux
                }
            }
            """.trimIndent()
        val after = """
            root = {
                bar = baz
                qux = quux
            }
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testPropertyUnwrapper_nested() {
        val before = """
            root = {
                outer = {
                    <caret>inner = {
                        foo = bar
                    }
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
        assertUnwrapped(before, after)
    }

    @Test
    fun testBlockUnwrapper() {
        val before = """
            root = {
                foo = <caret>{
                    bar = baz
                }
            }
            """.trimIndent()
        val after = """
            root = {
                bar = baz
            }
            """.trimIndent()
        assertOptions(before, PlsBundle.message("cwt.remove.block"), PlsBundle.message("cwt.unwrap.block"))
        assertUnwrapped(before, after, 1)
    }
}
