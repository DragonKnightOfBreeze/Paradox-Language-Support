package icu.windea.pls.ep.tools

import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.ep.tools.model.ParadoxLauncherJsonV3
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import icu.windea.pls.model.ParadoxModSource

/**
 * 导出模组配置为 Paradox Launcher JSON（>= 2021.10）。
 *
 * 参见：[ParadoxLauncherExporter202110.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter202110.cs)
 */
class ParadoxLauncherJsonV3Exporter : ParadoxModExporter {
    override val text: String = PlsBundle.message("mod.exporter.launcherJson.v3")


    override fun isAvailable() = true

    override fun toJson(gameId: String, collectionName: String, mods: List<ParadoxModInfo>): String {
        val valid = mods.filter { it.source != ParadoxModSource.Local }
        val json = ParadoxLauncherJsonV3(
            game = gameId,
            mods = valid.mapIndexed { i, m ->
                ParadoxLauncherJsonV3.Mod(
                    displayName = m.name.orEmpty(),
                    enabled = m.enabled,
                    position = i,
                    steamId = m.remoteId?.takeIf { m.source == ParadoxModSource.Steam },
                    pdxId = m.remoteId?.takeIf { m.source == ParadoxModSource.Paradox },
                )
            },
            name = collectionName,
        )
        return ObjectMappers.jsonMapper.writeValueAsString(json)
    }
}
