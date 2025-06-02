package icu.windea.pls.tools.exporter

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import icu.windea.pls.tools.model.*
import icu.windea.pls.tools.ui.*

private const val pos = 4096
private const val defaultSavedName = "playlist.json"

/**
 * 导出模组配置到启动器JSON配置文件。（< 2021.10）
 *
 *  See: [ParadoxLauncherExporter.cs](https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Exporter/ParadoxLauncherExporter.cs)
 */
class ParadoxToLauncherJsonV2Exporter : ParadoxModExporter {
    enum class Version {
        Default,
        V4,
        V5
    }

    var defaultSelected: VirtualFile? = null

    override val text: String = PlsBundle.message("mod.exporter.launcherJson.v2")

    override fun execute(project: Project, table: ParadoxModDependenciesTable) {
        val settings = table.model.settings
        val gameType = settings.gameType.orDefault()
        if (defaultSelected == null) {
            val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)?.toPathOrNull()
            val playlistsPath = gameDataPath?.resolve("playlists")
            val playlistsFile = playlistsPath?.toVirtualFile(false)
            if (playlistsFile != null) defaultSelected = playlistsFile
        }
        val descriptor = FileSaverDescriptor(PlsBundle.message("mod.exporter.launcherJson.v2.title"), "", "json")
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        val saved = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, table).save(defaultSavedName)
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
                        position = (i + 1 + pos).toString(10).padStart(10, '0'),
                        steamId = s.remoteId?.takeIf { s.source == ParadoxModSource.Steam },
                        pdxId = s.remoteId?.takeIf { s.source == ParadoxModSource.Paradox },
                    )
                },
                name = savedFile.nameWithoutExtension,
            )
            runWriteAction {
                jsonMapper.writeValue(savedFile.getOutputStream(this), json)
            }
            val count = validModDependencies.size

            run {
                val title = settings.qualifiedName ?: return@run
                val content = PlsBundle.message("mod.exporter.info", savedFile.nameWithoutExtension, count)
                createNotification(title, content, NotificationType.INFORMATION).notify(project)
            }
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().warn(e)

            run {
                val title = settings.qualifiedName ?: return@run
                val content = PlsBundle.message("mod.exporter.error")
                createNotification(title, content, NotificationType.WARNING).notify(project)
            }
        }
    }
}
