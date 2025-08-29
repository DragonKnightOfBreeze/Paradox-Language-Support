package icu.windea.pls.ep.tools.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ParadoxDlcLoadJson(
    @JsonProperty("disabled_dlcs")
    val disabledDlcs: List<String>,
    @JsonProperty("enabled_mods")
    val enabledMods: List<String>
)
