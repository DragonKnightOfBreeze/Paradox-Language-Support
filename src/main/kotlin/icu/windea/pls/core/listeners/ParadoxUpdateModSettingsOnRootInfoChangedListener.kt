package icu.windea.pls.core.listeners

import com.intellij.openapi.application.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

/**
 * 当根信息被添加或移除时，同步模组描述符配置和模组配置。
 */
class ParadoxUpdateModSettingsOnRootInfoChangedListener: ParadoxRootInfoListener {
    override fun onAdd(rootInfo: ParadoxRootInfo) {
        if(rootInfo !is ParadoxModRootInfo) return
        val settings = getProfilesSettings()
        val descriptorInfo = rootInfo.descriptorInfo
        val modDirectory = rootInfo.rootFile.path
        var descriptorSettings = settings.modDescriptorSettings.get(modDirectory)
        if(descriptorSettings != null) {
            syncDescriptorSettings(descriptorSettings, descriptorInfo, rootInfo)
            settings.updateSettings()
        } else {
            descriptorSettings = ParadoxModDescriptorSettingsState()
            syncDescriptorSettings(descriptorSettings, descriptorInfo, rootInfo)
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
    
    private fun syncDescriptorSettings(descriptorSettings: ParadoxModDescriptorSettingsState, descriptorInfo: ParadoxModDescriptorInfo, rootInfo: ParadoxRootInfo) {
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

