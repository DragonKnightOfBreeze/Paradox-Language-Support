package icu.windea.pls.ep.tools

import com.fasterxml.jackson.module.kotlin.readValue
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.ep.tools.model.ParadoxDlcLoadJson
import icu.windea.pls.ep.tools.model.ParadoxModImportData
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import icu.windea.pls.model.ParadoxModSource
import java.nio.file.Files
import java.nio.file.Path

/**
 * 从游戏数据目录下的 dlc_load.json 导入模组配置。
 *
 * 参见：[ParadoxImporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxImporter.cs)
 */
class ParadoxDlcLoadImporter : ParadoxModImporter {
    companion object {
        private const val collectionName = "Paradox"
        private val steamUgcRegex = Regex("^ugc_(\\d+)\\.mod$", RegexOption.IGNORE_CASE)
        private val relPathSteamUgcRegex = Regex("^.*/ugc_(\\d+)\\.mod$", RegexOption.IGNORE_CASE)
    }

    override val text: String = PlsBundle.message("mod.importer.game")

    override fun isAvailable() = true

    override fun importFromJson(jsonPath: Path): ParadoxModImportData {
        Files.newInputStream(jsonPath).use { input ->
            val data = ObjectMappers.jsonMapper.readValue<ParadoxDlcLoadJson>(input)
            val mods = data.enabledMods.map { raw ->
                val ugc = steamUgcRegex.find(raw)?.groupValues?.getOrNull(1)
                    ?: relPathSteamUgcRegex.find(raw)?.groupValues?.getOrNull(1)
                val source = if (ugc != null) ParadoxModSource.Steam else ParadoxModSource.Local
                ParadoxModInfo(
                    name = raw, // 交由 UI 解析成描述符路径
                    modDirectory = null,
                    remoteId = ugc,
                    source = source,
                    enabled = true,
                )
            }
            return ParadoxModImportData(
                gameId = null,
                collectionName = collectionName,
                mods = mods,
            )
        }
    }
}
