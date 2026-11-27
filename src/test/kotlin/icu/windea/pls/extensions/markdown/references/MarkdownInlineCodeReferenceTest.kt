@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.references

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import icu.windea.pls.extensions.markdown.MarkdownInlineCodeInfo
import icu.windea.pls.extensions.markdown.MarkdownTestBase
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownInlineCodeReference
 */
@RunWith(JUnit4::class)
class MarkdownInlineCodeReferenceTest : MarkdownTestBase() {
    // region resolve

    @Test
    fun testResolve() {
        val target = findScriptedVariable()
        configureMarkdown("`@greeting`")
        val reference = createReference(target)

        assertSame(target, reference.resolve())
    }

    @Test
    fun testRangeInElement_excludesPrefix() {
        val target = findScriptedVariable()
        configureMarkdown("`@greeting`")
        val reference = createReference(target)

        assertEquals(2, reference.rangeInElement.startOffset)
        assertEquals(10, reference.rangeInElement.endOffset)
        assertEquals("greeting", reference.rangeInElement.substring(myFixture.file.text))
    }

    @Test
    fun testResolve_returnsNullForInvalidTarget() {
        val target = findScriptedVariable()
        configureMarkdown("`@greeting`")
        val reference = createReference(target)

        WriteCommandAction.runWriteCommandAction(project) { target.delete() }
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        assertFalse(target.isValid)
        assertNull(reference.resolve())
    }

    // endregion

    // region rename

    @Test
    fun testHandleElementRename_preservesScriptedVariablePrefix() {
        val target = findScriptedVariable()
        configureMarkdown("`@greeting`")
        val reference = createReference(target)

        WriteCommandAction.runWriteCommandAction(project) { reference.handleElementRename("farewell") }
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        assertEquals("`@farewell`", myFixture.file.text)
    }

    @Test
    fun testHandleElementRename_stripsRedundantPrefix() {
        val target = findScriptedVariable()
        configureMarkdown("`@greeting`")
        val reference = createReference(target)

        WriteCommandAction.runWriteCommandAction(project) { reference.handleElementRename("@farewell") }
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        assertEquals("`@farewell`", myFixture.file.text)
    }

    @Test
    fun testHandleElementRename_noPrefix() {
        val target = findScriptedVariable()
        configureMarkdown("`greeting`")
        val reference = createReference(target)

        WriteCommandAction.runWriteCommandAction(project) { reference.handleElementRename("farewell") }
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        assertEquals("`farewell`", myFixture.file.text)
    }

    @Test
    fun testHandleElementRename_doubleBackticks() {
        val target = findScriptedVariable()
        configureMarkdown("``@greeting``")
        val reference = createReference(target)

        WriteCommandAction.runWriteCommandAction(project) { reference.handleElementRename("farewell") }
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        assertEquals("``@farewell``", myFixture.file.text)
    }

    // endregion

    private fun createReference(target: ParadoxScriptScriptedVariable): MarkdownInlineCodeReference {
        val codeSpan = findCodeSpan()
        val info = MarkdownInlineCodeInfo.from(codeSpan) ?: error("Expected inline code info")
        return MarkdownInlineCodeReference(codeSpan, info.rangeInElement, target, info.prefix)
    }

    private fun findScriptedVariable(): ParadoxScriptScriptedVariable {
        val file = addScriptedVariableFile("common/scripted_variables/00_test.txt", "@greeting = \"hello\"")
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: error("Expected a PSI file")
        return PsiTreeUtil.findChildOfType(psiFile, ParadoxScriptScriptedVariable::class.java)
            ?: error("Expected a scripted variable")
    }
}
