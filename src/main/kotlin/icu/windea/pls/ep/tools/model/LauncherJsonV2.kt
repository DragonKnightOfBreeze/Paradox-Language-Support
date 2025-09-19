package icu.windea.pls.ep.tools.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * `playlist.json` 的 V2 版本的模型类。
 *
 * 参见：[ModInfo.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Json/v2/ModInfo.cs)
 */
data class LauncherJsonV2(
    @JsonProperty("game")
    val game: String,
    @JsonProperty("mods")
    val mods: List<Mod>,
    @JsonProperty("name")
    val name: String,
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
        val position: String, // (i + 1 + 4096).toString(16).padStart(10, '0')
        @JsonProperty("steamId")
        val steamId: String? = null,
    )
}
