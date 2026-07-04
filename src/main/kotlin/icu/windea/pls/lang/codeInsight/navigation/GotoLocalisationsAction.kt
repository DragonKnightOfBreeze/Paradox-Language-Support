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
 * 导航到当前本地化的包括自身在内的拥有相同名称的本地化。
 */
class GotoLocalisationsAction : BaseCodeInsightAction() {
    private val handler = GotoLocalisationsHandler()

    override fun getHandler() = handler

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return // 忽略直接位于游戏或模组的根目录下的文件
        if (!ParadoxPsiFileMatchService.isLocalisationFile(file)) return // 仅限有效的本地化文件
        e.presentation.isVisible = true
        val element = findElement(file, editor.caretModel.offset)
        if (!ParadoxPsiMatchService.isLocalisation(element)) return
        e.presentation.isEnabled = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFileService.findLocalisation(file, offset) { BY_NAME }
    }
}
