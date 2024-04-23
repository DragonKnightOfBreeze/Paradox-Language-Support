package icu.windea.pls.config.configGroup

import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.toolbar.floating.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*

//com.intellij.openapi.externalSystem.autoimport.HideProjectRefreshActions

class HideConfigGroupRefreshAction: DumbAwareAction() {
    init {
        templatePresentation.icon = AllIcons.Actions.Close
        templatePresentation.hoveredIcon = AllIcons.Actions.CloseHovered
        templatePresentation.text = PlsBundle.message("configGroup.refresh.action.hide.text")
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
        FloatingToolbarProvider.getProvider<ConfigGroupRefreshFloatingProvider>()
            .updateToolbarComponents(project)
    }
    
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}