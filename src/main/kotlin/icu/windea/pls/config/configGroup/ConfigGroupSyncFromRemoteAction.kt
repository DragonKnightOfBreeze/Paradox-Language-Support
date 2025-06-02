package icu.windea.pls.config.configGroup

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.util.*
import icu.windea.pls.lang.*

class ConfigGroupSyncFromRemoteAction : DumbAwareAction() {
    init {
        templatePresentation.icon = PlsIcons.Actions.SyncConfigGroupsFromRemote
        templatePresentation.text = PlsBundle.message("configGroup.syncFromRemote.action.text")
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        //如果当前文件是游戏或模组文件，相关配置启用且合法，则可见
        //如果可见，则总是可用，即使本地规则仓库的状态是最新的

        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file?.fileInfo == null) return
        val valid = PlsConfigRepositoryManager.isValidToSync()
        presentation.isEnabledAndVisible = valid
    }

    override fun actionPerformed(e: AnActionEvent) {
        PlsConfigRepositoryManager.syncFromConfigRepositoryUrls()
    }
}
