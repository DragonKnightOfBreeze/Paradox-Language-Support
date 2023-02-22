package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

class ParadoxRefreshOnModGameTypeChangedListener : ParadoxModGameTypeListener {
    override fun onChange(modSettings: ParadoxModSettingsState) {
        val gameType = modSettings.gameType
        
        //更新游戏类型信息缓存
        modSettings.modDirectory = null
        modSettings.modDependencies.forEach { it.modDirectory?.let { modDirectory -> refreshGameType(modDirectory, gameType) } }
        getProfilesSettings().updateSettings()
        
        //重新解析文件
        val modDirectories = mutableSetOf<String>()
        modSettings.modDependencies.forEach { it.modDirectory?.let { modDirectory -> modDirectories.add(modDirectory) } }
        runWriteAction { ParadoxCoreHandler.reparseFilesInRoot(modDirectories) }
        
        //检查是否也需要强制刷新inlayHints - 不需要
    }
    
    private fun refreshGameType(modDirectory: String, gameType: ParadoxGameType?) {
        val settings = getProfilesSettings().modDescriptorSettings.get(modDirectory) ?: return
        settings.gameType = gameType
    }
}
   

