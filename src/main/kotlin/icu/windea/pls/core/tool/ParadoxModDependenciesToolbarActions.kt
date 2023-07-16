package icu.windea.pls.core.tool

import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.ui.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

interface ParadoxModDependenciesToolbarActions {
    class AddAction(
        private val project: Project,
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : AnActionButtonRunnable {
        override fun run(e: AnActionButton) {
            //添加模组依赖时可以多选
            val settings = tableModel.settings
            val gameType = settings.gameType.orDefault()
            val descriptor = ParadoxDirectoryDescriptor(chooseMultiple = true)
                .withTitle(PlsBundle.message("mod.dependencies.add.title"))
                .apply { putUserData(PlsDataKeys.gameType, gameType) }
            FileChooser.chooseFiles(descriptor, project, tableView, null) { files ->
                try {
                    var count = 0
                    val newSettingsList = mutableListOf<ParadoxModDependencySettingsState>()
                    for(file in files) {
                        val rootInfo = file.rootInfo
                        if(rootInfo == null) continue //NOTE 目前要求这里的模组目录下必须有模组描述符文件
                        val modPath = file.path
                        count++
                        if(!tableModel.modDependencyDirectories.add(modPath)) continue //忽略已有的
                        val newSettings = ParadoxModDependencySettingsState()
                        newSettings.enabled = true
                        newSettings.modDirectory = modPath
                        newSettingsList.add(newSettings)
                    }
                    
                    //如果最后一个模组依赖是当前模组自身，需要插入到它之前，否则直接添加到最后
                    val isCurrentAtLast = tableModel.isCurrentAtLast()
                    val position = if(isCurrentAtLast) tableModel.rowCount - 1 else tableModel.rowCount
                    tableModel.insertRows(position, newSettingsList)
                    //选中刚刚添加的所有模组依赖
                    tableView.setRowSelectionInterval(position, position + newSettingsList.size - 1)
                    
                    notify(settings, project, PlsBundle.message("mod.dependencies.add.info", count))
                } catch(e: Exception) {
                    if(e is ProcessCanceledException) throw e
                    thisLogger().info(e)
                    notifyWarning(settings, project, PlsBundle.message("mod.dependencies.add.error"))
                }
            }
        }
    }
    
    class EnableAllAction(
        private val project: Project,
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : AnAction(AllIcons.Actions.Selectall) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.enableAll")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val selectedRows = tableView.selectedRows
            if(selectedRows.isNotEmpty()) {
                for(selectedRow in selectedRows) {
                    val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
                    item.enabled = true
                }
            } else {
                for(item in tableModel.items) {
                    item.enabled = true
                }
            }
        }
    }
    
    class DisableAllAction(
        private val project: Project,
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : AnAction(AllIcons.Actions.Unselectall) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.disableAll")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val selectedRows = tableView.selectedRows
            if(selectedRows.isNotEmpty()) {
                for(selectedRow in selectedRows) {
                    val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
                    item.enabled = false
                }
            } else {
                for(item in tableModel.items) {
                    item.enabled = false
                }
            }
        }
    }
    
    class EditAction(
        private val project: Project,
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : AnAction(AllIcons.Actions.Edit) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.edit")
            shortcutSet = CustomShortcutSet.fromString("ENTER")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = tableView.selectedRowCount == 1
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val selectedRow = tableView.selectedRow
            val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
            ParadoxModDependencySettingsDialog(project, item, tableView).show()
        }
    }
    
    class ImportAction(
        private val project: Project,
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : AnAction(AllIcons.ToolbarDecorator.Import) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.import")
            shortcutSet = CustomShortcutSet.fromString("alt I")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val popup = ParadoxModDependenciesImportPopup(project, tableView, tableModel)
            JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(e.dataContext)
        }
    }
    
    class ExportAction(
        private val project: Project,
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : AnAction(AllIcons.ToolbarDecorator.Export) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.export")
            shortcutSet = CustomShortcutSet.fromString("alt E")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            //导出全部，而非当前选中的行
            val popup = ParadoxModDependenciesExportPopup(project, tableView, tableModel)
            JBPopupFactory.getInstance().createListPopup(popup).showInBestPositionFor(e.dataContext)
        }
    }
}