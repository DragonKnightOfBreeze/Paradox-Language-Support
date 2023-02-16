package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import icu.windea.pls.lang.model.*

/**
 * 单个项目中所有模组的配置。
 */
@State(name = "ParadoxModSettings", storages = [Storage("paradox-language-support.mods.xml")])
class ParadoxAllModSettings : SimplePersistentStateComponent<ParadoxAllModSettingsState>(ParadoxAllModSettingsState())

class ParadoxAllModSettingsState : BaseState() {
    val descriptorSettings: MutableMap<String, ParadoxModDescriptorSettingsState> by map() 
    
    val settings: MutableMap<String, ParadoxModSettingsState> by map()
}

class ParadoxModDescriptorSettingsState: BaseState() {
    var name: String? by string()
    var version: String? by string()
    var supportedVersion: String? by string()
    var gameType: ParadoxGameType? by enum()
    var modDirectory: String? by string()
}

/**
 * 单个模组的配置。
 * @property modDependencies 模组依赖。不包括游戏目录和本模组。
 * @property orderInDependencies 进行模组排序时，当前模组在模组列表中的位置。从0开始。-1表示放在列表的最后。
 */
class ParadoxModSettingsState : BaseState() {
    var gameType: ParadoxGameType? by enum()
    var modDirectory: String? by string()
    var gameDirectory: String? by string()
    val modDependencies: MutableMap<String, ParadoxModDependencySettingsState> by map() //modDirectory > modDependencySettings
    var orderInDependencies: Int by property(-1)
    
    //有些地方需要用到列表（保证排序和去重）
    val modDependencyList: MutableList<ParadoxModDependencySettingsState> = mutableListOf()
}

/**
 * 单个模组依赖的配置。
 * 
 * 始终将模组放到自身的模组依赖列表中，其排序可以调整。
 * 
 * @property selected 用于以后的基于模组列表运行游戏的功能。
 * @property order 模组在模组列表中的顺序，从1开始。
 */
class ParadoxModDependencySettingsState : BaseState() {
    var modDirectory: String? by string()
    var selected: Boolean by property(true)
    var order: Int by property(-1)
}