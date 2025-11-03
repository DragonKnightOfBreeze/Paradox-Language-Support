package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.lang.tools.PlsUrlService
import javax.swing.Icon

abstract class HandleUrlActionBase(
    icon: Icon? = null,
    text: String? = null,
    description: String? = null,
) : DumbAwareAction(text, description, icon) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val visible = isVisible(e)
        presentation.isEnabledAndVisible = visible
        if (!visible) return
        val targetUrl = getTargetUrl(e)
        val enabled = targetUrl != null && isEnabled(e)
        presentation.isEnabled = enabled
        if (!enabled) return
        presentation.description = templatePresentation.description + " (" + targetUrl + ")"
    }

    protected open fun isVisible(e: AnActionEvent): Boolean = true

    protected open fun isEnabled(e: AnActionEvent): Boolean = true

    protected abstract fun getTargetUrl(e: AnActionEvent): String?

    protected fun copyUrl(e: AnActionEvent) {
        val targetUrl = getTargetUrl(e) ?: return
        PlsUrlService.copyUrl(targetUrl)
    }

    protected fun openUrl(e: AnActionEvent) {
        val targetUrl = getTargetUrl(e) ?: return
        PlsUrlService.openUrl(targetUrl)
    }
}
