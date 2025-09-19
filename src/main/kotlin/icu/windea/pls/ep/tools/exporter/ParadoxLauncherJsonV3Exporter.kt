package icu.windea.pls.ep.tools.exporter

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.util.ObjectMappers
import icu.windea.pls.ep.tools.model.LauncherJsonV3
import icu.windea.pls.lang.PlsDataKeys
import icu.windea.pls.lang.settings.qualifiedName
import icu.windea.pls.lang.ui.tools.ParadoxModDependenciesTable
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxModSource

/**
 * 导出模组配置到启动器JSON配置文件。（>= 2021.10）
 *
 * See: [ParadoxLauncherExporter202110.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter202110.cs)
 */
class ParadoxLauncherJsonV3Exporter : ParadoxModExporter {
    companion object {
        private const val playlistsName = "playlists"
        private const val playlistJsonName = "playlist.json"
    }

    override val text: String = PlsBundle.message("mod.exporter.launcherJson.v3")

    override fun execute(project: Project, table: ParadoxModDependenciesTable) {
        val settings = table.model.settings
        val qualifiedName = settings.qualifiedName
        val gameType = settings.finalGameType
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
        val defaultSavedDir = gameDataPath?.resolve(playlistsName)
        val defaultSavedFileName = playlistJsonName
        val descriptor = FileSaverDescriptor(PlsBundle.message("mod.exporter.launcherJson.v3.title"), "", "json")
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        val saved = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, table).save(defaultSavedDir, defaultSavedFileName)
        val savedFile = saved?.getVirtualFile(true) ?: return

        try {//使用正在编辑的模组依赖
            //不导出本地模组
            val validModDependencies = table.model.modDependencies.filter { it.source != ParadoxModSource.Local }
            val json = LauncherJsonV3(
                game = gameType.id,
                mods = validModDependencies.mapIndexed t@{ i, s ->
                    LauncherJsonV3.Mod(
                        displayName = s.name.orEmpty(),
                        enabled = s.enabled,
                        position = i,
                        steamId = s.remoteId?.takeIf { s.source == ParadoxModSource.Steam },
                        pdxId = s.remoteId?.takeIf { s.source == ParadoxModSource.Paradox },
                    )
                },
                name = savedFile.nameWithoutExtension,
            )
            runWriteAction {
                ObjectMappers.jsonMapper.writeValue(savedFile.getOutputStream(this), json)
            }
            val count = validModDependencies.size

            PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, PlsBundle.message("mod.exporter.info", savedFile.nameWithoutExtension, count)).notify(project)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)

            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.exporter.error")).notify(project)
        }
    }
}
