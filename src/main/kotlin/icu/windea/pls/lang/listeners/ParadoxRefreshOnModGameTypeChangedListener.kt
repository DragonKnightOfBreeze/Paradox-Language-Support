package icu.windea.pls.lang.listeners

import com.intellij.openapi.application.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

/**
 * 当模组的游戏类型变更后，重新解析文件。
 */
class ParadoxRefreshOnModGameTypeChangedListener : ParadoxModGameTypeListener {
    override fun onChange(modSettings: ParadoxModSettingsState) {
        val gameType = modSettings.gameType
        
        //更新游戏类型信息缓存
        modSettings.modDirectory?.let { modDirectory -> refreshGameType(modDirectory, gameType) }
        modSettings.modDependencies.forEach { it.modDirectory?.let { modDirectory -> refreshGameType(modDirectory, gameType) } }
        getProfilesSettings().updateSettings()
        
        //重新解析文件（IDE之后会自动请求重新索引）
        val modDirectories = mutableSetOf<String>()
        modSettings.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) }
        modSettings.modDependencies.forEach { it.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) } }
        val files = ParadoxCoreHandler.findFilesByRootFilePaths(modDirectories)
        ParadoxCoreHandler.reparseFiles(files)
        
        //此时不需要刷新内嵌提示
    }
    
    private fun refreshGameType(modDirectory: String, gameType: ParadoxGameType?) {
        val settings = getProfilesSettings().modDescriptorSettings.get(modDirectory) ?: return
        settings.gameType = gameType
    }
}
   

