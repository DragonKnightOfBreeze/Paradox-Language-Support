package icu.windea.pls.core.tool

data class ParadoxLauncherJson(
    val game: String,
    val mods: List<Mod>,
    val name: String,
) {
    data class Mod(
        val displayName: String,
        val enabled: Boolean = true,
        val position: Int,
        val steamId: String,
    )
}