package icu.windea.pls.lang.actions.config

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.TooltipDescriptionProvider
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.lang.fileInfo

// com.intellij.openapi.externalSystem.autoimport.ProjectRefreshAction

class ConfigGroupRefreshAction : DumbAwareAction(), TooltipDescriptionProvider {
    init {
        templatePresentation.icon = ChronicleIcons.Actions.RefreshConfigGroups
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file?.fileInfo == null) return
        e.presentation.isVisible = true
        val project = e.project ?: return
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val configGroups = configGroupService.getConfigGroups().values.filter { it.changed }
        e.presentation.isEnabled = configGroups.isNotEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val configGroups = configGroupService.getConfigGroups().values.filter { it.changed }
        configGroups.forEach { configGroup -> configGroup.changed = false }
        configGroupService.refreshConfigGroupsAsync(configGroups)
    }
}
