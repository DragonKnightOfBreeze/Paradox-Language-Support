package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.ui.BrowsePathsDialog

class BrowsePathsAction : AnAction(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val contextFile = getContextFile(e)
        val project = e.project
        BrowsePathsDialog(contextFile, project).show()
    }

    private fun getContextFile(e: AnActionEvent): VirtualFile? {
        val files = VirtualFileService.findFiles(e)
        return files.find { it.fileInfo != null }
    }
}
