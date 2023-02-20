package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

/**
 * 当根信息被添加或移除时，同步游戏配置、模组配置和模组描述符配置。
 */
class ParadoxUpdateSettingsOnRootInfoChangedListener : ParadoxRootInfoListener {
    override fun onAdd(rootInfo: ParadoxRootInfo) {
        when(rootInfo) {
            is ParadoxModRootInfo -> addModSettings(rootInfo)
            is ParadoxGameRootInfo -> addGameSettings(rootInfo)
        }
    }
    
    private fun addGameSettings(rootInfo: ParadoxGameRootInfo) {
        val settings = getProfilesSettings()
        val gameDirectory = rootInfo.rootFile.path
        val launcherSettingsInfo = rootInfo.launcherSettingsInfo
        var gameSettings = settings.gameSettings.get(gameDirectory)
        if(gameSettings == null) {
            gameSettings = ParadoxGameSettingsState()
            gameSettings.gameType = rootInfo.gameType
            gameSettings.gameVersion = launcherSettingsInfo.rawVersion
            gameSettings.gameDirectory = gameDirectory
            settings.gameSettings.put(gameDirectory, gameSettings)
            //settings.updateSettings()
            
            ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxGameSettingsListener.TOPIC).onAdd(gameSettings)
        } else {
            gameSettings.gameVersion = launcherSettingsInfo.rawVersion
        }
    }
    
    private fun addModSettings(rootInfo: ParadoxModRootInfo) {
        val settings = getProfilesSettings()
        val modDirectory = rootInfo.rootFile.path
        val descriptorInfo = rootInfo.descriptorInfo
        var descriptorSettings = settings.modDescriptorSettings.get(modDirectory)
        if(descriptorSettings != null) {
            syncModDescriptorSettings(descriptorSettings, descriptorInfo, rootInfo)
            settings.updateSettings()
        } else {
            descriptorSettings = ParadoxModDescriptorSettingsState()
            syncModDescriptorSettings(descriptorSettings, descriptorInfo, rootInfo)
            settings.modDescriptorSettings.put(modDirectory, descriptorSettings)
            //settings.updateSettings()
        }
        
        var modSettings = settings.modSettings.get(modDirectory)
        if(modSettings == null) {
            modSettings = ParadoxModSettingsState()
            modSettings.gameType = descriptorSettings.gameType
            modSettings.modDirectory = modDirectory
            settings.modSettings.put(modDirectory, modSettings)
            //settings.updateSettings()
            
            ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onAdd(modSettings)
        }
    }
    
    private fun syncModDescriptorSettings(descriptorSettings: ParadoxModDescriptorSettingsState, descriptorInfo: ParadoxModDescriptorInfo, rootInfo: ParadoxRootInfo) {
        descriptorSettings.name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
        descriptorSettings.version = descriptorInfo.version?.takeIfNotEmpty()
        descriptorSettings.picture = descriptorInfo.version?.takeIfNotEmpty()
        descriptorSettings.supportedVersion = descriptorInfo.supportedVersion?.takeIfNotEmpty()
        descriptorSettings.remoteFileId = descriptorInfo.remoteFileId?.takeIfNotEmpty()
        descriptorSettings.modDirectory = rootInfo.rootFile.path
    }
    
    override fun onRemove(rootInfo: ParadoxRootInfo) {
        
    }
}


