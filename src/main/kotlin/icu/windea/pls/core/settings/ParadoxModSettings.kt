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
 */
class ParadoxModSettingsState : BaseState() {
    var gameType: ParadoxGameType? by enum()
    var modDirectory: String? by string()
    var gameDirectory: String? by string()
    val modDependencies: MutableList<ParadoxModDependencySettingsState> by list() //需要保证排序和去重
}

/**
 * 单个模组依赖的配置。
 * 
 * 始终将模组放到自身的模组依赖列表中，其排序可以调整。
 * 
 * @property selected 用于以后的基于模组列表运行游戏的功能。
 */
class ParadoxModDependencySettingsState : BaseState() {
    var modDirectory: String? by string()
    var selected: Boolean by property(true)
}