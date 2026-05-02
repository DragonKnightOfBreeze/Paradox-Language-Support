package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.ParadoxLanguage

/**
 * 导航到对应的规则。
 */
class GotoRelatedConfigsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedConfigsHandler()

    override fun getHandler() = handler

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file.language !is ParadoxLanguage) return
        presentation.isEnabledAndVisible = true
    }
}
