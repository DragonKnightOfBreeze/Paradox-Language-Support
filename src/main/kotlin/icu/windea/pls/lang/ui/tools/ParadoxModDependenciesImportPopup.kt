package icu.windea.pls.lang.ui.tools

import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.ep.tools.importer.ParadoxModImporter
import icu.windea.pls.lang.PlsDataKeys
import icu.windea.pls.lang.errorDetails
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.qualifiedName
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.tools.toModDependencies
import icu.windea.pls.model.tools.toModSetInfo
import kotlinx.coroutines.CancellationException

class ParadoxModDependenciesImportPopup(
    private val project: Project,
    private val table: ParadoxModDependenciesTable,
    modImporters: List<ParadoxModImporter> = ParadoxModImporter.getAll(table.model.settings.finalGameType),
) : BaseListPopupStep<ParadoxModImporter>(getTitle(), *modImporters.toTypedArray()) {
    companion object {
        private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.import.popup.title")

        private val logger = logger<ParadoxModDependenciesImportPopup>()
    }

    override fun getIconFor(value: ParadoxModImporter) = value.icon

    override fun getTextFor(value: ParadoxModImporter) = value.text

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: ParadoxModImporter, finalChoice: Boolean) = doFinalStep { execute(selectedValue) }

    private fun execute(modImporter: ParadoxModImporter) {
        val settings = table.model.settings
        val gameType = settings.finalGameType
        val selected = modImporter.getSelectedFile(gameType)?.toVirtualFile()
        val descriptor = modImporter.createFileChooserDescriptor(gameType)
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        FileChooser.chooseFile(descriptor, project, table, selected) { file ->
            doExecute(settings, modImporter, file)
        }
    }

    private fun doExecute(settings: ParadoxGameOrModSettingsState, modImporter: ParadoxModImporter, file: VirtualFile) {
        // EDT
        val gameType = settings.finalGameType
        val qualifiedName = settings.qualifiedName
        val modSetInfo = table.model.modDependencies.toModSetInfo(gameType) // 需要从 tableModel 中获取，而非直接从 settings 中获取
        val result = try {
            runWithModalProgressBlocking(project, PlsBundle.message("mod.dependencies.import.progress.title")) {
                modImporter.execute(file.toNioPath(), modSetInfo)
            }
        } catch (e: Exception) {
            if (e is ProcessCanceledException || e is CancellationException) throw e
            logger.warn(e)
            val content = PlsBundle.message("mod.dependencies.import.error") + e.message.errorDetails
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }
        val from = result.newModSetInfo.name
        if (result.actualTotal == 0) {
            val content = PlsBundle.message("mod.dependencies.import.empty", from)
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }

        // 添加到模组依赖表格中
        table.addModDependencies(result.newModSetInfo.toModDependencies())

        if (result.warning != null) {
            val content = PlsBundle.message("mod.dependencies.import.info", from, result.actualTotal) + result.warning.errorDetails
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }
        val content = PlsBundle.message("mod.dependencies.import.info", from, result.actualTotal)
        PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, content).notify(project)
    }
}
