package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor

// com.intellij.codeInsight.actions.BaseCodeInsightAction.BaseCodeInsightAction
// com.intellij.testIntegration.GotoTestOrCodeAction

/**
 * 导航动作的基类。为实现类提供一些实用的封装和抽象。
 */
abstract class GotoActionBase : BaseCodeInsightAction() {
    final override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        val visible = isVisible(file, editor)
        e.presentation.isVisible = visible
        val enabled = visible && isEnabled(file, editor)
        e.presentation.isEnabled = enabled
    }

    open fun isVisible(file: PsiFile, editor: Editor): Boolean = true

    open fun isEnabled(file: PsiFile, editor: Editor): Boolean = true
}
