package icu.windea.pls.config.configGroup

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.TooltipDescriptionProvider
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.fileInfo

//com.intellij.openapi.externalSystem.autoimport.ProjectRefreshAction

class ConfigGroupRefreshAction : DumbAwareAction(), TooltipDescriptionProvider {
    init {
        templatePresentation.icon = PlsIcons.Actions.RefreshConfigGroups
        templatePresentation.text = PlsBundle.message("configGroup.refresh.action.text")
        templatePresentation.description = PlsBundle.message("configGroup.refresh.action.desc")
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file?.fileInfo == null) return
        presentation.isVisible = true
        val project = e.project ?: return
        val configGroupService = project.service<CwtConfigGroupService>()
        val configGroups = configGroupService.getConfigGroups().values.filter { it.changed.get() }
        presentation.isEnabled = configGroups.isNotEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val configGroupService = project.service<CwtConfigGroupService>()
        val configGroups = configGroupService.getConfigGroups().values.filter { it.changed.get() }
        configGroups.forEach { configGroup -> configGroup.changed.set(false) }
        configGroupService.refreshConfigGroups(configGroups)
        configGroupService.updateRefreshFloatingToolbar()
    }
}

