package icu.windea.pls.lang.ui.tools

import com.intellij.notification.NotificationType
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.tools.ParadoxModExporter
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import icu.windea.pls.lang.PlsDataKeys
import icu.windea.pls.lang.settings.qualifiedName
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxModSource
import java.nio.file.Paths
import javax.swing.Icon

class ParadoxModDependenciesExportPopup(
    private val project: Project,
    private val table: ParadoxModDependenciesTable
) : BaseListPopupStep<ParadoxModExporter>(getTitle(), *getValues()) {
    companion object {
        private const val playlistsName = "playlists"
        private const val playlistJsonName = "playlist.json"

        private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.export.popup.title")

        private fun getValues() = ParadoxModExporter.EP_NAME.extensions
    }

    override fun getIconFor(value: ParadoxModExporter): Icon? = value.icon

    override fun getTextFor(value: ParadoxModExporter): String = value.text

    override fun isSpeedSearchEnabled(): Boolean = true

    override fun onChosen(selectedValue: ParadoxModExporter, finalChoice: Boolean) = doFinalStep {
        val settings = table.model.settings
        val qualifiedName = settings.qualifiedName
        val gameType = settings.finalGameType
        val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
        val defaultSavedDir = gameDataPath?.resolve(playlistsName)
        val defaultSavedFileName = playlistJsonName
        val descriptor = FileSaverDescriptor(selectedValue.text, "", "json")
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        val saved = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, table).save(defaultSavedDir, defaultSavedFileName)
        val savedFile = saved?.getVirtualFile(true) ?: return@doFinalStep

        try {
            // 从表模型构造平台无关的模组信息列表
            val mods = table.model.modDependencies.map { s ->
                ParadoxModInfo(
                    name = s.name,
                    modDirectory = s.modDirectory?.let { Paths.get(it) },
                    remoteId = s.remoteId,
                    source = s.source ?: ParadoxModSource.Local,
                    enabled = s.enabled,
                    version = s.version,
                    supportedVersion = s.supportedVersion,
                )
            }
            // 执行导出
            val path = Paths.get(savedFile.path)
            selectedValue.exportTo(path, gameType.id, mods)

            val count = mods.count { it.source != ParadoxModSource.Local }
            PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, PlsBundle.message("mod.exporter.info", savedFile.nameWithoutExtension, count)).notify(project)
        } catch (_: Exception) {
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, PlsBundle.message("mod.exporter.error")).notify(project)
        }
    }
}
