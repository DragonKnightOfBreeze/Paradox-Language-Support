package icu.windea.pls.localisation.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.moveUpDown.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.editorActions.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

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
