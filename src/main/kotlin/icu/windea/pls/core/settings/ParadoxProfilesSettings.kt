package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*

@State(name = "ParadoxProfilesSettings", storages = [Storage("paradox-language-support.profiles.xml")])
class ParadoxProfilesSettings : SimplePersistentStateComponent<ParadoxProfilesSettingsState>(ParadoxProfilesSettingsState())

class ParadoxProfilesSettingsState : BaseState() {
    val modDescriptorSettings: MutableMap<String, ParadoxModDescriptorSettingsState> by map()
    val gameSettings: MutableMap<String, ParadoxGameSettingsState> by map()
    val modSettings: MutableMap<String, ParadoxModSettingsState> by map()
    
    fun updateSettings() = incrementModificationCount()
}

/**
 * @see ParadoxModDescriptorInfo
 */
class ParadoxModDescriptorSettingsState : BaseState() {
    var name: String? by string()
    var version: String? by string()
    var picture: String? by string()
    var supportedVersion: String? by string()
    var remoteFileId: String? by string()
    var gameType: ParadoxGameType? by enum()
    var modDirectory: String? by string()
}

class ParadoxGameSettingsState : BaseState() {
    var gameType: ParadoxGameType? by enum()
    var gameDirectory: String? by string()
    val modDependencies: MutableList<ParadoxModDependencySettingsState> by list() //需要保证排序和去重
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
    
    var name by getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())::name
    var version by getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())::version
    var supportedVersion by getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())::supportedVersion
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
    
    var name by getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())::name
    var version by getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())::version
    var supportedVersion by getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())::supportedVersion
    var gameType by getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())::gameType
}