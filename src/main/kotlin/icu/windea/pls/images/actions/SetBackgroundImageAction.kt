package icu.windea.pls.images.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import icu.windea.pls.images.*
import org.intellij.images.editor.actions.*

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
