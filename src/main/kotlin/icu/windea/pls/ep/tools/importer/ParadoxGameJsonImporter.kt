package icu.windea.pls.ep.tools.importer

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.ep.tools.model.ContentLoadJson
import icu.windea.pls.ep.tools.model.DlcLoadJson
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModInfo
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists

/**
 * 从游戏的 JSON 配置文件导入模组信息。
 *
 * 数据文件默认位于游戏数据目录下，且按照游戏使用的模组描述符文件，选用不同的文件：
 * - `.metadata/metadata.json`（VIC3）：`content_load.json`
 * - `descriptor.mod`（其他游戏）：`dlc_load.json`
 *
 * 参见：[ParadoxImporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxImporter.cs)
 */
class ParadoxGameJsonImporter : ParadoxJsonBasedModImporter() {
    override val text: String = PlsBundle.message("mod.importer.game")

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModImporter.Result {
        val gameType = modSetInfo.gameType
        val gameDataDirPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
        if (gameDataDirPath == null) {
            throw IllegalStateException(PlsBundle.message("mod.importer.error.gameDataDir0"))
        }
        if (gameDataDirPath.notExists()) {
            throw IllegalStateException(PlsBundle.message("mod.importer.error.gameDataDir", gameDataDirPath))
        }

        val newModInfos = mutableListOf<ParadoxModInfo>()
        val existingModDirectories = modSetInfo.mods.mapNotNullTo(mutableSetOf()) { it.modDirectory?.orNull() }

        when {
            ParadoxMetadataManager.useDescriptorMod(gameType) -> {
                val data = readData(filePath, DlcLoadJson::class.java)
                for (item in data.enabledMods) {
                    val modDirectory = ParadoxMetadataManager.getModDirectoryFromModDescriptorPathInGameData(item, gameDataDirPath) ?: continue
                    if (!existingModDirectories.add(modDirectory)) continue // 忽略已有的
                    newModInfos.add(ParadoxModInfo(modDirectory))
                }
                val newModSetInfo = ParadoxModSetInfo(gameType, ParadoxModSetInfo.defaultName, newModInfos)
                return ParadoxModImporter.Result(total = data.enabledMods.size, actualTotal = newModInfos.size, newModSetInfo = newModSetInfo)
            }
            else -> {
                val data = readData(filePath, ContentLoadJson::class.java)
                for (item in data.enabledMods) {
                    val modDirectory = ParadoxMetadataManager.getModDirectoryFromModDescriptorPathInGameData(item.path, gameDataDirPath) ?: continue
                    if (!existingModDirectories.add(modDirectory)) continue // 忽略已有的
                    newModInfos.add(ParadoxModInfo(modDirectory))
                }
                val newModSetInfo = ParadoxModSetInfo(gameType, ParadoxModSetInfo.defaultName, newModInfos)
                return ParadoxModImporter.Result(total = data.enabledMods.size, actualTotal = newModInfos.size, newModSetInfo = newModSetInfo)
            }
        }
    }

    override fun createFileChooserDescriptor(gameType: ParadoxGameType): FileChooserDescriptor {
        val jsonFileName = getJsonFileName(gameType)
        return FileChooserDescriptorFactory.createSingleFileDescriptor("json")
            .withTitle(PlsBundle.message("mod.importer.game.title", jsonFileName))
    }

    override fun getSelectedFile(gameType: ParadoxGameType): Path? {
        // 对应的 JSON 文件，或者游戏数据目录
        val jsonFileName = getJsonFileName(gameType)
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)?.takeIf { it.exists() } ?: return null
        val gameJsonPath = gameDataPath.resolve(jsonFileName).takeIf { it.exists() }
        if (gameJsonPath != null) return gameJsonPath
        return gameDataPath
    }

    private fun getJsonFileName(gameType: ParadoxGameType): String {
        return when {
            ParadoxMetadataManager.useDescriptorMod(gameType) -> Constants.dlcLoadPath
            else -> Constants.contentLoadPath
        }
    }
}
