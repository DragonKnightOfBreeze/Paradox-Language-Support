package icu.windea.pls.model

import com.fasterxml.jackson.module.kotlin.readValue
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.lang.PlsDataProvider
import org.junit.Assume
import org.junit.Test
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

class ParadoxLauncherSettingsInfoTest {
    @Test
    fun readLauncherSettings_fromString() {
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

        val model = ObjectMappers.jsonMapper.readValue<ParadoxLauncherSettingsInfo>(launcherSettingsJson)
        doAssert(model)
    }

    @Test
    fun readLauncherSettings_fromLocal_ifExists() {
        val gameDataDir = PlsDataProvider().getGameDataPath(ParadoxGameType.Stellaris.title)
        Assume.assumeTrue("Skip: gameDataDir not found", gameDataDir != null)
        gameDataDir!!

        val file1 = gameDataDir.resolve("launcher-settings.json")
        val file2 = gameDataDir.resolve("launcher/launcher-settings.json")
        val file = listOf(file1, file2).firstOrNull { it.exists() && it.isRegularFile() }
        Assume.assumeTrue("Skip: launcher-settings.json not found", file != null)

        val model = ObjectMappers.jsonMapper.readValue(file!!.toFile(), ParadoxLauncherSettingsInfo::class.java)
        doAssert(model)
    }

    private fun doAssert(model: ParadoxLauncherSettingsInfo) {
        // 基本断言：gameId 和路径存在（不必验证有效性）
        assert(model.gameId.isNotEmpty())
        println("launcher-settings.json -> gameId=${model.gameId}, version=${model.version}, dataPath=${model.gameDataPath}")
    }
}
