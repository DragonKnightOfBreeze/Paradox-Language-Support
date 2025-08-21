package icu.windea.pls.cwt.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.moveUpDown.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.editorActions.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

/**
 * 用于在 CWT 文件中，为成员（属性、单独的值）适配 *上移/下移声明* 的功能。兼容附加的注释。
 *
 * 入口：主菜单，点击 `Code -> Move Statement Up/Down`。
 */
class CwtMover : ContainerBasedMover() {
    override fun checkFileAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean) = file.language is CwtLanguage

    override fun isContainerElement(element: PsiElement) = element is CwtBlockElement

    override fun isMemberElement(element: PsiElement) = element is CwtMemberElement

    override fun canAttachComments(memberElement: PsiElement) = true

    override fun canSkipBlankLines(memberElement: PsiElement) = true

    override fun getLineRangeForMemberElements(editor: Editor, containerElement: PsiElement): LineRange? {
        return when {
            containerElement is CwtRootBlock -> LineRange(containerElement)
            containerElement is CwtBlock -> {
                val start = containerElement.children(forward = true).find { it.elementType == LEFT_BRACE } ?: return null
                val end = containerElement.children(forward = false).find { it.elementType == RIGHT_BRACE } ?: return null
                getLineRangeInExclusive(editor, containerElement, start.endOffset, end.startOffset)
            }
            else -> null
        }
    }
}
