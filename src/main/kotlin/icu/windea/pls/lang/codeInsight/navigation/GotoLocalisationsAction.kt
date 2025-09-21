package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.lang.util.psi.ParadoxPsiMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 导航到当前本地化的包括自身在内的相同名称的本地化。
 */
class GotoLocalisationsAction : BaseCodeInsightAction() {
    private val handler = GotoLocalisationsHandler()

    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxLocalisationFile) return
        val fileInfo = file.fileInfo ?: return
        if (fileInfo.path.length <= 1) return //忽略直接位于游戏或模组入口目录下的文件
        presentation.isVisible = true
        val offset = editor.caretModel.offset
        val localisation = findElement(file, offset)
        presentation.isEnabled = ParadoxPsiMatcher.isLocalisation(localisation)
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFinder.findLocalisation(file, offset) { BY_NAME }
    }
}
