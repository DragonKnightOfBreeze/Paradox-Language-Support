package icu.windea.pls.lang.configGroup

import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*

//com.intellij.openapi.externalSystem.autoimport.ProjectRefreshAction

@Suppress("DialogTitleCapitalization")
class ConfigGroupRefreshAction : DumbAwareAction(){
    init {
        templatePresentation.icon = AllIcons.Actions.Refresh
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
        refreshConfigGroups(configGroups, configGroupService)
        reparseFiles(configGroups)
    }
    
    private fun refreshConfigGroups(configGroups: List<CwtConfigGroup>, configGroupService: CwtConfigGroupService) {
        configGroups.forEach { configGroup ->
            configGroupService.refreshConfigGroup(configGroup.gameType)
        }
    }
    
    private fun reparseFiles(configGroups: List<CwtConfigGroup>) {
        //TODO 1.2.0+ 这里需要考虑优化：刷新CWT规则分组会导致重新索引相关文件，重新编制相关索引，刷新相关内嵌提示，等等
        
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
        
        //重新解析文件
        runWriteAction { ParadoxCoreHandler.reparseFilesByRootFilePaths(rootFilePaths) }
    }
    
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}