package icu.windea.pls.ep.tools

import com.fasterxml.jackson.module.kotlin.readValue
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.ep.tools.model.LauncherJsonV2
import icu.windea.pls.ep.tools.model.ParadoxModImportData
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import icu.windea.pls.model.ParadoxModSource
import java.nio.file.Files
import java.nio.file.Path

/**
 * 从 Paradox Launcher JSON（< 2021.10，V2）导入模组配置。
 *
 * 简介：
 * - 读取 `playlist.json`（旧版启动器导出）并映射为平台无关的 [ParadoxModImportData]。
 * - V2 结构特点：`mods[].position` 为十六进制字符串（宽度 10），需要按十六进制转整数后排序。
 *
 * 行为：
 * - 排序：按照 `position` 的十六进制值升序排列。
 * - 来源：根据 `steamId/pdxId` 推断 [ParadoxModSource]，缺省为 Local。
 *
 * 参考：
 * - Irony Mod Manager: `ParadoxLauncherImporter.cs` 与 `ParadoxLauncherExporter.cs`
 */
class ParadoxLauncherJsonV2Importer : ParadoxModImporter.JsonBased {
    override val text: String = PlsBundle.message("mod.importer.launcherJson") + " (V2)"

    override fun isAvailable() = true

    override fun importFromJson(jsonPath: Path): ParadoxModImportData {
        Files.newInputStream(jsonPath).use { input ->
            val data = ObjectMappers.jsonMapper.readValue<LauncherJsonV2>(input)
            val mods = data.mods
                .sortedBy { it.position.toLong(16) }
                .map { m ->
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
