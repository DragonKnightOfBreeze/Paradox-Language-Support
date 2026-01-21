package icu.windea.pls.lang.actions

import com.intellij.ide.lightEdit.LightEditCompatible
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.tools.PlsPathService
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxFileInfo
import java.nio.file.Path
import javax.swing.Icon

@Suppress("UnstableApiUsage")
abstract class HandlePathActionBase(
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
        val targetPath = getTargetPath(e)
        if (targetPath == null) return
        presentation.isEnabled = true
        presentation.description = templatePresentation.description + " (" + targetPath + ")"
    }

    protected open fun isVisible(e: AnActionEvent): Boolean = true

    protected open fun isEnabled(e: AnActionEvent): Boolean = true

    protected abstract fun getTargetPath(e: AnActionEvent): Path?

    protected fun getFileInfo(e: AnActionEvent): ParadoxFileInfo? {
        val files = PlsFileManager.findFiles(e)
        return files.firstNotNullOfOrNull { it.fileInfo }
    }

    protected fun openPath(e: AnActionEvent) {
        val targetPath = getTargetPath(e) ?: return
        PlsPathService.getInstance().openPath(targetPath)
    }

    protected fun copyPath(e: AnActionEvent) {
        val targetPath = getTargetPath(e) ?: return
        PlsPathService.getInstance().copyPath(targetPath)
    }
}
