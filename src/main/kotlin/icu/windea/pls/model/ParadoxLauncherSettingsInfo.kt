package icu.windea.pls.model

data class ParadoxLauncherSettingsInfo(
    val gameId: String,
    val version: String? = null,
    val rawVersion: String? = null,
    val distPlatform: String = "steam",
    val gameDataPath: String = "%USER_DOCUMENTS%/Paradox Interactive/${ParadoxGameType.resolve(gameId)}",
    val modPath: String = "mod",
    val dlcPath: String = "",
    val exePath: String,
    val exeArgs: List<String> = emptyList()
)
