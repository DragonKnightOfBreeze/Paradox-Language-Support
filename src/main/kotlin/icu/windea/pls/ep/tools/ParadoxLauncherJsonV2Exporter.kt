package icu.windea.pls.ep.tools

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.ui.tools.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.tools.*

/**
 * 导出模组配置到启动器JSON配置文件。（< 2021.10）
 *
 *  See: [ParadoxLauncherExporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter.cs)
 */
class ParadoxLauncherJsonV2Exporter : ParadoxModExporter {
    companion object {
        private const val playlistsName = "playlists"
        private const val playlistJsonName = "playlist.json"
    }

    override val text: String = PlsBundle.message("mod.exporter.launcherJson.v2")

    override fun execute(project: Project, table: ParadoxModDependenciesTable) {
        val settings = table.model.settings
        val qualifiedName = settings.qualifiedName
        val gameType = settings.gameType ?: return
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
        val defaultSavedDir = gameDataPath?.resolve(playlistsName)
        val defaultSavedFileName = playlistJsonName
        val descriptor = FileSaverDescriptor(PlsBundle.message("mod.exporter.launcherJson.v2.title"), "", "json")
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        val saved = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, table).save(defaultSavedDir, defaultSavedFileName)
        val savedFile = saved?.getVirtualFile(true) ?: return

        try {//使用正在编辑的模组依赖
            //不导出本地模组
            val validModDependencies = table.model.modDependencies.filter { it.source != ParadoxModSource.Local }
            val json = ParadoxLauncherJsonV2(
                game = gameType.id,
                mods = validModDependencies.mapIndexed t@{ i, s ->
                    ParadoxLauncherJsonV2.Mod(
                        displayName = s.name.orEmpty(),
                        enabled = s.enabled,
                        position = (i + 1 + 4096).toString(10).padStart(10, '0'),
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
