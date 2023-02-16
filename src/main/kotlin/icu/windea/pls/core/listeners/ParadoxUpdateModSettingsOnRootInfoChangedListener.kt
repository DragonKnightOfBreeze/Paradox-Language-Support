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
        val allModSettings = getAllModSettings()
        val descriptorInfo = rootInfo.descriptorInfo
        val modDirectory = rootInfo.rootFile.path
        var descriptorSettings = allModSettings.descriptorSettings.get(modDirectory)
        if(descriptorSettings != null) {
            descriptorSettings.name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
            descriptorSettings.version = descriptorInfo.version?.takeIfNotEmpty()
            descriptorSettings.supportedVersion = descriptorInfo.supportedVersion?.takeIfNotEmpty()
            descriptorSettings.modDirectory = rootInfo.rootFile.path
        } else {
            descriptorSettings = ParadoxModDescriptorSettingsState()
            descriptorSettings.name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
            descriptorSettings.version = descriptorInfo.version?.takeIfNotEmpty()
            descriptorSettings.supportedVersion = descriptorInfo.supportedVersion?.takeIfNotEmpty()
            descriptorSettings.modDirectory = rootInfo.rootFile.path
            allModSettings.descriptorSettings.put(modDirectory, descriptorSettings)
        }
        var settings = allModSettings.settings.get(modDirectory)
        if(settings == null) {
            settings = ParadoxModSettingsState()
            settings.gameType = descriptorSettings.gameType
            settings.modDirectory = modDirectory
            allModSettings.settings.put(modDirectory, settings)
            
            ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onAdd(settings)
        }
    }
    
    override fun onRemove(rootInfo: ParadoxRootInfo) {
        
    }
}