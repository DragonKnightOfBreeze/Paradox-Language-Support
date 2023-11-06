package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.model.*

/**
 * 当根目录信息被添加时，同步相关配置。
 */
class ParadoxUpdateSettingsOnRootInfoChangedListener : ParadoxRootInfoListener {
    override fun onAdd(rootInfo: ParadoxRootInfo) {
        when(rootInfo) {
            is ParadoxGameRootInfo -> addGameSettings(rootInfo)
            is ParadoxModRootInfo -> addModSettings(rootInfo)
        }
    }
    
    private fun addGameSettings(rootInfo: ParadoxGameRootInfo) {
        val settings = getProfilesSettings()
        val gameFile = rootInfo.rootFile
        val gameDirectory = gameFile.path
        var gameDescriptorSettings = settings.gameDescriptorSettings.get(gameDirectory)
        if(gameDescriptorSettings == null) {
            gameDescriptorSettings = ParadoxGameDescriptorSettingsState()
            gameDescriptorSettings.fromRootInfo(rootInfo)
            settings.gameDescriptorSettings.put(gameDirectory, gameDescriptorSettings)
            settings.updateSettings()
        } else {
            gameDescriptorSettings.fromRootInfo(rootInfo)
            settings.updateSettings()
        }
        
        var gameSettings = settings.gameSettings.get(gameDirectory)
        if(gameSettings == null) {
            gameSettings = ParadoxGameSettingsState()
            gameSettings.gameType = gameDescriptorSettings.gameType
            gameSettings.gameDirectory = gameDescriptorSettings.gameDirectory
            settings.gameSettings.put(gameDirectory, gameSettings)
            settings.updateSettings()
            
            ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxGameSettingsListener.TOPIC).onAdd(gameSettings)
        }
    }
    
    private fun addModSettings(rootInfo: ParadoxModRootInfo) {
        val settings = getProfilesSettings()
        val modFile = rootInfo.rootFile
        val modDirectory = modFile.path
        var modDescriptorSettings = settings.modDescriptorSettings.get(modDirectory)
        if(modDescriptorSettings == null) {
            modDescriptorSettings = ParadoxModDescriptorSettingsState()
            modDescriptorSettings.fromRootInfo(rootInfo)
            settings.modDescriptorSettings.put(modDirectory, modDescriptorSettings)
            settings.updateSettings()
        } else {
            modDescriptorSettings.fromRootInfo(rootInfo)
            settings.updateSettings()
        }
        
        var modSettings = settings.modSettings.get(modDirectory)
        if(modSettings == null) {
            modSettings = ParadoxModSettingsState()
            modSettings.gameType = modDescriptorSettings.gameType
            modSettings.modDirectory = modDirectory
            settings.modSettings.put(modDirectory, modSettings)
            settings.updateSettings()
            
            ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onAdd(modSettings)
        }
    }
    
    override fun onRemove(rootInfo: ParadoxRootInfo) {
        
    }
}


