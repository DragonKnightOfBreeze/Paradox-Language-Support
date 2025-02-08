package icu.windea.pls.tools.ui

import com.intellij.icons.*
import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.ui.*
import icu.windea.pls.model.*

interface ParadoxModDependenciesToolbarActions {
    class AddAction(
        private val project: Project,
        private val table: ParadoxModDependenciesTable
    ) : AnActionButtonRunnable {
        override fun run(e: AnActionButton) {
            //添加模组依赖时可以多选
            val settings = table.model.settings
            val gameType = settings.gameType.orDefault()
            val descriptor = ParadoxDirectoryDescriptor(chooseMultiple = true)
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
                        newSettings.enabled = true
                        newSettings.modDirectory = modPath
                        newSettingsList.add(newSettings)
                    }

                    //如果最后一个模组依赖是当前模组自身，需要插入到它之前，否则直接添加到最后
                    val isCurrentAtLast = table.model.isCurrentAtLast()
                    val position = if (isCurrentAtLast) table.model.rowCount - 1 else table.model.rowCount
                    table.model.insertRows(position, newSettingsList)
                    //选中刚刚添加的所有模组依赖
                    table.setRowSelectionInterval(position, position + newSettingsList.size - 1)

                    run n@{
                        val title = settings.qualifiedName ?: return@n
                        val content = PlsBundle.message("mod.dependencies.add.info", count)
                        createNotification(title, content, NotificationType.INFORMATION).notify(project)
                    }
                } catch (e: Exception) {
                    if (e is ProcessCanceledException) throw e
                    thisLogger().warn(e)

                    run n@{
                        val title = settings.qualifiedName ?: return@n
                        val content = PlsBundle.message("mod.dependencies.add.error")
                        createNotification(title, content, NotificationType.WARNING).notify(project)
                    }
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

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }

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

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }

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

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }

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

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }

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

        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }

        override fun actionPerformed(e: AnActionEvent) {
            //导出全部，而非当前选中的行
            val popup = ParadoxModDependenciesExportPopup(project, table)
            JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(e.dataContext)
        }
    }
}
