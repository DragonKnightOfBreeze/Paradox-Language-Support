package icu.windea.pls.lang.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.orNull
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModSource
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constants.PlsConstants

/**
 * PLS 资料设置。
 *
 * 由插件自动根据游戏信息与模组信息进行配置。
 */
@Service(Service.Level.APP)
@State(name = "PlsProfilesSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsProfilesSettings : SimplePersistentStateComponent<PlsProfilesSettings.State>(State()) {
    fun getGameSettings(rootInfo: ParadoxRootInfo.Game): ParadoxGameSettingsState? {
        return state.gameSettings.get(rootInfo.rootFile.path)
    }

    fun getModSettings(rootInfo: ParadoxRootInfo.Mod): ParadoxModSettingsState? {
        return state.modSettings.get(rootInfo.rootFile.path)
    }

    fun getGameOrModSettings(rootInfo: ParadoxRootInfo): ParadoxGameOrModSettingsState? {
        return when (rootInfo) {
            is ParadoxRootInfo.Game -> getGameSettings(rootInfo)
            is ParadoxRootInfo.Mod -> getModSettings(rootInfo)
            else -> null
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): PlsProfilesSettings = service()
    }

    class State : BaseState() {
        @get:Property(surroundWithTag = false)
        @get:MapAnnotation(entryTagName = "gameDescriptorSettings", keyAttributeName = "path", surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false)
        val gameDescriptorSettings: MutableMap<String, ParadoxGameDescriptorSettingsState> by linkedMap()
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
}

/**
 * @see ParadoxRootInfo.Game
 */
@Tag("settings")
class ParadoxGameDescriptorSettingsState : BaseState() {
    var gameType: ParadoxGameType? by enum()
    var gameVersion: String? by string()
    var gameDirectory: String? by string()

    val finalGameType: ParadoxGameType get() = gameType ?: PlsSettings.getInstance().state.defaultGameType

    fun fromRootInfo(rootInfo: ParadoxRootInfo.Game) {
        gameDirectory = rootInfo.rootFile.path
        gameType = rootInfo.gameType
        gameVersion = rootInfo.version
    }
}

/**
 * @see ParadoxRootInfo.Mod
 */
@Tag("settings")
class ParadoxModDescriptorSettingsState : BaseState() {
    var name: String? by string()
    var version: String? by string()
    var supportedVersion: String? by string()
    var picture: String? by string()
    var tags: MutableSet<String> by stringSet()
    var remoteId: String? by string()
    var inferredGameType: ParadoxGameType? by enum()
    var gameType: ParadoxGameType? by enum()
    var source: ParadoxModSource by enum(ParadoxModSource.Local)
    var modDirectory: String? by string()

    val finalGameType: ParadoxGameType get() = inferredGameType ?: gameType ?: PlsSettings.getInstance().state.defaultGameType

    fun fromRootInfo(rootInfo: ParadoxRootInfo.Mod) {
        modDirectory = rootInfo.rootFile.path
        inferredGameType = rootInfo.inferredGameType
        if (inferredGameType != null) gameType = inferredGameType

        name = rootInfo.name.orNull() ?: PlsBundle.message("root.name.unnamed")
        version = rootInfo.version

        supportedVersion = rootInfo.supportedVersion
        picture = rootInfo.picture
        tags = rootInfo.tags.toMutableSet()
        remoteId = rootInfo.remoteId
        source = rootInfo.source
    }
}

sealed interface ParadoxGameOrModSettingsState {
    val gameType: ParadoxGameType?
    val gameDirectory: String?

    var options: ParadoxGameOrModOptionsSettingsState
    var modDependencies: MutableList<ParadoxModDependencySettingsState>

    val finalGameType: ParadoxGameType

    fun copyModDependencies(): MutableList<ParadoxModDependencySettingsState> {
        return modDependencies.mapTo(mutableListOf()) { ParadoxModDependencySettingsState().apply { copyFrom(it) } }
    }
}

interface ParadoxGameDescriptorAwareSettingsState {
    val gameDirectory: String?

    val gameDescriptorSettings: ParadoxGameDescriptorSettingsState?
        get() = gameDirectory?.let { PlsProfilesSettings.getInstance().state.gameDescriptorSettings.get(it) }

    val gameType: ParadoxGameType? get() = gameDescriptorSettings?.gameType
    val gameVersion: String? get() = gameDescriptorSettings?.gameVersion

    val finalGameType: ParadoxGameType get() = gameType ?: PlsSettings.getInstance().state.defaultGameType
}

interface ParadoxModDescriptorAwareSettingsState {
    val modDirectory: String?

    val modDescriptorSettings: ParadoxModDescriptorSettingsState?
        get() = modDirectory?.orNull()?.let { PlsProfilesSettings.getInstance().state.modDescriptorSettings.get(it) }

    val name: String? get() = modDescriptorSettings?.name
    val version: String? get() = modDescriptorSettings?.version
    val supportedVersion: String? get() = modDescriptorSettings?.supportedVersion
    val picture: String? get() = modDescriptorSettings?.picture
    val tags: MutableSet<String>? get() = modDescriptorSettings?.tags
    val remoteId: String? get() = modDescriptorSettings?.remoteId
    val inferredGameType: ParadoxGameType? get() = modDescriptorSettings?.inferredGameType
    val gameType: ParadoxGameType? get() = modDescriptorSettings?.gameType
    val source: ParadoxModSource? get() = modDescriptorSettings?.source

    val finalGameType: ParadoxGameType get() = inferredGameType ?: gameType ?: PlsSettings.getInstance().state.defaultGameType
}

@Tag("settings")
class ParadoxGameSettingsState : BaseState(), ParadoxGameOrModSettingsState, ParadoxGameDescriptorAwareSettingsState {
    override var gameType: ParadoxGameType? by enum()
    override var gameDirectory: String? by string()

    @get:Property(surroundWithTag = false)
    override var options: ParadoxGameOrModOptionsSettingsState by property(ParadoxGameOrModOptionsSettingsState())
    @get:XCollection(style = XCollection.Style.v2)
    override var modDependencies: MutableList<ParadoxModDependencySettingsState> by list()

    override val finalGameType: ParadoxGameType get() = gameType ?: PlsSettings.getInstance().state.defaultGameType
}

/**
 * 单个模组的设置。
 *
 * @property modDependencies 模组依赖。不包括游戏目录和本模组。
 */
@Tag("settings")
class ParadoxModSettingsState : BaseState(), ParadoxGameOrModSettingsState, ParadoxGameDescriptorAwareSettingsState, ParadoxModDescriptorAwareSettingsState {
    override var gameType: ParadoxGameType? by enum()
    override var gameDirectory: String? by string()
    override var modDirectory: String? by string()

    @get:Property(surroundWithTag = false)
    override var options: ParadoxGameOrModOptionsSettingsState by property(ParadoxGameOrModOptionsSettingsState())
    @get:XCollection(style = XCollection.Style.v2)
    override var modDependencies: MutableList<ParadoxModDependencySettingsState> by list()

    override val finalGameType: ParadoxGameType get() = inferredGameType ?: gameType ?: PlsSettings.getInstance().state.defaultGameType
    val finalGameDirectory: String? get() = gameDirectory?.orNull() ?: PlsSettings.getInstance().state.defaultGameDirectories[finalGameType.id]?.orNull()
}

/**
 * 单个模组依赖的设置。
 *
 * 始终将模组放到自身的模组依赖列表中，其排序可以调整。
 *
 * @property enabled 用于以后的基于模组列表运行游戏的功能。
 */
@Tag("settings")
class ParadoxModDependencySettingsState : BaseState(), ParadoxModDescriptorAwareSettingsState {
    override var modDirectory: String? by string()

    var enabled: Boolean by property(true)
}

val ParadoxGameOrModSettingsState.qualifiedName: String
    get() = when (this) {
        is ParadoxGameSettingsState -> buildString {
            append(finalGameType.title)
            if (gameVersion.isNotNullOrEmpty()) {
                append("@").append(gameVersion)
            }
        }
        is ParadoxModSettingsState -> buildString {
            append(finalGameType.title).append(" Mod: ")
            append(name?.orNull() ?: PlsBundle.message("root.name.unnamed"))
            if (version.isNotNullOrEmpty()) {
                append("@").append(version)
            }
        }
    }

/**
 * 游戏或模组的额外选项配置。
 *
 * @property disableTiger 在游戏或模组级别，是否禁用 [Tiger](https://github.com/amtep/tiger) 检查工具。
 */
@Tag("options")
class ParadoxGameOrModOptionsSettingsState : BaseState() {
    var disableTiger: Boolean by property(false)
}
