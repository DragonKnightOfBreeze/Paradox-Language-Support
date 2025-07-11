package icu.windea.pls.tools.actions

import com.intellij.openapi.actionSystem.*
import icu.windea.pls.lang.*

class PlsToolsActionGroup : DefaultActionGroup() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val fileInfo = virtualFile?.fileInfo
        e.presentation.isEnabledAndVisible = fileInfo != null
    }
}
