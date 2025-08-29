package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.fileInfo

/**
 * 导航到对应的规则的动作。
 */
class GotoRelatedConfigsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedConfigsHandler()

    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file.fileInfo == null) return
        if (file.language !is ParadoxBaseLanguage) return
        presentation.isEnabledAndVisible = true
    }
}
