package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.actionSystem.*
import icu.windea.pls.lang.*

class ToolsActionGroup : DefaultActionGroup() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val fileInfo = virtualFile?.fileInfo
        e.presentation.isEnabledAndVisible = fileInfo != null
    }
}
