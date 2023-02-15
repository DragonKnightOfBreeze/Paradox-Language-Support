package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*

/**
 * 单个项目中所有模组的配置。
 */
@State(name = "ParadoxModSettings", storages = [Storage("paradox-language-support.mods.xml")])
class ParadoxAllModSettings : SimplePersistentStateComponent<ParadoxAllModSettingsState>(ParadoxAllModSettingsState())

class ParadoxAllModSettingsState : BaseState() {
    private val settings: MutableMap<String, ParadoxModSettingsState> by map() //modPath > modSettings
    
    fun getSetting(modPath: String): ParadoxModSettingsState? {
        return settings.get(modPath)
    }
    
    fun addSetting(modPath: String, modSettings: ParadoxModSettingsState) {
        settings.putIfAbsent(modPath, modSettings)
    }
    
    fun removeSetting(modPath: String) {
        settings.remove(modPath)
    }
    
    fun getDependencySetting(modPath: String): ParadoxModDependencySettingsState? {
        return settings.values.firstNotNullOfOrNull { it.modDependencies.get(modPath) }
    }
    
    //must also update roots when update settings
    
    var roots: MutableSet<VirtualFile> = computeRoots()
    
    fun computeRoots(): MutableSet<VirtualFile> {
        val result = mutableSetOf<VirtualFile>()
        val rootPaths = mutableSetOf<String>()
        settings.values.forEach { modSettings ->
            modSettings.modPath?.let {
                modSettings.gameDirectory?.let { path ->
                    rootPaths.add(path)
                }
                modSettings.modDependencies.keys.forEach { modPath ->
                    modPath.let { path ->
                        rootPaths.add(path)
                    }
                }
            }
        }
        rootPaths.forEach { rootPath ->
            val root = rootPath.toPathOrNull()?.let { VfsUtil.findFile(it, true) }
            if(root != null) result.add(root)
        }
        return result
    }
}

/**
 * 单个模组的配置。
 * @property modDependencies 模组依赖。不包括游戏目录和本模组。
 * @property orderInDependencies 进行模组排序时，当前模组在模组列表中的位置。从0开始。-1表示放在列表的最后。
 */
class ParadoxModSettingsState : BaseState() {
    var name: String? by string()
    var version: String? by string()
    var supportedVersion: String? by string()
    var gameType: ParadoxGameType by enum(getSettings().defaultGameType)
    var gameDirectory: String? by string()
    var modPath: String? by string()
    val modDependencies: MutableMap<String, ParadoxModDependencySettingsState> by map() //modPath > modDepencencySettings
    var orderInDependencies: Int by property(-1)
}

/**
 * 单个模组依赖的配置。
 */
class ParadoxModDependencySettingsState(val modSettings: ParadoxModSettingsState) : BaseState() {
    var name: String? by string()
    var version: String? by string()
    var supportedVersion: String? by string()
    val gameType: ParadoxGameType get() = modSettings.gameType
    var modPath: String? by string()
}