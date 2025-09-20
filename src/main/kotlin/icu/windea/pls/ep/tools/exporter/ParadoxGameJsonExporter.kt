package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.fileChooser.FileSaverDescriptor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.normalizePath
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.ep.tools.model.ContentLoadJson
import icu.windea.pls.ep.tools.model.DlcLoadJson
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * 导出模组信息到游戏的 JSON 配置文件。
 *
 * 数据文件默认位于游戏数据目录下，且按照游戏使用的模组描述符文件，选用不同的文件：
 * - `.metadata/metadata.json`（VIC3）：`content_load.json`
 * - `descriptor.mod`（其他游戏）：`dlc_load.json`
 *
 * 参见：[JsonExporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/JsonExporter.cs)
 */
class ParadoxGameJsonExporter : ParadoxJsonBasedModExporter() {
    override val text: String = PlsBundle.message("mod.exporter.game")

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModExporter.Result {
        val gameType = modSetInfo.gameType
        val gameDataDirPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
            ?: throw IllegalStateException(PlsBundle.message("mod.importer.error.gameDataDir0"))

        val enabledMods = modSetInfo.mods.filter { it.enabled }

        // 构建 "modDirectory -> gameData 相对描述符路径" 的映射，用于尽可能输出准确的描述符路径
        val descriptorMapping = ParadoxMetadataManager.buildDescriptorMapping(gameDataDirPath)

        // 依据游戏类型选择不同的 JSON 结构
        return if (ParadoxMetadataManager.useDescriptorMod(gameType)) {
            // dlc_load.json: enabled_mods 是字符串列表
            val enabledModPaths = enabledMods.mapNotNull { modInfo ->
                val modDir = modInfo.modDirectory ?: return@mapNotNull null
                descriptorMapping[modDir.normalizePath()] ?: modInfo.remoteId?.let { "mod/ugc_${it}.mod" }
            }
            val data = DlcLoadJson(
                disabledDlcs = emptyList(),
                enabledMods = enabledModPaths,
            )
            writeData(filePath, data)
            ParadoxModExporter.Result(total = enabledMods.size, actualTotal = enabledModPaths.size)
        } else {
            // content_load.json: enabledMods 是对象列表，字段为 path
            val enabledModEntries = enabledMods.mapNotNull { modInfo ->
                val modDir = modInfo.modDirectory ?: return@mapNotNull null
                val path = descriptorMapping[modDir.normalizePath()] ?: modInfo.remoteId?.let { "mod/ugc_${it}.mod" }
                path?.let { ContentLoadJson.EnabledMod(path = it) }
            }
            val data = ContentLoadJson(
                disabledDlcs = emptyList(),
                enabledMods = enabledModEntries,
                enabledUgc = emptyList(),
            )
            writeData(filePath, data)
            ParadoxModExporter.Result(total = enabledMods.size, actualTotal = enabledModEntries.size)
        }
    }

    override fun createFileSaverDescriptor(gameType: ParadoxGameType): FileSaverDescriptor {
        val jsonFileName = getJsonFileName(gameType)
        return FileSaverDescriptor(PlsBundle.message("mod.exporter.game.title", jsonFileName), "", "json")
    }

    override fun getSavedBaseDir(gameType: ParadoxGameType): Path? {
        // 游戏数据目录
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)?.takeIf { it.exists() } ?: return null
        return gameDataPath
    }

    override fun getSavedFileName(gameType: ParadoxGameType): String {
        // 对应的 JSON 文件
        return getJsonFileName(gameType)
    }

    private fun getJsonFileName(gameType: ParadoxGameType): String {
        return when {
            ParadoxMetadataManager.useDescriptorMod(gameType) -> Constants.dlcLoadPath
            else -> Constants.contentLoadPath
        }
    }
}
