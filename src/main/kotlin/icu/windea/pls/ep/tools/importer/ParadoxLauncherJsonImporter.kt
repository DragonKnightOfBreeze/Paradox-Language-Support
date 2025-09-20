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
 * 自动探测 `mods[].position` 的类型：
 * - 若为整数（Int），解析为 V3；
 * - 若为字符串（String），解析为 V2（排序时转为数值再比较）。
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

        // 自动探测：V2 (position:String) / V3 (position:Int)
        val isV3: Boolean? = ParadoxMetadataManager.detectLauncherPlaylistPositionIsInt(filePath)

        if (isV3 == true) {
            // 按 V3 解析：position 为 Int，排序按数值
            val data = readData(filePath, LauncherJsonV3::class.java)
            if (data.game != gameType.gameId) {
                throw IllegalStateException(PlsBundle.message("mod.importer.error.gameType"))
            }
            for (mod in data.mods.sortedBy { it.position }) {
                val modDirectory = ParadoxMetadataManager.getModDirectoryFromSteamId(mod.steamId, workshopDirPath)
                if (!existingModDirectories.add(modDirectory)) continue // 忽略已有的
                val newModInfo = ParadoxModInfo(modDirectory, mod.enabled)
                newModInfos.add(newModInfo)
            }
            val newModSetInfo = ParadoxModSetInfo(gameType, data.name, newModInfos)
            return ParadoxModImporter.Result(total = data.mods.size, actualTotal = newModInfos.size, newModSetInfo = newModSetInfo)
        } else if (isV3 == false) {
            // 按 V2 解析：position 为 String，排序时转为数值（去前导 0，失败时置于末尾）
            val data = readData(filePath, LauncherJsonV2::class.java)
            if (data.game != gameType.gameId) {
                throw IllegalStateException(PlsBundle.message("mod.importer.error.gameType"))
            }
            for (mod in data.mods.sortedBy { ParadoxMetadataManager.parseLauncherV2PositionToInt(it.position) }) {
                val modDirectory = ParadoxMetadataManager.getModDirectoryFromSteamId(mod.steamId, workshopDirPath)
                if (!existingModDirectories.add(modDirectory)) continue // 忽略已有的
                val newModInfo = ParadoxModInfo(modDirectory = modDirectory, enabled = mod.enabled)
                newModInfos.add(newModInfo)
            }
            val newModSetInfo = ParadoxModSetInfo(gameType, data.name, newModInfos)
            return ParadoxModImporter.Result(total = data.mods.size, actualTotal = newModInfos.size, newModSetInfo = newModSetInfo)
        } else {
            // 无法探测时，保持兼容：先尝试 V2，失败再尝试 V3
            run {
                val data = runCatching { readData(filePath, LauncherJsonV2::class.java) }.getOrNull() ?: return@run
                if (data.game != gameType.gameId) {
                    throw IllegalStateException(PlsBundle.message("mod.importer.error.gameType"))
                }
                for (mod in data.mods.sortedBy { ParadoxMetadataManager.parseLauncherV2PositionToInt(it.position) }) {
                    val modDirectory = ParadoxMetadataManager.getModDirectoryFromSteamId(mod.steamId, workshopDirPath)
                    if (!existingModDirectories.add(modDirectory)) continue // 忽略已有的
                    val newModInfo = ParadoxModInfo(modDirectory = modDirectory, enabled = mod.enabled)
                    newModInfos.add(newModInfo)
                }
                val newModSetInfo = ParadoxModSetInfo(gameType, data.name, newModInfos)
                return ParadoxModImporter.Result(total = data.mods.size, actualTotal = newModInfos.size, newModSetInfo = newModSetInfo)
            }
            val data = readData(filePath, LauncherJsonV3::class.java)
            if (data.game != gameType.gameId) {
                throw IllegalStateException(PlsBundle.message("mod.importer.error.gameType"))
            }
            for (mod in data.mods.sortedBy { it.position }) {
                val modDirectory = ParadoxMetadataManager.getModDirectoryFromSteamId(mod.steamId, workshopDirPath)
                if (!existingModDirectories.add(modDirectory)) continue // 忽略已有的
                val newModInfo = ParadoxModInfo(modDirectory, mod.enabled)
                newModInfos.add(newModInfo)
            }
            val newModSetInfo = ParadoxModSetInfo(gameType, data.name, newModInfos)
            return ParadoxModImporter.Result(total = data.mods.size, actualTotal = newModInfos.size, newModSetInfo = newModSetInfo)
        }
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
