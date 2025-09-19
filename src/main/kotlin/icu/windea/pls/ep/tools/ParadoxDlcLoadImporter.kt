package icu.windea.pls.ep.tools

import com.fasterxml.jackson.module.kotlin.readValue
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.ep.tools.model.DlcLoadJson
import icu.windea.pls.ep.tools.model.ParadoxModImportData
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import icu.windea.pls.model.ParadoxModSource
import java.nio.file.Files
import java.nio.file.Path

/**
 * 从游戏数据目录下的 dlc_load.json 导入模组配置。
 *
 * 简介：
 * - 读取 `dlc_load.json` 的 `enabled_mods` 列表，逐项解析。
 * - 当条目匹配 `ugc_*.mod`（或其相对路径形式）时，提取 UGC ID，来源映射为 [ParadoxModSource.Steam]；否则视为本地来源。
 * - 不直接解析描述符 `.mod` 指向的实际目录，统一交由 UI 层按需解析（见调用处）。
 *
 * 行为与限制：
 * - 仅导入“启用”的模组；集合名固定为 `"Paradox"`。
 * - 不读取 DLC 状态（`disabled_dlcs`），该字段不参与此处导入逻辑。
 */
class ParadoxDlcLoadImporter : ParadoxModImporter.JsonBased {
    companion object {
        private const val collectionName = "Paradox"
        private val steamUgcRegex = Regex("^ugc_(\\d+)\\.mod$", RegexOption.IGNORE_CASE)
        private val relPathSteamUgcRegex = Regex("^.*/ugc_(\\d+)\\.mod$", RegexOption.IGNORE_CASE)
    }

    override val text: String = PlsBundle.message("mod.importer.game")

    override fun isAvailable() = true

    override fun importFromJson(jsonPath: Path): ParadoxModImportData {
        Files.newInputStream(jsonPath).use { input ->
            val data = ObjectMappers.jsonMapper.readValue<DlcLoadJson>(input)
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

