package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.fileChooser.FileSaverDescriptor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.tools.model.Constants
import icu.windea.pls.ep.tools.model.LauncherJsonV3
import icu.windea.pls.lang.util.ParadoxMetadataManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * 导出模组信息到官方启动器（>= 2021.10）的 JSON 配置文件。
 *
 * 数据文件默认位于游戏数据目录的 `playlists` 子目录下，如 `playlist.json`。
 *
 * 参见：[ParadoxLauncherExporter202110.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter202110.cs)
 */
class ParadoxLauncherJsonV3Exporter : ParadoxJsonBasedModExporter() {
    override val text: String = PlsBundle.message("mod.exporter.launcherJson.v3")

    // override fun execute(project: Project, table: ParadoxModDependenciesTable) {
    //     val settings = table.model.settings
    //     val qualifiedName = settings.qualifiedName
    //     val gameType = settings.finalGameType
    //     val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
    //     val defaultSavedDir = gameDataPath?.resolve(playlistsName)
    //     val defaultSavedFileName = playlistJsonName
    //     val descriptor = FileSaverDescriptor(PlsBundle.message("mod.exporter.launcherJson.v3.title"), "", "json")
    //         .apply { putUserData(PlsDataKeys.gameType, gameType) }
    //     val saved = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, table).save(defaultSavedDir, defaultSavedFileName)
    //     val savedFile = saved?.getVirtualFile(true) ?: return
    //
    //     try {
    //         //使用正在编辑的模组依赖
    //         //不导出本地模组
    //         val validModDependencies = table.model.modDependencies.filter { it.source != ParadoxModSource.Local }
    //         val json = LauncherJsonV3(
    //             game = gameType.id,
    //             mods = validModDependencies.mapIndexed t@{ i, s ->
    //                 LauncherJsonV3.Mod(
    //                     displayName = s.name.orEmpty(),
    //                     enabled = s.enabled,
    //                     position = i,
    //                     steamId = s.remoteId?.takeIf { s.source == ParadoxModSource.Steam },
    //                     pdxId = s.remoteId?.takeIf { s.source == ParadoxModSource.Paradox },
    //                 )
    //             },
    //             name = savedFile.nameWithoutExtension,
    //         )
    //         runWriteAction {
    //             ObjectMappers.jsonMapper.writeValue(savedFile.getOutputStream(this), json)
    //         }
    //         val count = validModDependencies.size
    //
    //         PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, PlsBundle.message("mod.exporter.info", savedFile.nameWithoutExtension, count)).notify(project)
    //     } catch (e: Exception) {
    //         if (e is ProcessCanceledException) throw e
    //         thisLogger().warn(e)
    //
    //         PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.exporter.error")).notify(project)
    //     }
    // }

    override suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): ParadoxModExporter.Result {
        val gameType = modSetInfo.gameType
        val mods = modSetInfo.mods.filter { it.enabled }

        val valid = mods.mapNotNull { m ->
            val remoteId = m.remoteId ?: ParadoxMetadataManager.getRemoteFileIdFromModDir(m.modDirectory) ?: return@mapNotNull null
            val displayName = m.name ?: ParadoxMetadataManager.getModDisplayNameFromDescriptor(m.modDirectory).orEmpty()
            remoteId to displayName
        }

        val playlistName = filePath.fileName.toString().substringBeforeLast('.')
        val json = LauncherJsonV3(
            game = gameType.gameId,
            name = playlistName,
            mods = valid.mapIndexed { index, (remoteId, displayName) ->
                LauncherJsonV3.Mod(
                    displayName = displayName,
                    enabled = true,
                    position = index,
                    steamId = remoteId,
                    pdxId = null,
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
