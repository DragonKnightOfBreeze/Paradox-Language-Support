package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.ParadoxLocalisationCategory

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
        presentation.isEnabled = localisation != null && localisation.category != null
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiManager.findLocalisation(file, offset)
    }
}
