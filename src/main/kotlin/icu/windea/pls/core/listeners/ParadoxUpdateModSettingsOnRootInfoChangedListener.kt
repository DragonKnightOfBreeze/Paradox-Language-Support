package icu.windea.pls.core.listeners

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

/**
 * 当根信息被添加或移除时，同步更改模组配置并更新库信息。
 */
class ParadoxUpdateModSettingsOnRootInfoChangedListener: ParadoxRootInfoListener {
    override fun onAdd(rootInfo: ParadoxRootInfo) {
        if(rootInfo !is ParadoxModRootInfo) return
        val modSettings = createModSettings(rootInfo)
        val projects = ProjectManager.getInstance().openProjects
        for(project in projects) {
            val allModSettings = getAllModSettings()
            allModSettings.addSetting(rootInfo.rootFile.path, modSettings)
            allModSettings.roots = allModSettings.computeRoots()
        }
    }
    
    private fun createModSettings(rootInfo: ParadoxModRootInfo): ParadoxModSettingsState {
        val modSettings = ParadoxModSettingsState()
        val descriptorInfo = rootInfo.descriptorInfo
        modSettings.name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
        modSettings.version = descriptorInfo.version?.takeIfNotEmpty()
        modSettings.supportedVersion = descriptorInfo.supportedVersion?.takeIfNotEmpty()
        modSettings.modPath = rootInfo.rootFile.path
        return modSettings
    }
    
    override fun onRemove(rootInfo: ParadoxRootInfo) {
        if(rootInfo !is ParadoxModRootInfo) return
        val projects = ProjectManager.getInstance().openProjects
        for(project in projects) {
            val allModSettings = getAllModSettings()
            allModSettings.removeSetting(rootInfo.rootFile.path)
            allModSettings.roots = allModSettings.computeRoots()
        }
    }
}