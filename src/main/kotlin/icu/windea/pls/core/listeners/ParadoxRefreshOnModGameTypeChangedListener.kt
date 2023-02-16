package icu.windea.pls.core.listeners

import icu.windea.pls.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

class ParadoxRefreshOnModGameTypeChangedListener : ParadoxModGameTypeListener {
    override fun onChange(modSettings: ParadoxModSettingsState) {
        val gameType = modSettings.gameType
        
        //更新游戏类型信息缓存
        modSettings.modDirectory?.let { modDirectory -> refreshGameType(modDirectory, gameType) }
        modSettings.modDependencies.keys.forEach { modDirectory -> refreshGameType(modDirectory, gameType) }
        
        //重新解析文件
        val modDirectories = mutableSetOf<String>()
        modSettings.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) }
        modSettings.modDependencies.keys.forEach { modDirectory -> modDirectories.add(modDirectory) }
        ParadoxCoreHandler.reparseFilesInRoot(modDirectories)
    }
    
    private fun refreshGameType(modDirectory: String, gameType: ParadoxGameType?) {
        val settings = getAllModSettings().descriptorSettings.get(modDirectory) ?: return
        settings.gameType = gameType
    }
}
   

