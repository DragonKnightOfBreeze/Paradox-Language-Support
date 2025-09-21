package icu.windea.pls.lang.ui.tools

import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.tools.exporter.ParadoxModExporter
import icu.windea.pls.lang.PlsDataKeys
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.qualifiedName
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.tools.toModSetInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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

        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch {
            doExecute(settings, modExporter, savedFile)
        }
    }

    private suspend fun doExecute(settings: ParadoxGameOrModSettingsState, modExporter: ParadoxModExporter, file: VirtualFile) {
        val gameType = settings.finalGameType
        val qualifiedName = settings.qualifiedName
        val modSetInfo = settings.modDependencies.toModSetInfo(gameType, "")
        val result = try {
            modExporter.execute(file.toNioPath(), modSetInfo)
        } catch (e: Exception) {
            if (e is ProcessCanceledException || e is CancellationException) throw e
            logger.warn(e)
            val content = PlsBundle.message("mod.exporter.error", e.message.orEmpty())
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }
        if (result.warning != null) {
            val content = PlsBundle.message("mod.exporter.warning", result.actualTotal, modSetInfo.name, result.warning)
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }

        val content = PlsBundle.message("mod.exporter.info", result.actualTotal, modSetInfo.name)
        PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, content).notify(project)
    }
}
