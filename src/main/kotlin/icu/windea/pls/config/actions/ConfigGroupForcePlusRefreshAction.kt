package icu.windea.pls.config.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.TooltipDescriptionProvider
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.lang.fileInfo
import kotlinx.coroutines.launch

// com.intellij.openapi.externalSystem.autoimport.ProjectRefreshAction

class ConfigGroupForcePlusRefreshAction : DumbAwareAction(), TooltipDescriptionProvider {
    init {
        templatePresentation.icon = PlsIcons.Actions.ForceRefreshConfigGroups
        templatePresentation.text = PlsBundle.message("configGroup.action.refresh.force.plus.text")
        templatePresentation.description = PlsBundle.message("configGroup.action.refresh.force.plus.desc")
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file?.fileInfo == null) return
        presentation.isEnabledAndVisible
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            // do first
            configGroupService.refreshBuiltInConfigFiles(project)
            // do second
            val configGroups = configGroupService.getConfigGroups().values
            configGroups.forEach { configGroup -> configGroup.changed = false }
            configGroupService.refreshConfigGroupsAsync(configGroups)
        }
    }
}
