package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import java.nio.file.Path
import javax.swing.Icon

abstract class HandlePathActionBase(
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
        val targetPath = getTargetPath(e)
        val enabled = targetPath != null && isEnabled(e)
        presentation.isEnabled = enabled
        if (!enabled) return
        presentation.description = templatePresentation.description + " (" + targetPath + ")"
    }

    protected open fun isVisible(e: AnActionEvent): Boolean = true

    protected open fun isEnabled(e: AnActionEvent): Boolean = true

    protected abstract fun getTargetPath(e: AnActionEvent): Path?
}
