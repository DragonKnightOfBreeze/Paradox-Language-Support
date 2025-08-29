package icu.windea.pls.core.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.parents
import com.intellij.psi.util.startOffset
import com.intellij.util.DocumentUtil.isLineEmpty
import fleet.util.takeWhileInclusive
import icu.windea.pls.core.children
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.lang.util.PlsPsiManager

abstract class ContainerBasedMover : LineMover() {
    override fun checkAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean): Boolean {
        if (!checkFileAvailable(editor, file, info, down)) return false
        return doCheckAvailable(editor, file, info, down)
    }

    private fun doCheckAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean): Boolean {
        if (!super.checkAvailable(editor, file, info, down)) return false

        val document = editor.document
        val lineMoverRange = info.toMove
        val elementRange = getElementRange(editor, file, lineMoverRange)
        if (elementRange == null) return false

        // 尝试找到容器，如果找不到则直接返回false（不适用此 Mover）
        val startElement = elementRange.getFirst()
        val endElement = elementRange.getSecond()
        val parent = PsiTreeUtil.findCommonParent(startElement, endElement)
        if (parent == null) return false
        val container = parent.parents(withSelf = true).find { isContainerElement(it) }
        if (container == null) return false

        // 将待移动的行范围设置为期望的范围（开始成员~结束成员，兼容附加的注释）
        val memberTuple = findMemberElements(container, startElement, endElement)
        val (startMember, endMember) = memberTuple ?: return info.prohibitMove()
        val lineRange = LineRange(startMember, endMember, document)
        info.toMove = lineRange

        // 检查要移动到的行是否未超出容器的成员范围
        val lineRangeForMembers = getLineRangeForMemberElements(editor, container)
        if (lineRangeForMembers == null) return info.prohibitMove()
        val moveToLine = when {
            down -> lineRange.endLine
            else -> lineRange.startLine - 1
        }
        val moveToLineCheck = when {
            down -> moveToLine <= lineRangeForMembers.endLine
            else -> moveToLine >= lineRangeForMembers.startLine
        }
        if (!moveToLineCheck) return info.prohibitMove()

        // 将要移动到的行范围首先设置为期望的上一行或者下一行
        val lineRangeToMoveFirst = LineRange(moveToLine, moveToLine + 1)
        info.toMove2 = lineRangeToMoveFirst

        // 如果必要，要移动到的行要跳过空白行（仅限在那之后能找到成员时，才真正跳过）
        val lineToCheck = if (down) lineRangeForMembers.endLine else lineRangeForMembers.startLine
        val memberToCheck = if (down) endMember else startMember
        val finalMoveToLine = when {
            moveToLine == lineToCheck -> moveToLine
            canSkipBlankLines(memberToCheck) -> {
                var line = moveToLine
                val lineCount = document.lineCount
                while (true) {
                    val nextLine = if (down) line + 1 else line - 1
                    if (nextLine < 0 || nextLine >= lineCount) break
                    if (nextLine == lineToCheck) break
                    line = nextLine
                    if (!isLineEmpty(document, nextLine)) break
                }
                line
            }
            else -> moveToLine
        }

        // 如果必要，将要移动到的行范围设置为期望的范围（期望的那一行对应的开始成员~结束成员，兼容附加的注释）
        val moveToLineStartOffset = editor.logicalPositionToOffset(LogicalPosition(finalMoveToLine, 0))
        val moveToLineEndOffset = editor.logicalPositionToOffset(LogicalPosition(finalMoveToLine, document.getLineEndOffset(finalMoveToLine))) - 1
        val startElementMoveTo = file.findElementAt(moveToLineStartOffset)
        if (startElementMoveTo == null) return true
        val endElementMoveTo = when {
            moveToLineEndOffset <= moveToLineStartOffset -> startElementMoveTo
            else -> file.findElementAt(moveToLineEndOffset - 1)
        }
        if (endElementMoveTo == null) return true
        val memberTupleMoveTo = findMemberElements(container, startElementMoveTo, endElementMoveTo, canFromAttachedComments = true)
        val (startMemberMoveTo, endMemberMoveTo) = memberTupleMoveTo ?: return true
        val lineRangeMoveTo = LineRange(startMemberMoveTo, endMemberMoveTo, document)
        info.toMove2 = lineRangeMoveTo

        return true
    }

    private fun findMemberElements(
        containerElement: PsiElement,
        startElement: PsiElement,
        endElement: PsiElement,
        canFromAttachedComments: Boolean = false
    ): Tuple2<PsiElement, PsiElement>? {
        val startMember0 = when {
            startElement == containerElement -> startElement.firstChild
            else -> startElement.parents(withSelf = true).takeWhile { it != containerElement }.lastOrNull()
        }
        if (startMember0 == null) return null
        val endMember0 = when {
            endElement == containerElement -> endElement.lastChild
            else -> endElement.parents(withSelf = true).takeWhile { it != containerElement }.lastOrNull()
        }
        if (endMember0 == null) return null
        val members0 = containerElement.children()
            .dropWhile { it != startMember0 }.takeWhileInclusive { it != endMember0 }
            .filter { it !is PsiWhiteSpace }
            .toList()
        if (members0.isEmpty()) return null
        val endMember1 = members0.findLast { isMemberElement(it) || (canFromAttachedComments && it is PsiComment) }
        val finalEndMember = when {
            endMember1 == null -> null
            endMember1 is PsiComment && canFromAttachedComments -> PlsPsiManager.getAttachingElement(endMember1)
                ?: endMember1.takeIf { isMemberElement(it) }
            else -> endMember1
        }
        if (finalEndMember == null) return null
        val startMember1 = members0.find { isMemberElement(it) } ?: finalEndMember
        val finalStartMember = when {
            canAttachComments(startMember1) -> PlsPsiManager.getAttachedComments(startMember1).lastOrNull()
                ?: startMember1
            else -> startMember1
        }
        return tupleOf(finalStartMember, finalEndMember)
    }

    protected abstract fun checkFileAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean): Boolean

    protected abstract fun isContainerElement(element: PsiElement): Boolean

    protected open fun isMemberElement(element: PsiElement): Boolean = true

    protected open fun canAttachComments(memberElement: PsiElement): Boolean = false

    protected open fun canSkipBlankLines(memberElement: PsiElement): Boolean = false

    protected open fun getLineRangeForMemberElements(editor: Editor, containerElement: PsiElement): LineRange? = LineRange(containerElement)

    protected fun getLineRangeInExclusive(editor: Editor, containerElement: PsiElement, startOffset: Int?, endOffset: Int?): LineRange? {
        val startLine = when {
            startOffset == null -> editor.document.getLineNumber(containerElement.startOffset)
            else -> editor.document.getLineNumber(startOffset) + 1
        }
        val endLine = when {
            endOffset == null -> editor.document.getLineNumber(containerElement.endOffset)
            else -> editor.document.getLineNumber(endOffset) - 1
        }
        if (startLine >= endLine) return null
        return LineRange(startLine, endLine)
    }
}
