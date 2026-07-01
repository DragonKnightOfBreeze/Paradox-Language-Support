package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 导航到当前本地化的相关定义。
 */
class GotoRelatedDefinitionsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedDefinitionsHandler()

    override fun getHandler() = handler

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (ParadoxPsiFileMatchService.isTopFileFromRoot(file)) return // 忽略直接位于游戏或模组的根目录下的文件
        if (!ParadoxPsiFileMatchService.isLocalisationFile(file, injectable = true)) return
        presentation.isVisible = true
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        if (!ParadoxPsiMatchService.isNormalLocalisation(element)) return
        presentation.isEnabled = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFileService.findLocalisation(file, offset)
    }
}
