package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*

/**
 * 单个项目中所有模组的配置。
 */
@State(name = "ParadoxModSettings", storages = [Storage("paradox-language-support.mods.xml")])
class ParadoxProjectModSettings(private val project: Project) : SimplePersistentStateComponent<ParadoxProjectModSettingsState>(ParadoxProjectModSettingsState())

class ParadoxProjectModSettingsState : BaseState() {
    val settings: MutableMap<String, ParadoxModSettingsState> by map() //path > setting
    
    fun getSetting(modPath: String): ParadoxModSettingsState? {
        return settings.get(modPath)
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
                modSettings.modDependencies.forEach { modDependency ->
                    modDependency.modPath?.let { path ->
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
    
    //val rootsWithModPaths: MutableMap<VirtualFile, MutableSet<String>> by lazy {
    //    val result = mutableMapOf<VirtualFile, MutableSet<String>>()
    //    val rootPathsWithModPaths = mutableMapOf<String, MutableSet<String>>()
    //    settings.values.forEach { modSettings ->
    //        modSettings.path?.let { modPath ->
    //            modSettings.gameDirectory?.let { path ->
    //                rootPathsWithModPaths.getOrPut(path) { mutableSetOf() }.add(modPath)
    //            }
    //            modSettings.modDependencies.forEach { modDependency ->
    //                modDependency.path?.let { path ->
    //                    rootPathsWithModPaths.getOrPut(path) { mutableSetOf() }.add(modPath)
    //                }
    //            }
    //        }
    //    }
    //    rootPathsWithModPaths.forEach { (rootPath, modPaths) ->
    //        if(modPaths.isEmpty()) return@forEach
    //        val root = rootPath.toPathOrNull()?.let { VfsUtil.findFile(it, true) }
    //        if(root != null) result.put(root, modPaths)
    //    }
    //    result
    //}
    //
    //val roots: Set<VirtualFile> get() = rootsWithModPaths.keys
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
    val modDependencies: MutableList<ParadoxModDependencySettingsState> by list()
    var orderInDependencies: Int by property(-1)
}

/**
 * 单个模组依赖的配置。
 */
class ParadoxModDependencySettingsState(val modSettings: ParadoxModSettingsState) : BaseState() {
    var name: String? by string()
    var version: String? by string()
    val gameType: ParadoxGameType get() = modSettings.gameType
    var modPath: String? by string()
}