package icu.windea.pls.model.tools

import com.fasterxml.jackson.annotation.*

data class ParadoxDlcLoadJson(
    @JsonProperty("disabled_dlcs")
    val disabledDlcs: List<String>,
    @JsonProperty("enabled_mods")
    val enabledMods: List<String>
)
