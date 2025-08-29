package icu.windea.pls.lang.util

import com.fasterxml.jackson.module.kotlin.readValue
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.model.ParadoxLauncherSettingsInfo
import org.junit.Test

class ParadoxMetadataTest {
    val launcherSettingsJson = """
{
  "gameId": "stellaris",
  "version": "Phoenix v4.0.16 (05ae)",
  "rawVersion": "v4.0.16",
  "distPlatform": "steam",
  "gameDataPath": "%USER_DOCUMENTS%/Paradox Interactive/Stellaris",
  "themeFile": "./assets/theme-settings.json",
  "browserDlcUrl": "https://store.steampowered.com/dlc/281990/Stellaris/",
  "exePath": "./stellaris.exe",
  "exeArgs": [
    "-gdpr-compliant"
  ],
  "ingameSettingsLayoutPath": "./settings-layout.json",
  "alternativeExecutables": [
    {
      "exePath": "./stellaris.exe",
      "exeArgs": [
        "-gdpr-compliant",
        "-nakama"
      ],
      "label": {
        "de": "Shopübergreifender Mehrspielermodus",
        "en": "Cross-Store Multiplayer",
        "es": "Juego cruzado entre plataformas",
        "fr": "Multijoueur multiplateforme",
        "ja": "クロスストア・マルチプレイヤー",
        "ko": "크로스-스토어 멀티플레이어",
        "pl": "Gra wieloosobowa (wszystkie platformy PC)",
        "pt": "Multijogador entre Lojas",
        "ru": "Кроссплатформенный сетевой режим",
        "tr": "Mağazalar Arası Multiplayer",
        "zh-hans": "跨平台多人联机",
        "zh-hant": "跨平台多人遊戲"
      },
      "visibleIn": [
        "GAME_SETTINGS",
        "HOME_PAGE"
      ]
    }
  ]
}
    """.trimIndent()

    @Test
    fun parseLauncherSettingsJsonTest() {
        val result = ObjectMappers.jsonMapper.readValue<ParadoxLauncherSettingsInfo>(launcherSettingsJson)
        println(result)
    }
}
