package icu.windea.pls.tools.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo

abstract class LaunchGameActionBase : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    protected fun getGameType(e: AnActionEvent): ParadoxGameType? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val gameType = selectGameType(file) ?: return null
        if (gameType == ParadoxGameType.Core) return null
        return gameType
    }

    protected fun getRootInfo(e: AnActionEvent): ParadoxRootInfo? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val rootFile = selectRootFile(file) ?: return null
        val rootInfo = rootFile.rootInfo ?: return null
        return rootInfo
    }
}
