package icu.windea.pls.ep.tools.importer

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.ep.tools.model.LauncherJsonV2
import icu.windea.pls.ep.tools.model.LauncherJsonV3
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModInfo
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists

/**
 * 从官方启动器的 JSON 配置文件导入模组信息。
 *
 * 数据文件默认位于游戏数据目录的 `playlists` 子目录下，如 `playlist.json`。
 *
 * 首先尝试解析为 V2 版本，如果失败则再解析为 V3 版本。
 *
 * 参见：[ParadoxLauncherImporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxLauncherImporter.cs)
 */
class ParadoxLauncherJsonImporter : ParadoxJsonBasedModImporter() {
    override val text: String = PlsBundle.message("mod.importer.launcherJson")

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModImporter.Result {
        val gameType = modSetInfo.gameType
        val workshopDirPath = PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)
        if (workshopDirPath == null) {
            throw IllegalStateException(PlsBundle.message("mod.importer.error.steamWorkshopDir0"))
        }
        if (workshopDirPath.notExists()) {
            throw IllegalStateException(PlsBundle.message("mod.importer.error.steamWorkshopDir", workshopDirPath))
        }

        val newModInfos = mutableListOf<ParadoxModInfo>()
        val existingModDirectories = modSetInfo.mods.mapTo(mutableSetOf()) { it.modDirectory }

        // 尝试解析为 V2 版本
        run {
            val data = runCatching { readData(filePath, LauncherJsonV2::class.java) }.getOrNull() ?: return@run
            if (data.game != gameType.id && data.game != gameType.steamId) {
                throw IllegalStateException(PlsBundle.message("mod.importer.error.gameType"))
            }
            for (mod in data.mods.sortedBy { it.position }) {
                val modDirectory = ParadoxMetadataManager.getModDirectoryFromSteamId(mod.steamId, workshopDirPath)
                if (!existingModDirectories.add(modDirectory)) continue // 忽略已有的
                val newModInfo = ParadoxModInfo(modDirectory = modDirectory, enabled = mod.enabled)
                newModInfos.add(newModInfo)
            }
            val newModSetInfo = ParadoxModSetInfo(gameType, data.name, newModInfos)
            return ParadoxModImporter.Result(data.mods.size, newModInfos.size, newModSetInfo)
        }

        // 解析为 V3 版本
        val data = readData(filePath, LauncherJsonV3::class.java)
        if (data.game != gameType.id && data.game != gameType.steamId) {
            throw IllegalStateException(PlsBundle.message("mod.importer.error.gameType"))
        }
        for (mod in data.mods.sortedBy { it.position }) {
            val modDirectory = ParadoxMetadataManager.getModDirectoryFromSteamId(mod.steamId, workshopDirPath)
            if (!existingModDirectories.add(modDirectory)) continue // 忽略已有的
            val newModInfo = ParadoxModInfo(modDirectory = modDirectory, enabled = mod.enabled)
            newModInfos.add(newModInfo)
        }
        val newModSetInfo = ParadoxModSetInfo(gameType, data.name, newModInfos)
        return ParadoxModImporter.Result(data.mods.size, newModInfos.size, newModSetInfo)
    }

    override fun createFileChooserDescriptor(gameType: ParadoxGameType): FileChooserDescriptor {
        return FileChooserDescriptorFactory.createSingleFileDescriptor("json")
            .withTitle(PlsBundle.message("mod.importer.launcherJson.title"))
    }

    override fun getSelectedFile(gameType: ParadoxGameType): Path? {
        // 游戏数据目录中的 playlists/playlist.json，或者 playlists 目录
        val jsonFileName = getJsonFileName()
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)?.takeIf { it.exists() } ?: return null
        val playlistPath = gameDataPath.resolve(Constants.playlistsName).takeIf { it.exists() } ?: return null
        val playlistJsonPath = playlistPath.resolve(jsonFileName).takeIf { it.exists() }
        if (playlistJsonPath != null) return playlistJsonPath
        return playlistPath
    }

    private fun getJsonFileName(): String {
        return Constants.playlistJsonName
    }
}
