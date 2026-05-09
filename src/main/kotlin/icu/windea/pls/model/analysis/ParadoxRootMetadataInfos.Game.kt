package icu.windea.pls.model.analysis

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
    val gameDataPath: String = "", // ~/Paradox Interactive/{gameTitle}
    val modPath: String = "mod",
    val dlcPath: String = "",
    val exePath: String,
    val exeArgs: List<String> = emptyList(),
) : ParadoxRootMetadataInfo
