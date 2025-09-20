package icu.windea.pls.ep.tools.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * `dlc_load.json` 的模型类。
 *
 * 参见：[DLCLoad.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Common/DLCLoad.cs)
 */
data class DlcLoadJson(
    @JsonProperty("disabled_dlcs")
    val disabledDlcs: List<String> = emptyList(),
    @JsonProperty("enabled_mods")
    val enabledMods: List<String> = emptyList(),
)

/**
 * `content_load.json` 的模型类。
 *
 * 参见：[ContentLoad.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Common/ContentLoad.cs)
 */
data class ContentLoadJson(
    @JsonProperty("disabledDLC")
    val disabledDlcs: List<DisabledDlc> = emptyList(),
    @JsonProperty("enabledMods")
    val enabledMods: List<EnabledMod> = emptyList(),
    @JsonProperty("enabledUGC")
    val enabledUgc: List<Any> = emptyList(), // 如果存在此属性，则为 V2
) {
    data class DisabledDlc(
        @JsonProperty("paradoxAppId")
        val paradoxAppId: String
    )

    data class EnabledMod(
        @JsonProperty("path")
        val path: String
    )
}

/**
 * `playlist.json` 的 V2 版本的模型类。
 *
 * 参见：[ModInfo.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Json/v2/ModInfo.cs)
 */
data class LauncherJsonV2(
    @JsonProperty("game")
    val game: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("mods")
    val mods: List<Mod> = emptyList(),
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Mod(
        @JsonProperty("displayName")
        val displayName: String,
        @JsonProperty("enabled")
        val enabled: Boolean,
        @JsonProperty("pdxId")
        val pdxId: String? = null,
        @JsonProperty("position")
        val position: String, // (i + 1 + 4096).toString(10).padStart(10, '0')
        @JsonProperty("steamId")
        val steamId: String? = null,
    )
}

/**
 * `playlist.json` 的 V3 版本的模型类。
 *
 * 参见：[ModInfo.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Json/v3/ModInfo.cs)
 */
data class LauncherJsonV3(
    @JsonProperty("game")
    val game: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("mods")
    val mods: List<Mod> = emptyList(),
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Mod(
        @JsonProperty("displayName")
        val displayName: String,
        @JsonProperty("enabled")
        val enabled: Boolean,
        @JsonProperty("pdxId")
        val pdxId: String? = null,
        @JsonProperty("position")
        val position: Int, // i
        @JsonProperty("steamId")
        val steamId: String? = null,
    )
}
