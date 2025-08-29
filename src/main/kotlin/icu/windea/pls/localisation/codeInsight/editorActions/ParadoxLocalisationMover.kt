package icu.windea.pls.localisation.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.moveUpDown.LineRange
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.children
import icu.windea.pls.core.codeInsight.editorActions.ContainerBasedMover
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LOCALE
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

/**
 * 用于在本地化文件中，为属性（即本地化条目）适配 *上移/下移声明* 的功能。兼容附加的注释。
 *
 * 入口：主菜单，点击 `Code -> Move Statement Up/Down`。
 */
class ParadoxLocalisationMover : ContainerBasedMover() {
    override fun checkFileAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean) = file.language is ParadoxLocalisationLanguage

    override fun isContainerElement(element: PsiElement) = element is ParadoxLocalisationPropertyList

    override fun isMemberElement(element: PsiElement) = element is ParadoxLocalisationProperty

    override fun canAttachComments(memberElement: PsiElement) = true

    override fun getLineRangeForMemberElements(editor: Editor, containerElement: PsiElement): LineRange? {
        return when {
            containerElement is ParadoxLocalisationPropertyList -> {
                val start = containerElement.children(forward = true).find { it.elementType == LOCALE } ?: return null
                getLineRangeInExclusive(editor, containerElement, start.endOffset, null)
            }
            else -> null
        }
    }
}
