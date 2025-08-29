package icu.windea.pls.images.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.images.ImageManager
import org.intellij.images.editor.actions.BackgroundImageDialog

//org.intellij.images.editor.actions.SetBackgroundImageAction

class SetBackgroundImageAction : DumbAwareAction() {
    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val image = file != null && ImageManager.isExtendedImageFileType(file.fileType)
        val visible = !e.isFromContextMenu || image
        e.presentation.setEnabled(project != null)
        e.presentation.setVisible(visible)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project == null) return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val image = file != null && ImageManager.isExtendedImageFileType(file.fileType)
        val dialog = BackgroundImageDialog(project, if (image) file.path else null)
        dialog.showAndGet()
    }
}
