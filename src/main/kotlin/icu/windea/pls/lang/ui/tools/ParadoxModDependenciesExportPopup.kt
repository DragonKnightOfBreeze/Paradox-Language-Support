package icu.windea.pls.lang.ui.tools

import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.exporter.ParadoxModExporter
import icu.windea.pls.lang.PlsDataKeys
import icu.windea.pls.lang.errorDetails
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.qualifiedName
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.tools.toModSetInfo
import kotlinx.coroutines.CancellationException

class ParadoxModDependenciesExportPopup(
    private val project: Project,
    private val table: ParadoxModDependenciesTable,
    modExporters: List<ParadoxModExporter> = ParadoxModExporter.getAll(table.model.settings.finalGameType),
) : BaseListPopupStep<ParadoxModExporter>(getTitle(), *modExporters.toTypedArray()) {
    companion object {
        private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.export.popup.title")

        private val logger = logger<ParadoxModDependenciesExportPopup>()
    }

    override fun getIconFor(value: ParadoxModExporter) = value.icon

    override fun getTextFor(value: ParadoxModExporter) = value.text

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: ParadoxModExporter, finalChoice: Boolean) = doFinalStep { execute(selectedValue) }

    private fun execute(modExporter: ParadoxModExporter) {
        val settings = table.model.settings
        val gameType = settings.finalGameType
        val descriptor = modExporter.createFileSaverDescriptor(gameType)
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        val baseDir = modExporter.getSavedBaseDir(gameType)
        val fileName = modExporter.getSavedFileName(gameType)
        val saved = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, table).save(baseDir, fileName)
        val savedFile = saved?.getVirtualFile(true) ?: return

        doExecute(settings, modExporter, savedFile)
    }

    private fun doExecute(settings: ParadoxGameOrModSettingsState, modExporter: ParadoxModExporter, file: VirtualFile) {
        val gameType = settings.finalGameType
        val qualifiedName = settings.qualifiedName
        val modSetInfo = table.model.modDependencies.toModSetInfo(gameType) // 需要从 tableModel 中获取，而非直接从 settings 中获取
        val result = try {
            runWithModalProgressBlocking(project, PlsBundle.message("mod.dependencies.export.progress.title")) {
                modExporter.execute(file.toNioPath(), modSetInfo)
            }
        } catch (e: Exception) {
            if (e is ProcessCanceledException || e is CancellationException) throw e
            logger.warn(e)
            val content = PlsBundle.message("mod.dependencies.export.error") + e.message.errorDetails
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }
        val from = modSetInfo.name
        if (result.actualTotal == 0) {
            val content = PlsBundle.message("mod.dependencies.export.empty", from)
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }

        if (result.warning != null) {
            val content = PlsBundle.message("mod.dependencies.export.info", from, result.actualTotal) + result.warning.errorDetails
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }
        val content = PlsBundle.message("mod.dependencies.export.info", from, result.actualTotal)
        PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, content).notify(project)
    }
}
