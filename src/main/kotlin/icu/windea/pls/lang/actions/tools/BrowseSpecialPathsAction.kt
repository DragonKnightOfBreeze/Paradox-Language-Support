package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.ui.tools.BrowseSpecialPathsDialog

class BrowseSpecialPathsAction : AnAction(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val file = getContextFile(e)
        BrowseSpecialPathsDialog(project, file).show()
    }

    private fun getContextFile(e: AnActionEvent): VirtualFile? {
        return VirtualFileService.findFiles(e).find { it.fileInfo != null }
    }
}
