package icu.windea.pls.lang.configGroup

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.toolbar.floating.*
import com.intellij.openapi.project.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*

//com.intellij.openapi.externalSystem.autoimport.ProjectRefreshAction

@Suppress("DialogTitleCapitalization")
class ConfigGroupRefreshAction : DumbAwareAction() {
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
        
        configGroups.forEach { configGroup ->
            configGroupService.refreshConfigGroup(configGroup.gameType)
        }
        
        //重新解析已打开的文件
        ParadoxCoreHandler.reparseOpenedFiles()
        
        val action = NotificationAction.createSimple(PlsBundle.message("configGroup.refresh.notification.action.reindex")) {
            //重新解析文件（IDE之后会自动请求重新索引）
            //TODO 1.2.0+ 需要考虑优化 - 重新索引可能不是必要的，也可能仅需要重新索引少数几个文件
            val rootFilePaths = getRootFilePaths(configGroups)
            ParadoxCoreHandler.reparseFilesByRootFilePaths(rootFilePaths)
        }
        
        NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
            PlsBundle.message("configGroup.refresh.notification.title"),
            PlsBundle.message("configGroup.refresh.notification.content"),
            NotificationType.INFORMATION
        ).addAction(action).notify(project)
        
        configGroupService.getConfigGroups().values.forEach { it.changed.set(false) }
        
        FloatingToolbarProvider.getProvider<ConfigGroupRefreshFloatingProvider>()
            .updateToolbarComponents(project)
    }
    
    private fun getRootFilePaths(configGroups: List<CwtConfigGroup>): Set<String> {
        val gameTypes = configGroups.mapNotNullTo(mutableSetOf()) { it.gameType }
        val rootFilePaths = mutableSetOf<String>()
        getProfilesSettings().gameDescriptorSettings.values.forEach f@{ settings ->
            val gameType = settings.gameType ?: return@f
            if(gameType !in gameTypes) return@f
            settings.gameDirectory?.let { rootFilePaths.add(it) }
        }
        getProfilesSettings().modDescriptorSettings.values.forEach f@{ settings ->
            val gameType = settings.inferredGameType ?: settings.finalGameType
            if(gameType !in gameTypes) return@f
            settings.modDirectory?.let { rootFilePaths.add(it) }
        }
        return rootFilePaths
    }
    
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}