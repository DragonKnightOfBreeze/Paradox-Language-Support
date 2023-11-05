package icu.windea.pls.lang.configGroup

import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*

@Suppress("DialogTitleCapitalization")
class CwtConfigGroupRefreshAction : DumbAwareAction(){
    init {
        templatePresentation.icon = AllIcons.Actions.Refresh
        templatePresentation.text = PlsBundle.message("configGroup.refresh.action.text")
        templatePresentation.description = PlsBundle.message("configGroup.refresh.action.desc1")
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if(file?.fileInfo == null) return
        presentation.isVisible = true
        val project = e.project ?: return
        presentation.description = PlsBundle.message("configGroup.refresh.action.desc2", project.name)
        val configGroupService = project.service<CwtConfigGroupService>()
        val changedConfigGroups = configGroupService.getConfigGroups().values.filter { it.changed.get() }
        presentation.isEnabled = changedConfigGroups.isNotEmpty()
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val configGroupService = project.service<CwtConfigGroupService>()
        val changedConfigGroups = configGroupService.getConfigGroups().values.filter { it.changed.get() }
        changedConfigGroups.forEach { configGroup ->
            configGroupService.refreshConfigGroup(configGroup.gameType)
        }
    }
    
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}