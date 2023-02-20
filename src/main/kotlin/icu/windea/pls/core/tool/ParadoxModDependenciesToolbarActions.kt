package icu.windea.pls.core.tool

import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.ui.table.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*

interface ParadoxModDependenciesToolbarActions {
    class EditAction(
        private val project: Project,
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : AnAction(IconUtil.getEditIcon()) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.toolbar.action.edit")
            shortcutSet = CustomShortcutSet.fromString("ENTER")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = tableView.selectedRowCount != 1
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
            JBPopupFactory.getInstance().createListPopup(ParadoxModDependenciesImportPopup()).showInBestPositionFor(e.dataContext)
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
            JBPopupFactory.getInstance().createListPopup(ParadoxModDependenciesExportPopup()).showInBestPositionFor(e.dataContext)
        }
    }
}