package icu.windea.pls.core.editor

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see EditorService
 */
@RunWith(JUnit4::class)
class EditorServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region selectElement

    @Test
    fun selectElement_basic() {
        myFixture.configureByText("test.cwt", """
            key1 = value1
            key2 = value2
        """.trimIndent())
        val editor = myFixture.editor
        val property = findProperty("key2")

        EditorService.selectElement(editor, property)

        // 主光标移动到属性范围并选中，且不产生多余的光标
        val caret = editor.caretModel.primaryCaret
        assertEquals(property.textRange.startOffset, caret.selectionStart)
        assertEquals(property.textRange.endOffset, caret.selectionEnd)
        assertEquals(1, editor.caretModel.caretCount)
    }

    // endregion

    // region selectElements

    @Test
    fun selectElements_basic() {
        myFixture.configureByText("test.cwt", """
            key1 = value1
            key2 = value2
            key3 = value3
        """.trimIndent())
        val editor = myFixture.editor
        val selected = listOf(findProperty("key1"), findProperty("key3"))

        EditorService.selectElements(editor, selected)

        // 第一个元素由主光标选中，其余元素由次光标选中
        assertEquals(2, editor.caretModel.caretCount)
        val primary = editor.caretModel.primaryCaret
        assertEquals(selected[0].textRange.startOffset, primary.selectionStart)
        assertEquals(selected[0].textRange.endOffset, primary.selectionEnd)
        val secondary = editor.caretModel.allCarets.first { it !== primary }
        assertEquals(selected[1].textRange.startOffset, secondary.selectionStart)
        assertEquals(selected[1].textRange.endOffset, secondary.selectionEnd)
    }

    @Test
    fun selectElements_single() {
        myFixture.configureByText("test.cwt", """
            key1 = value1
            key2 = value2
        """.trimIndent())
        val editor = myFixture.editor
        val selected = listOf(findProperty("key1"))

        EditorService.selectElements(editor, selected)

        // 只有一个元素时，仅使用主光标
        assertEquals(1, editor.caretModel.caretCount)
        val primary = editor.caretModel.primaryCaret
        assertEquals(selected[0].textRange.startOffset, primary.selectionStart)
        assertEquals(selected[0].textRange.endOffset, primary.selectionEnd)
    }

    // endregion

    private fun findProperty(name: String): CwtProperty {
        return PsiTreeUtil.collectElementsOfType(myFixture.file, CwtProperty::class.java).first { it.name == name }
    }
}
