package icu.windea.pls.script.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.moveUpDown.LineRange
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.children
import icu.windea.pls.core.codeInsight.editorActions.ContainerBasedMover
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LEFT_BRACE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.NESTED_RIGHT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RIGHT_BRACE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.RIGHT_BRACKET
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptRootBlock

/**
 * 用于在脚本文件中，为成员（封装变量、属性、单独的值）适配 *上移/下移声明* 的功能。兼容附加的注释。
 *
 * 入口：主菜单，点击 `Code -> Move Statement Up/Down`。
 */
class ParadoxScriptMover : ContainerBasedMover() {
    override fun checkFileAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean) = file.language is ParadoxScriptLanguage

    override fun isContainerElement(element: PsiElement) = element is ParadoxScriptMemberContainer

    override fun isMemberElement(element: PsiElement) = element is ParadoxScriptMemberElement

    override fun canAttachComments(memberElement: PsiElement) = true

    override fun canSkipBlankLines(memberElement: PsiElement) = true

    override fun getLineRangeForMemberElements(editor: Editor, containerElement: PsiElement): LineRange? {
        return when {
            containerElement is ParadoxScriptRootBlock -> LineRange(containerElement)
            containerElement is ParadoxScriptBlock -> {
                val start = containerElement.children(forward = true).find { it.elementType == LEFT_BRACE } ?: return null
                val end = containerElement.children(forward = false).find { it.elementType == RIGHT_BRACE } ?: return null
                getLineRangeInExclusive(editor, containerElement, start.endOffset, end.startOffset)
            }
            containerElement is ParadoxScriptParameterCondition -> {
                val start = containerElement.children(forward = true).find { it.elementType == NESTED_RIGHT_BRACKET } ?: return null
                val end = containerElement.children(forward = false).find { it.elementType == RIGHT_BRACKET } ?: return null
                getLineRangeInExclusive(editor, containerElement, start.endOffset, end.startOffset)
            }
            else -> null
        }
    }
}
