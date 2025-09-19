package icu.windea.pls.ep.tools.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * `dlc_load.json` 的模型类。
 *
 * 参见：[DLCLoad.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Models/Paradox/Common/DLCLoad.cs)
 */
data class DlcLoadJson(
    @JsonProperty("disabled_dlcs")
    val disabledDlcs: List<String>,
    @JsonProperty("enabled_mods")
    val enabledMods: List<String>,
)

