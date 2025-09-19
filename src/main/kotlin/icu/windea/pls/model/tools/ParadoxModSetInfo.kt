package icu.windea.pls.model.tools

import icu.windea.pls.lang.settings.ParadoxModDependencySettingsState
import icu.windea.pls.model.ParadoxGameType

/**
 * 模组集信息。
 *
 * @property gameType 游戏类型。
 * @property name 模组集名称。来自启动器的播放集名称，或者使用默认名称。
 * @property mods 模组信息列表。
 */
data class ParadoxModSetInfo(
    val gameType: ParadoxGameType,
    val name: String,
    val mods: List<ParadoxModInfo> = emptyList(),
)

fun List<ParadoxModDependencySettingsState>.toModSetInfo(gameType: ParadoxGameType, name: String): ParadoxModSetInfo {
    return ParadoxModSetInfo(gameType, name, this.mapNotNullTo(mutableListOf()) { it.toModInfo() })
}

fun ParadoxModSetInfo.toModDependencies(): List<ParadoxModDependencySettingsState> {
    return this.mods.mapNotNull { it.toModDependency() }
}
