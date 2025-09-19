package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.fileChooser.FileSaverDescriptor
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path

/**
 * 导出模组配置到启动器JSON配置文件。（>= 2021.10）
 *
 * 参见：[ParadoxLauncherExporter202110.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter202110.cs)
 */
class ParadoxLauncherJsonV3Exporter : ParadoxJsonBasedModExporter() {
    companion object {
        private const val playlistsName = "playlists"
        private const val playlistJsonName = "playlist.json"
    }

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
        TODO("Not yet implemented")
    }

    override fun createFileSaverDescriptor(gameType: ParadoxGameType): FileSaverDescriptor {
        TODO("Not yet implemented")
    }

    override fun getSavedBaseDir(gameType: ParadoxGameType): Path? {
        TODO("Not yet implemented")
    }

    override fun getSavedFileName(gameType: ParadoxGameType): String? {
        TODO("Not yet implemented")
    }
}
