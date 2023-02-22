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

interface ParadoxGameOrModSettingsState {
    var gameType: ParadoxGameType?
    var gameVersion: String?
    var gameDirectory: String?
    var modDependencies: MutableList<ParadoxModDependencySettingsState>
    
    val qualifiedName: String
    
    fun copyModDependencies(): MutableList<ParadoxModDependencySettingsState> {
        return modDependencies.mapTo(mutableListOf()) { ParadoxModDependencySettingsState().apply { copyFrom(it) } }
    }
}

class ParadoxGameSettingsState : BaseState(), ParadoxGameOrModSettingsState {
    override var gameType: ParadoxGameType? by enum()
    override var gameVersion: String? by string()
    override var gameDirectory: String? by string()
    override var modDependencies: MutableList<ParadoxModDependencySettingsState> by list()
    
    override val qualifiedName: String
        get() = buildString {
            append(gameType.orDefault().description)
            append("@")
            append(gameVersion)
        }
}

/**
 * 单个模组的配置。
 * @property modDependencies 模组依赖。不包括游戏目录和本模组。
 */
class ParadoxModSettingsState : BaseState(), ParadoxGameOrModSettingsState {
    override var gameType: ParadoxGameType? by enum()
    override var gameVersion: String? by string()
    override var gameDirectory: String? by string()
    var modDirectory: String? by string()
    override var modDependencies: MutableList<ParadoxModDependencySettingsState> by list()
    
    val modDescriptorSettings: ParadoxModDescriptorSettingsState
        get() = getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())
    
    val name: String? get() = modDescriptorSettings.name
    val version: String? get() = modDescriptorSettings.version
    val supportedVersion: String? get() = modDescriptorSettings.supportedVersion
    val remoteFileId: String? get() = modDescriptorSettings.remoteFileId
    
    override val qualifiedName: String
        get() = buildString {
            append(gameType.orDefault().description).append(" Mod: ")
            append(name)
            version?.let { version -> append("@").append(version) }
        }
}

/**
 * 单个模组依赖的配置。
 *
 * 始终将模组放到自身的模组依赖列表中，其排序可以调整。
 *
 * @property enabled 用于以后的基于模组列表运行游戏的功能。
 */
class ParadoxModDependencySettingsState : BaseState() {
    var modDirectory: String? by string()
    var enabled: Boolean by property(true)
    
    val modDescriptorSettings: ParadoxModDescriptorSettingsState
        get() = getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())
    
    val name: String? get() = modDescriptorSettings.name
    val version: String? get() = modDescriptorSettings.version
    val supportedVersion: String? get() = modDescriptorSettings.supportedVersion
    val remoteFileId: String? get() = modDescriptorSettings.remoteFileId
    val gameType: ParadoxGameType? get() = modDescriptorSettings.gameType
}