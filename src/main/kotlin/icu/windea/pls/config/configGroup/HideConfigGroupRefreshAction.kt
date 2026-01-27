package icu.windea.pls.config.configGroup

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fileInfo

// com.intellij.openapi.externalSystem.autoimport.HideProjectRefreshActions

class HideConfigGroupRefreshAction : DumbAwareAction() {
    init {
        templatePresentation.icon = AllIcons.Actions.Close
        templatePresentation.hoveredIcon = AllIcons.Actions.CloseHovered
        templatePresentation.text = PlsBundle.message("configGroup.refresh.action.hide.text")
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file?.fileInfo == null) return
        presentation.isVisible = true
        val project = e.project ?: return
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val configGroups = configGroupService.getConfigGroups().values.filter { it.changed }
        presentation.isEnabled = configGroups.isNotEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val configGroups = configGroupService.getConfigGroups().values.filter { it.changed }
        configGroups.forEach { configGroup -> configGroup.changed = false }
        configGroupService.updateRefreshStatus()
    }
}
