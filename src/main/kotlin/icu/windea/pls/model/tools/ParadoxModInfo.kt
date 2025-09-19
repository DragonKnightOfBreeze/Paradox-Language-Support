package icu.windea.pls.model.tools

import icu.windea.pls.core.orNull
import icu.windea.pls.lang.settings.ParadoxModDependencySettingsState
import icu.windea.pls.lang.settings.ParadoxModDescriptorSettingsState
import icu.windea.pls.model.ParadoxModSource

/**
 * 模组信息。
 *
 * @property modDirectory 模组根目录。
 * @property enabled 是否启用。
 * @property name 显示名称。如果为空，需要基于 [modDirectory] 进一步获取。
 * @property remoteId 远端 ID（Steam/Paradox 等平台上的 ID）。如果为空，需要基于 [modDirectory] 进一步获取。
 * @property source 模组来源（本地、Steam、Paradox Launcher 等）。如果为空，需要基于 [modDirectory] 进一步获取。
 * @property version 模组版本。如果为空，需要基于 [modDirectory] 进一步获取。
 * @property supportedVersion 兼容的游戏版本。如果为空，需要基于 [modDirectory] 进一步获取。
 */
data class ParadoxModInfo(
    val modDirectory: String? = null,
    val enabled: Boolean = true,
    val name: String? = null,
    val remoteId: String? = null,
    val source: ParadoxModSource? = null,
    val version: String? = null,
    val supportedVersion: String? = null,
)

fun ParadoxModDescriptorSettingsState.toModInfo(enabled: Boolean): ParadoxModInfo? {
    return ParadoxModInfo(
        modDirectory = modDirectory?.orNull() ?: return null,
        enabled = enabled,
        name = name,
        remoteId = remoteId,
        source = source,
        version = version,
        supportedVersion = supportedVersion,
    )
}

fun ParadoxModDependencySettingsState.toModInfo(): ParadoxModInfo? {
    return modDescriptorSettings?.toModInfo(enabled)
}

fun ParadoxModInfo.toModDependency(): ParadoxModDependencySettingsState? {
    val modDirectory = modDirectory?.orNull() ?: return null
    return ParadoxModDependencySettingsState().also {
        it.modDirectory = modDirectory
        it.enabled = enabled
    }
}
