package icu.windea.pls.lang.ui.tools

import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.tools.importer.ParadoxModImporter
import icu.windea.pls.lang.PlsDataKeys
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.qualifiedName
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.tools.toModDependencies
import icu.windea.pls.model.tools.toModSetInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ParadoxModDependenciesImportPopup(
    private val project: Project,
    private val table: ParadoxModDependenciesTable
) : BaseListPopupStep<ParadoxModImporter>(getTitle(), *getValues()) {
    companion object {
        private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.import.popup.title")

        private fun getValues() = ParadoxModImporter.EP_NAME.extensions

        private val logger = logger<ParadoxModDependenciesImportPopup>()
    }

    override fun getIconFor(value: ParadoxModImporter) = value.icon

    override fun getTextFor(value: ParadoxModImporter) = value.text

    override fun isSpeedSearchEnabled() = true

    override fun onChosen(selectedValue: ParadoxModImporter, finalChoice: Boolean) = doFinalStep { execute(selectedValue) }

    private fun execute(modImporter: ParadoxModImporter) {
        val settings = table.model.settings
        val gameType = settings.finalGameType
        val selected = modImporter.getSelectedFile(gameType)
        val descriptor = modImporter.createFileChooserDescriptor(gameType)
            .apply { putUserData(PlsDataKeys.gameType, gameType) }
        FileChooser.chooseFile(descriptor, project, table, selected) { file ->
            val coroutineScope = PlsFacade.getCoroutineScope()
            coroutineScope.launch {
                doExecute(settings, modImporter, file)
            }
        }
    }

    private suspend fun doExecute(settings: ParadoxGameOrModSettingsState, modImporter: ParadoxModImporter, file: VirtualFile) {
        val gameType = settings.finalGameType
        val qualifiedName = settings.qualifiedName
        val modSetInfo = settings.modDependencies.toModSetInfo(gameType, "")
        val result = try {
            modImporter.execute(file.toNioPath(), modSetInfo)
        } catch (e: Exception) {
            if (e is ProcessCanceledException || e is CancellationException) throw e
            logger.warn(e)
            val content = PlsBundle.message("mod.importer.error", 0, e.message.orEmpty())
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }
        if (result.warning != null) {
            val content = PlsBundle.message("mod.importer.error", result.actualTotal, result.warning)
            PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
            return
        }

        // 如果最后一个模组依赖是当前模组自身，需要插入到它之前，否则直接添加到最后
        val isCurrentAtLast = table.model.isCurrentAtLast()
        val position = if (isCurrentAtLast) table.model.rowCount - 1 else table.model.rowCount
        val newSettingsList = modSetInfo.toModDependencies()
        table.model.insertRows(position, newSettingsList)
        // 选中刚刚添加的所有模组依赖
        table.setRowSelectionInterval(position, position + newSettingsList.size - 1)

        val content = PlsBundle.message("mod.importer.info", result.actualTotal, modSetInfo.name)
        PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, content).notify(project)
    }
}
