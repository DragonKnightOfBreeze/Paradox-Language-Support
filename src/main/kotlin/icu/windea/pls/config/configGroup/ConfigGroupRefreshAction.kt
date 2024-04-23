package icu.windea.pls.config.configGroup

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.toolbar.floating.*
import com.intellij.openapi.project.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*

//com.intellij.openapi.externalSystem.autoimport.ProjectRefreshAction

class ConfigGroupRefreshAction : DumbAwareAction(), TooltipDescriptionProvider {
    init {
        templatePresentation.icon = PlsIcons.Actions.RefreshConfigGroup
        templatePresentation.text = PlsBundle.message("configGroup.refresh.action.text")
        templatePresentation.description = PlsBundle.message("configGroup.refresh.action.desc")
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if(file?.fileInfo == null) return
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
        FloatingToolbarProvider.getProvider<ConfigGroupRefreshFloatingProvider>()
            .updateToolbarComponents(project)
    }
    
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}