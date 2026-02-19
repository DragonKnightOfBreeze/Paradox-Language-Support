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

    // region CwtPropertyRemover

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

    // region CwtValueRemover

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
        val option1 = PlsBundle.message("cwt.remove.property", "key")
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

    // region CwtPropertyUnwrapper

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
        val option1 = PlsBundle.message("cwt.remove.property", "foo")
        val option2 = PlsBundle.message("cwt.unwrap.property", "foo")
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
        val option1 = PlsBundle.message("cwt.remove.property", "foo")
        val option2 = PlsBundle.message("cwt.unwrap.property", "foo")
        assertOptions(before, option1, option2)
        assertUnwrapped(before, after, 1)
    }

    @Test
    fun testPropertyUnwrapper_mixedContent() {
        val before = """
            <caret>foo = {
                # comment
                bar = baz
                value
            }
            """.trimIndent()
        val after = """
            # comment
            bar = baz
            value
            """.trimIndent()
        assertUnwrapped(before, after, 1)
    }

    // endregion

    // region CwtBlockUnwrapper

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
        val option1 = PlsBundle.message("cwt.remove.block")
        val option2 = PlsBundle.message("cwt.unwrap.block")
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
}
