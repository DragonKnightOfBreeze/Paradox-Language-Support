package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

/**
 * 当根信息被添加或移除时，同步游戏配置、模组配置和模组描述符配置。
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
        val launcherSettingsInfo = rootInfo.launcherSettingsInfo
        var gameSettings = settings.gameSettings.get(gameDirectory)
        if(gameSettings == null) {
            gameSettings = ParadoxGameSettingsState()
            gameSettings.gameType = rootInfo.gameType
            gameSettings.gameVersion = launcherSettingsInfo.rawVersion
            gameSettings.gameDirectory = gameDirectory
            settings.gameSettings.put(gameDirectory, gameSettings)
            settings.updateSettings()
            
            ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxGameSettingsListener.TOPIC).onAdd(gameSettings)
        } else {
            gameSettings.gameVersion = launcherSettingsInfo.rawVersion
            settings.updateSettings()
        }
    }
    
    private fun addModSettings(rootInfo: ParadoxModRootInfo) {
        val settings = getProfilesSettings()
        val modFile = rootInfo.rootFile
        val modDirectory = modFile.path
        val descriptorInfo = rootInfo.descriptorInfo
        var modDescriptorSettings = settings.modDescriptorSettings.get(modDirectory)
        if(modDescriptorSettings != null) {
            modDescriptorSettings.fromDescriptorInfo(descriptorInfo)
            modDescriptorSettings.modDirectory = rootInfo.rootFile.path
            settings.updateSettings()
        } else {
            modDescriptorSettings = ParadoxModDescriptorSettingsState()
            modDescriptorSettings.fromDescriptorInfo(descriptorInfo)
            modDescriptorSettings.modDirectory = rootInfo.rootFile.path
            settings.modDescriptorSettings.put(modDirectory, modDescriptorSettings)
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


