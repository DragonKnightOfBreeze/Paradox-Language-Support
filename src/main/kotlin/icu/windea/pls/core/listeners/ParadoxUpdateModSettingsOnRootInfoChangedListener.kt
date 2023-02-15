package icu.windea.pls.core.listeners

import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

/**
 * 当根信息被添加或移除时，同步模组描述符配置。
 */
class ParadoxUpdateModSettingsOnRootInfoChangedListener: ParadoxRootInfoListener {
    override fun onAdd(rootInfo: ParadoxRootInfo) {
        if(rootInfo !is ParadoxModRootInfo) return
        val descriptorInfo = rootInfo.descriptorInfo
        val modPath = rootInfo.rootFile.path
        val settings = getAllModSettings().descriptorSettings.get(modPath)
        if(settings != null) {
            settings.name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
            settings.version = descriptorInfo.version?.takeIfNotEmpty()
            settings.supportedVersion = descriptorInfo.supportedVersion?.takeIfNotEmpty()
            settings.modPath = rootInfo.rootFile.path
        } else {
            val newSettings = ParadoxModDescriptorSettingsState()
            newSettings.name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
            newSettings.version = descriptorInfo.version?.takeIfNotEmpty()
            newSettings.supportedVersion = descriptorInfo.supportedVersion?.takeIfNotEmpty()
            newSettings.modPath = rootInfo.rootFile.path
            getAllModSettings().descriptorSettings.put(modPath, newSettings)
        }
    }
    
    override fun onRemove(rootInfo: ParadoxRootInfo) {
        
    }
}