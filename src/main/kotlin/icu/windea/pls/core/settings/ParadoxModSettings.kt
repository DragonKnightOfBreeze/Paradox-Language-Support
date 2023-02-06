package icu.windea.pls.core.settings

import com.intellij.openapi.components.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*

@State(name = "ParadoxModSettings", storages = [Storage("paradox-language-support.mods.xml")])
class ParadoxModSettings: SimplePersistentStateComponent<ParadoxModSettingsState>(ParadoxModSettingsState())

class ParadoxModSettingsState: BaseState() {
    val settings: MutableMap<String, ParadoxModSettingState> by map() //path > setting
}

/**
 * 单个模组的配置。
 * @property modDependencies 模组依赖。不包括游戏目录和本模组。
 * @property orderInDependencies 进行模组排序时，当前模组在模组列表中的位置。从0开始。-1表示放在列表的最后。
 */
class ParadoxModSettingState: BaseState() {
    var name: String? by string()
    var version: String? by string()
    var path: String? by string()
    var gameType: ParadoxGameType by enum(getSettings().defaultGameType)
    var gameDirectory: String? by string()
    val modDependencies: MutableList<ParadoxModDependencySettingState> by list()
    var orderInDependencies: Int by property(-1)
}

/**
 * 单个模组依赖的配置。
 */
class ParadoxModDependencySettingState: BaseState() {
    var name: String? by string()
    var version: String? by string()
    var path: String? by string()
    var gameType: ParadoxGameType by enum(getSettings().defaultGameType)
}