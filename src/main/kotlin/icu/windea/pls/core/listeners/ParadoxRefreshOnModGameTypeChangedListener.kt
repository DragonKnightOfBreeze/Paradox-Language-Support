package icu.windea.pls.core.listeners

import icu.windea.pls.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

class ParadoxRefreshOnModGameTypeChangedListener : ParadoxModGameTypeListener {
    override fun onChange(modSettings: ParadoxModSettingsState) {
        val gameType = modSettings.gameType
        
        //更新游戏类型信息缓存
        modSettings.modPath?.let { modPath -> refreshGameType(modPath, gameType) }
        modSettings.modDependencies.keys.forEach { modPath -> refreshGameType(modPath, gameType) }
        
        //重新解析文件
        val modPaths = mutableListOf<String>()
        modSettings.modPath?.let { modPath -> modPaths.add(modPath) }
        modSettings.modDependencies.keys.forEach { modPath -> modPaths.add(modPath) }
        ParadoxCoreHandler.reparseFilesInRoot(modPaths)
    }
    
    private fun refreshGameType(modPath: String, gameType: ParadoxGameType?) {
        val settings = getAllModSettings().descriptorSettings.get(modPath) ?: return
        settings.gameType = gameType
    }
}
   

