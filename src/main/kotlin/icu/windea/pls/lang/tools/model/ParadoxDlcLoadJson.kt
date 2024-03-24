package icu.windea.pls.lang.tools.model

import com.fasterxml.jackson.annotation.*

data class ParadoxDlcLoadJson(
    @JsonProperty("disabled_dlcs")
    val disabledDlcs: List<String>,
    @JsonProperty("enabled_mods")
    val enabledMods: List<String>
)