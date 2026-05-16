package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.ui.tools.CopySpecialUrlPopup

class CopySpecialUrlAction : AnAction(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val file = getContextFile(e)
        val popup = CopySpecialUrlPopup(file)
        JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(e.dataContext)
    }

    private fun getContextFile(e: AnActionEvent): VirtualFile? {
        return VirtualFileService.findFiles(e).find { it.fileInfo != null }
    }
}
