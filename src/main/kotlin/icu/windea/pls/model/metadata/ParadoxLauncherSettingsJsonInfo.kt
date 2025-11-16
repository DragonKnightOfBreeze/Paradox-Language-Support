package icu.windea.pls.model.metadata

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * 启动器设置信息（`launcher-settings.json`）。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ParadoxLauncherSettingsJsonInfo(
    val gameId: String,
    val version: String? = null,
    val rawVersion: String? = null,
    val distPlatform: String = "steam",
    val gameDataPath: String = "", // %USER_DOCUMENTS%/Paradox Interactive/${ParadoxGameType.get(gameId)?.title}
    val modPath: String = "mod",
    val dlcPath: String = "",
    val exePath: String,
    val exeArgs: List<String> = emptyList()
) : ParadoxMetadataInfo
