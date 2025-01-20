package icu.windea.pls.tools.model

import com.fasterxml.jackson.annotation.*

data class ParadoxLauncherJsonV2(
    val game: String,
    val mods: List<Mod>,
    val name: String,
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Mod(
        val displayName: String,
        val enabled: Boolean,
        val pdxId: String? = null,
        val position: String, // (i + 1 + 4096).toString(10).padStart(10, '0')
        val steamId: String? = null,
    )
}
