package icu.windea.pls.ep.tools.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * `dlc_load.json` 的模型类。
 *
 * 参见：[DLCLoad.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Common/DLCLoad.cs)
 */
@Serializable
data class DlcLoadJson(
    @SerialName("disabled_dlcs")
    val disabledDlcs: List<String> = emptyList(),
    @SerialName("enabled_mods")
    val enabledMods: List<String> = emptyList(),
)

/**
 * `content_load.json` 的模型类。
 *
 * 参见：[ContentLoad.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Common/ContentLoad.cs)
 */
@Serializable
data class ContentLoadJson(
    @SerialName("disabledDLC")
    val disabledDlcs: List<DisabledDlc> = emptyList(),
    @SerialName("enabledMods")
    val enabledMods: List<EnabledMod> = emptyList(),
    @SerialName("enabledUGC")
    val enabledUgc: List<JsonElement> = emptyList(), // 如果存在此属性，则为 V2
) {
    @Serializable
    data class DisabledDlc(
        @SerialName("paradoxAppId")
        val paradoxAppId: String
    )

    @Serializable
    data class EnabledMod(
        @SerialName("path")
        val path: String
    )
}

/**
 * `playlist.json` 的 V2 版本的模型类。
 *
 * 参见：[ModInfo.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Json/v2/ModInfo.cs)
 */
@Serializable
data class LauncherJsonV2(
    @SerialName("game")
    val game: String,
    @SerialName("name")
    val name: String,
    @SerialName("mods")
    val mods: List<Mod> = emptyList(),
) {
    @Serializable
    data class Mod(
        @SerialName("displayName")
        val displayName: String,
        @SerialName("enabled")
        val enabled: Boolean,
        @SerialName("pdxId")
        val pdxId: String? = null,
        @SerialName("position")
        val position: String, // (i + 1 + 4096).toString(16).padStart(10, '0')
        @SerialName("steamId")
        val steamId: String? = null,
    )
}

/**
 * `playlist.json` 的 V3 版本的模型类。
 *
 * 参见：[ModInfo.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Json/v3/ModInfo.cs)
 */
@Serializable
data class LauncherJsonV3(
    @SerialName("game")
    val game: String,
    @SerialName("name")
    val name: String,
    @SerialName("mods")
    val mods: List<Mod> = emptyList(),
) {
    @Serializable
    data class Mod(
        @SerialName("displayName")
        val displayName: String,
        @SerialName("enabled")
        val enabled: Boolean,
        @SerialName("pdxId")
        val pdxId: String? = null,
        @SerialName("position")
        val position: Int, // i
        @SerialName("steamId")
        val steamId: String? = null,
    )
}
