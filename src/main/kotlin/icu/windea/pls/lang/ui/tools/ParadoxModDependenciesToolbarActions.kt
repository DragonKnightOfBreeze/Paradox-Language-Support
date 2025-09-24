package icu.windea.pls.lang.ui.tools

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.AnActionButton
import com.intellij.ui.AnActionButtonRunnable
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.PlsDataKeys
import icu.windea.pls.lang.errorDetails
import icu.windea.pls.lang.rootInfo
import icu.windea.pls.lang.settings.ParadoxModDependencySettingsState
import icu.windea.pls.lang.settings.qualifiedName
import icu.windea.pls.lang.util.PlsCoreManager

@Suppress("unused")
interface ParadoxModDependenciesToolbarActions {
    class AddAction(
        private val project: Project,
        private val table: ParadoxModDependenciesTable
    ) : AnActionButtonRunnable {
        override fun run(e: AnActionButton) {
            //添加模组依赖时可以多选
            val settings = table.model.settings
            val qualifiedName = settings.qualifiedName
            val gameType = settings.gameType ?: return
            val descriptor = FileChooserDescriptorFactory.multiDirs()
                .withTitle(PlsBundle.message("mod.dependencies.add.title"))
                .apply { putUserData(PlsDataKeys.gameType, gameType) }
            FileChooser.chooseFiles(descriptor, project, table, null) { files ->
                try {
                    var count = 0
                    val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
                    for (file in files) {
                        val rootInfo = file.rootInfo
                        if (rootInfo == null) continue //NOTE 目前要求这里的模组目录下必须有模组描述符文件
                        val modPath = file.path
                        count++
                        if (!table.model.modDependencyDirectories.add(modPath)) continue //忽略已有的
                        val newSettings = ParadoxModDependencySettingsState()
                        newSettings.modDirectory = modPath
                        newSettings.enabled = true
                        newSettingsList.add(newSettings)
                    }

                    table.addModDependencies(newSettingsList)

                    val content = PlsBundle.message("mod.dependencies.add.info", count)
                    PlsCoreManager.createNotification(NotificationType.INFORMATION, qualifiedName, content).notify(project)
                } catch (e: Exception) {
                    if (e is ProcessCanceledException) throw e
                    thisLogger().warn(e)
                    val content = PlsBundle.message("mod.dependencies.add.error") + e.message.errorDetails
                    PlsCoreManager.createNotification(NotificationType.WARNING, qualifiedName, content).notify(project)
                }
            }
        }

    }

    class EditAction(
        private val project: Project,
        private val table: ParadoxModDependenciesTable
    ) : AnAction(AllIcons.Actions.Edit) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.edit")
            registerCustomShortcutSet(CustomShortcutSet.fromString("ENTER"), null)
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = table.selectedRowCount == 1
        }

        override fun actionPerformed(e: AnActionEvent) {
            val selectedRow = table.selectedRow
            val item = table.model.getItem(table.convertRowIndexToModel(selectedRow))
            ParadoxModDependencySettingsDialog(project, item, table).show()
        }
    }

    class EnableAllAction(
        private val project: Project,
        private val table: ParadoxModDependenciesTable
    ) : AnAction(AllIcons.Actions.Selectall) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.enableAll")
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) {
            val selectedRows = table.selectedRows
            if (selectedRows.isNotEmpty()) {
                for (selectedRow in selectedRows) {
                    val item = table.model.getItem(table.convertRowIndexToModel(selectedRow))
                    item.enabled = true
                }
            } else {
                for (item in table.model.items) {
                    item.enabled = true
                }
            }
        }
    }

    class DisableAllAction(
        private val project: Project,
        private val table: ParadoxModDependenciesTable
    ) : AnAction(AllIcons.Actions.Unselectall) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.disableAll")
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) {
            val selectedRows = table.selectedRows
            if (selectedRows.isNotEmpty()) {
                for (selectedRow in selectedRows) {
                    val item = table.model.getItem(table.convertRowIndexToModel(selectedRow))
                    item.enabled = false
                }
            } else {
                for (item in table.model.items) {
                    item.enabled = false
                }
            }
        }
    }

    class ImportAction(
        private val project: Project,
        private val table: ParadoxModDependenciesTable
    ) : AnAction(AllIcons.ToolbarDecorator.Import) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.import")
            registerCustomShortcutSet(CustomShortcutSet.fromString("alt I"), null)
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) {
            val popup = ParadoxModDependenciesImportPopup(project, table)
            JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(e.dataContext)
        }
    }

    class ExportAction(
        private val project: Project,
        private val table: ParadoxModDependenciesTable
    ) : AnAction(AllIcons.ToolbarDecorator.Export) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.export")
            registerCustomShortcutSet(CustomShortcutSet.fromString("alt E"), null)
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) {
            //导出全部，而非当前选中的行
            val popup = ParadoxModDependenciesExportPopup(project, table)
            JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(e.dataContext)
        }
    }
}
