package icu.windea.pls.ep.tools.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * `content_load.json` 的模型类。
 *
 * 参见：[ContentLoad.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Common/ContentLoad.cs)
 */
data class ContentLoadJson(
    @JsonProperty("disabledDLC")
    val disabledDlcs: List<DisabledDlc>,
    @JsonProperty("enabledMods")
    val enabledMods: List<EnabledMod>,
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
