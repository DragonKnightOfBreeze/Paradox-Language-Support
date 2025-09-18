package icu.windea.pls.ep.tools

import com.fasterxml.jackson.module.kotlin.readValue
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.ep.tools.model.ParadoxLauncherJsonV3
import icu.windea.pls.ep.tools.model.ParadoxModImportData
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import icu.windea.pls.model.ParadoxModSource
import java.nio.file.Files
import java.nio.file.Path

/**
 * 从 Paradox Launcher JSON（>= 2021.10）导入模组配置。
 */
class ParadoxLauncherJsonV3Importer : ParadoxModImporter {
    override val text: String = PlsBundle.message("mod.importer.launcherJson")

    override fun isAvailable() = true

    override fun importFromJson(jsonPath: Path): ParadoxModImportData {
        Files.newInputStream(jsonPath).use { input ->
            val data = ObjectMappers.jsonMapper.readValue<ParadoxLauncherJsonV3>(input)
            val mods = data.mods.sortedBy { it.position }.map { m ->
                val source = when {
                    m.steamId != null -> ParadoxModSource.Steam
                    m.pdxId != null -> ParadoxModSource.Paradox
                    else -> ParadoxModSource.Local
                }
                ParadoxModInfo(
                    name = m.displayName,
                    modDirectory = null,
                    remoteId = m.steamId ?: m.pdxId,
                    source = source,
                    enabled = m.enabled,
                )
            }
            return ParadoxModImportData(
                gameId = data.game,
                collectionName = data.name,
                mods = mods,
            )
        }
    }
}
