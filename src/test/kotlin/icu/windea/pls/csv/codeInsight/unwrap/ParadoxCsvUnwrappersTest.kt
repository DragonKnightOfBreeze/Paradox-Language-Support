package icu.windea.pls.csv.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapTestCase
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * CWT Unwrappers 测试。
 *
 * @see ParadoxCsvUnwrapDescriptor
 * @see ParadoxCsvUnwrapper
 * @see ParadoxCsvRowRemover
 */
@RunWith(JUnit4::class)
class ParadoxCsvUnwrappersTest : UnwrapTestCase(), ChronicleTestScope {
    override fun getFileNameToCreate() = "test.csv"

    override fun createCode(code: String) = code

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region ParadoxCsvRowRemover

    @Test
    fun testRowRemover_firstRow() {
        val before = """
            key;value
            <caret>k1;v1
            k2;v2
            """.trimIndent()
        val after = """
            key;value
            <caret>k2;v2
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testRowRemover_middleRow() {
        val before = """
            key;value
            k0;v0
            <caret>k1;v1
            k2;v2
            """.trimIndent()
        val after = """
            key;value
            k0;v0
            <caret>k2;v2
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testRowRemover_lastRow() {
        val before = """
            key;value
            k1;v1
            <caret>k2;v2
            """.trimIndent()
        val after = """
            key;value
            k1;v1

            """.trimIndent()
        assertUnwrapped(before, after)
    }

    @Test
    fun testRowRemover_trailingSpaces() {
        val before = """
            key;value
            k0;v0
            <caret>k1;v1
            k2;v2
            """.trimIndent()
        val after = """
            key;value
            k0;v0
            <caret>k2;v2
            """.trimIndent()
        assertUnwrapped(before, after)
    }

    // endregion
}
