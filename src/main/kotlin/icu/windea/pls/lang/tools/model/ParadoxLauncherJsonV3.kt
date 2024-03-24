package icu.windea.pls.lang.tools.model

import com.fasterxml.jackson.annotation.*

data class ParadoxLauncherJsonV3(
    val game: String,
    val mods: List<Mod>,
    val name: String,
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Mod(
        val displayName: String,
        val enabled: Boolean,
        val pdxId: String? = null,
        val position: Int, // i
        val steamId: String? = null,
    )
}
