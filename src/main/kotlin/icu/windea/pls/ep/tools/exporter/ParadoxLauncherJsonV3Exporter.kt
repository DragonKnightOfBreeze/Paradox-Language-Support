package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.fileChooser.FileSaverDescriptor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.ep.tools.model.LauncherJsonV3
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModSource
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * 导出模组信息到官方启动器（>= 2021.10）的 JSON 配置文件。
 *
 * 数据文件默认位于游戏数据目录的 `playlists` 子目录下，如 `playlist.json`。
 *
 * 导出时会排除本地源的模组。
 *
 * 参见：[ParadoxLauncherExporter202110.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter202110.cs)
 */
class ParadoxLauncherJsonV3Exporter : ParadoxJsonBasedModExporter() {
    override val text: String = PlsBundle.message("mod.exporter.launcherJson.v3")

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModExporter.Result {
        // 仅导出启用的模组
        val mods = modSetInfo.mods.filter { it.enabled }
        if (mods.isEmpty()) {
            return ParadoxModExporter.Result(total = 0, actualTotal = 0)
        }

        val gameType = modSetInfo.gameType

        // 解析远端ID与显示名（优先结构字段，否则读取 descriptor.mod）
        val valid = mods.mapNotNull f@{ m ->
            val modInfo = ParadoxMetadataManager.getModInfoFromModDirectory(m.modDirectory) ?: return@f null
            val displayName = m.name?.orNull() ?: modInfo.name
            val remoteId = m.remoteId?.orNull() ?: modInfo.remoteId?.orNull()
            val source = m.source ?: modInfo.source

            // 导出到官方启动器时，排除本地源的模组
            if (source == ParadoxModSource.Local) return@f null
            val steamId = remoteId?.takeIf { source == ParadoxModSource.Steam }
            val pdxId = remoteId?.takeIf { source == ParadoxModSource.Paradox }
            if (steamId == null && pdxId == null) return@f null

            tupleOf(displayName, steamId, pdxId)
        }

        val playlistName = filePath.fileName.toString().substringBeforeLast('.')
        val json = LauncherJsonV3(
            game = gameType.gameId,
            name = playlistName,
            mods = valid.mapIndexed { index, (displayName, steamId, pdxId) ->
                LauncherJsonV3.Mod(
                    displayName = displayName,
                    enabled = true,
                    position = index,
                    steamId = steamId,
                    pdxId = pdxId,
                )
            }
        )

        writeData(filePath, json)
        return ParadoxModExporter.Result(total = mods.size, actualTotal = valid.size)
    }

    override fun createFileSaverDescriptor(gameType: ParadoxGameType): FileSaverDescriptor {
        return FileSaverDescriptor(PlsBundle.message("mod.exporter.launcherJson.v3.title"), "", "json")
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
