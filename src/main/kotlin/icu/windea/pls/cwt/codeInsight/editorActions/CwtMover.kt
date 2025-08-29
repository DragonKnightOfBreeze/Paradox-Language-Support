package icu.windea.pls.cwt.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.moveUpDown.LineRange
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.children
import icu.windea.pls.core.codeInsight.editorActions.ContainerBasedMover
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtBlockElement
import icu.windea.pls.cwt.psi.CwtElementTypes.LEFT_BRACE
import icu.windea.pls.cwt.psi.CwtElementTypes.RIGHT_BRACE
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.cwt.psi.CwtRootBlock

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
