package icu.windea.pls.lang.actions

import com.intellij.ide.lightEdit.LightEditCompatible
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.tools.PlsUrlService
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxFileInfo
import javax.swing.Icon

@Suppress("UnstableApiUsage")
abstract class HandleUrlActionBase(
    icon: Icon? = null,
    text: String? = null,
    description: String? = null,
) : DumbAwareAction(text, description, icon), LightEditCompatible {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        if (!isVisible(e)) return
        presentation.isVisible = true
        if (!isEnabled(e)) return
        val targetUrl = getTargetUrl(e)
        if (targetUrl == null) return
        presentation.isEnabled = true
        presentation.description = templatePresentation.description + " (" + targetUrl + ")"
    }

    protected open fun isVisible(e: AnActionEvent): Boolean = true

    protected open fun isEnabled(e: AnActionEvent): Boolean = true

    protected abstract fun getTargetUrl(e: AnActionEvent): String?

    protected fun getFileInfo(e: AnActionEvent): ParadoxFileInfo? {
        val files = PlsFileManager.findFiles(e)
        val fileInfo = files.firstNotNullOfOrNull { it.fileInfo }
        return fileInfo
    }

    protected fun copyUrl(e: AnActionEvent) {
        val targetUrl = getTargetUrl(e) ?: return
        PlsUrlService.getInstance().copyUrl(targetUrl)
    }

    protected fun openUrl(e: AnActionEvent) {
        val targetUrl = getTargetUrl(e) ?: return
        PlsUrlService.getInstance().openUrl(targetUrl)
    }
}
