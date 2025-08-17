package icu.windea.pls.script.codeInsight.editorActions.moveUpDown

import com.intellij.codeInsight.editorActions.moveUpDown.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.util.*
import fleet.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import kotlin.math.*

private val logger = logger<ParadoxScriptMover>()

//com.intellij.codeInsight.editorActions.moveUpDown.JavaStatementMover

class ParadoxScriptMover : LineMover() {
    override fun checkAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean): Boolean {
        val available = super.checkAvailable(editor, file, info, down)
        if (!available) return false

        val range = expandLineRangeToCoverElements(info.toMove, editor, file)
        if (range == null) return false

        info.toMove = range
        val startOffset = editor.logicalPositionToOffset(LogicalPosition(range.startLine, 0))
        val endOffset = editor.logicalPositionToOffset(LogicalPosition(range.endLine, 0))
        val statements = findElementsInRange(file, startOffset, endOffset)
        if (statements.isEmpty()) return false

        range.firstElement = statements[0]
        range.lastElement = statements[statements.size - 1]

        return true
    }

    private fun expandLineRangeToCoverElements(range: LineRange, editor: Editor, file: PsiFile): LineRange? {
        val psiRange = getElementRange(editor, file, range)
        if (psiRange == null) return null
        val parent = PsiTreeUtil.findCommonParent(psiRange.getFirst(), psiRange.getSecond())
        val elementRange = getElementRange(parent, psiRange.getFirst(), psiRange.getSecond())
        if (elementRange == null) return null
        val endOffset = elementRange.getSecond().textRange.endOffset
        val document = editor.document
        if (endOffset > document.textLength) {
            logger.assertTrue(!PsiDocumentManager.getInstance(file.project).isUncommited(document))
            logger.assertTrue(PsiDocumentManagerBase.checkConsistency(file, document))
        }
        var endLine: Int
        if (endOffset == document.textLength) {
            endLine = document.lineCount
        } else {
            endLine = editor.offsetToLogicalPosition(endOffset).line + 1
            endLine = min(endLine, document.lineCount)
        }
        val startLine = min(range.startLine, editor.offsetToLogicalPosition(elementRange.getFirst().textOffset).line)
        endLine = max(endLine, range.endLine)
        return LineRange(startLine, endLine)
    }

    private fun findElementsInRange(file: PsiFile, startOffset: Int, endOffset: Int): Array<out PsiElement> {
        val element1 = file.findElementAt(startOffset) ?: return PsiElement.EMPTY_ARRAY
        val element2 = file.findElementAt(endOffset - 1) ?: return PsiElement.EMPTY_ARRAY
        val parent = PsiTreeUtil.findCommonParent(element1, element2)
        val container = parent?.parents(withSelf = true)?.find { isContainerElement(it) } ?: return PsiElement.EMPTY_ARRAY
        val start = element1.parents(withSelf = true).takeWhile { it != container }.lastOrNull()
        val end = element2.parents(withSelf = true).takeWhile { it != container }.lastOrNull()
        val elements = container.children().dropWhile { it != start }.takeWhileInclusive { it != end }.filter { it !is PsiWhiteSpace }.toList()
        return elements.toTypedArray()
    }

    private fun isContainerElement(element: PsiElement): Boolean {
        return element is ParadoxScriptMemberContainer
    }
}
