package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.fileChooser.FileSaverDescriptor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.ep.tools.model.LauncherJsonV2
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * 导出模组信息到官方启动器（< 2021.10） JSON 配置文件。
 *
 * 数据文件默认位于游戏数据目录的 `playlists` 子目录下，如 `playlist.json`。
 *
 * 参见：[ParadoxLauncherExporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter.cs)
 */
class ParadoxLauncherJsonV2Exporter : ParadoxJsonBasedModExporter() {
    override val text: String = PlsBundle.message("mod.exporter.launcherJson.v2")

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModExporter.Result {
        val gameType = modSetInfo.gameType
        val mods = modSetInfo.mods.filter { it.enabled }

        // 解析远端ID与显示名（优先结构字段，否则读取 descriptor.mod）
        val valid = mods.mapNotNull { m ->
            val remoteId = m.remoteId ?: ParadoxMetadataManager.getRemoteFileIdFromModDir(m.modDirectory) ?: return@mapNotNull null
            val displayName = m.name ?: ParadoxMetadataManager.getModDisplayNameFromDescriptor(m.modDirectory).orEmpty()
            remoteId to displayName
        }

        val playlistName = filePath.fileName.toString().substringBeforeLast('.')
        val json = LauncherJsonV2(
            game = gameType.gameId,
            name = playlistName,
            mods = valid.mapIndexed { index, (remoteId, displayName) ->
                LauncherJsonV2.Mod(
                    displayName = displayName,
                    enabled = true,
                    position = ParadoxMetadataManager.formatLauncherPosition(index, isV4Plus = false),
                    steamId = remoteId, // 默认按 steamId 写出
                    pdxId = null,
                )
            }
        )

        writeData(filePath, json)
        return ParadoxModExporter.Result(total = mods.size, actualTotal = valid.size)
    }

    override fun createFileSaverDescriptor(gameType: ParadoxGameType): FileSaverDescriptor {
        return FileSaverDescriptor(PlsBundle.message("mod.exporter.launcherJson.v2.title"), "", "json")
    }

    override fun getSavedBaseDir(gameType: ParadoxGameType): Path? {
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)?.takeIf { it.exists() } ?: return null
        val playlistsDir = gameDataPath.resolve(Constants.playlistsName)
        return playlistsDir.takeIf { it.exists() } ?: gameDataPath
    }

    override fun getSavedFileName(gameType: ParadoxGameType): String {
        return Constants.playlistJsonName
    }
}
