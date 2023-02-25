package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*

@Service(Service.Level.APP)
@State(name = "ParadoxProfilesSettings", storages = [Storage("paradox-language-support.xml")])
class ParadoxProfilesSettings : SimplePersistentStateComponent<ParadoxProfilesSettingsState>(ParadoxProfilesSettingsState())

class ParadoxProfilesSettingsState : BaseState() {
    @get:Property(surroundWithTag = false)
    @get:MapAnnotation(entryTagName = "modDescriptorSettings", keyAttributeName = "path", surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false)
    val modDescriptorSettings: MutableMap<String, ParadoxModDescriptorSettingsState> by linkedMap()
    @get:Property(surroundWithTag = false)
    @get:MapAnnotation(entryTagName = "gameSettings", keyAttributeName = "path", surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false)
    val gameSettings: MutableMap<String, ParadoxGameSettingsState> by linkedMap()
    @get:Property(surroundWithTag = false)
    @get:MapAnnotation(entryTagName = "modSettings", keyAttributeName = "path", surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false)
    val modSettings: MutableMap<String, ParadoxModSettingsState> by linkedMap()
    
    fun updateSettings() = incrementModificationCount()
}

/**
 * @see ParadoxModDescriptorInfo
 */
@Tag("settings")
class ParadoxModDescriptorSettingsState : BaseState() {
    var name: String? by string()
    var version: String? by string()
    var supportedVersion: String? by string()
    var picture: String? by string()
    var tags: MutableSet<String> by stringSet()
    var remoteId: String? by string()
    var gameType: ParadoxGameType? by enum()
    var source: ParadoxModSource by enum(ParadoxModSource.Local)
    var modDirectory: String? by string()
    
    fun fromDescriptorInfo(descriptorInfo: ParadoxModDescriptorInfo) {
        name = descriptorInfo.name.takeIfNotEmpty() ?: PlsBundle.message("mod.name.unnamed")
        version = descriptorInfo.version
        supportedVersion = descriptorInfo.supportedVersion
        picture = descriptorInfo.picture
        tags = descriptorInfo.tags.orEmpty().toMutableSet()
        remoteId = descriptorInfo.remoteFileId
        if(remoteId != null) source = ParadoxModSource.Steam
    }
}

interface ParadoxModDescriptorAwareSettingsState {
    val modDescriptorSettings: ParadoxModDescriptorSettingsState
    
    val name get() = modDescriptorSettings.name
    val version get() = modDescriptorSettings.version
    val supportedVersion get() = modDescriptorSettings.supportedVersion
    val picture get() = modDescriptorSettings.picture
    val tags get() = modDescriptorSettings.tags
    val remoteId get() = modDescriptorSettings.remoteId
    val gameType get() = modDescriptorSettings.gameType
    val source get() = modDescriptorSettings.source
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

@Tag("settings")
class ParadoxGameSettingsState : BaseState(), ParadoxGameOrModSettingsState {
    override var gameType: ParadoxGameType? by enum()
    override var gameVersion: String? by string()
    override var gameDirectory: String? by string()
    @get:XCollection(style = XCollection.Style.v2)
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
@Tag("settings")
class ParadoxModSettingsState : BaseState(), ParadoxGameOrModSettingsState, ParadoxModDescriptorAwareSettingsState {
    override var gameType: ParadoxGameType? by enum()
    override var gameVersion: String? by string()
    override var gameDirectory: String? by string()
    var modDirectory: String? by string()
    @get:XCollection(style = XCollection.Style.v2)
    override var modDependencies: MutableList<ParadoxModDependencySettingsState> by list()
    
    override val modDescriptorSettings: ParadoxModDescriptorSettingsState
        get() = getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())
    
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
@Tag("settings")
class ParadoxModDependencySettingsState : BaseState(), ParadoxModDescriptorAwareSettingsState {
    var modDirectory: String? by string()
    var enabled: Boolean by property(true)
    
    override val modDescriptorSettings: ParadoxModDescriptorSettingsState
        get() = getProfilesSettings().modDescriptorSettings.getValue(modDirectory.orEmpty())
}