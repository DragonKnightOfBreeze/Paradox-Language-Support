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

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file.language !is ParadoxLanguage) return
        e.presentation.isEnabledAndVisible = true
    }
}
