package icu.windea.pls.core.tool

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.ui.*
import com.intellij.ui.table.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.actions.*
import icu.windea.pls.core.ui.*
import java.awt.event.*
import javax.swing.*

//com.intellij.openapi.roots.ui.configuration.classpath.ClasspathTableModel

class ParadoxModDependenciesTableModel(
    modDependencies: List<ParadoxModDependencySettingsState>
) : ListTableModel<ParadoxModDependencySettingsState>() {
    init {
        columnInfos = arrayOf(SelectedItem, NameItem, VersionItem)
        items = modDependencies
    }
    
    object SelectedItem : ColumnInfo<ParadoxModDependencySettingsState, Boolean>(PlsBundle.message("mod.settings.modDependencies.column.selected.name")) {
        const val columnIndex = 0
        
        override fun valueOf(item: ParadoxModDependencySettingsState): Boolean {
            return item.selected
        }
        
        override fun setValue(item: ParadoxModDependencySettingsState, value: Boolean) {
            item.selected = value
        }
        
        override fun isCellEditable(item: ParadoxModDependencySettingsState): Boolean {
            return true
        }
        
        override fun getColumnClass(): Class<*> {
            return Boolean::class.java
        }
    }
    
    object NameItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.settings.modDependencies.column.name.name")) {
        const val columnIndex = 1
        
        private val _comparator = compareBy<ParadoxModDependencySettingsState> { item -> item.name.orEmpty() }
        
        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            return item.name.orEmpty()
        }
        
        override fun getComparator(): Comparator<ParadoxModDependencySettingsState> {
            return _comparator
        }
    }
    
    object VersionItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.settings.modDependencies.column.version.name")) {
        const val columnIndex = 2
        
        private val _comparator = compareBy<ParadoxModDependencySettingsState> { item -> item.version.orEmpty() }
        
        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            return item.version.orEmpty()
        }
        
        override fun getComparator(): Comparator<ParadoxModDependencySettingsState> {
            return _comparator
        }
    }
}

//com.intellij.openapi.roots.ui.configuration.classpath.ClasspathPanelImpl.createTableWithButtons

fun createModDependenciesPanel(project: Project, modSettings: ParadoxModSettingsState?, modDependencies: List<ParadoxModDependencySettingsState>): JPanel {
    val tableModel = ParadoxModDependenciesTableModel(modDependencies)
    val tableView = TableView(tableModel)
    tableView.setShowGrid(false)
    tableView.cellSelectionEnabled = false
    tableView.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
    tableView.selectionModel.setSelectionInterval(0, 0)
    tableView.surrendersFocusOnKeystroke = true
    tableView.setFixedColumnWidth(ParadoxModDependenciesTableModel.SelectedItem.columnIndex, ParadoxModDependenciesTableModel.SelectedItem.name)
    //快速搜索
    object : SpeedSearchBase<TableView<ParadoxModDependencySettingsState>>(tableView) {
        override fun getSelectedIndex(): Int {
            return tableView.selectedRow
        }
        
        override fun getElementCount(): Int {
            return tableModel.rowCount
        }
        
        override fun getElementAt(viewIndex: Int): Any {
            return tableModel.getItem(tableView.convertRowIndexToModel(viewIndex))
        }
        
        override fun getElementText(element: Any): String {
            val modDirectory = (element as ParadoxModDependencySettingsState).modDirectory.orEmpty()
            val modDescriptorSettings = getProfilesSettings().modDescriptorSettings.getValue(modDirectory)
            return modDescriptorSettings.name.orEmpty()
        }
        
        override fun selectElement(element: Any, selectedText: String) {
            val count = tableModel.rowCount
            for(row in 0 until count) {
                if(element == tableModel.getItem(row)) {
                    val viewRow = tableView.convertRowIndexToView(row)
                    tableView.selectionModel.setSelectionInterval(viewRow, viewRow)
                    TableUtil.scrollSelectionToVisible(tableView)
                    break
                }
            }
        }
    }
    //双击打开模组依赖信息对话框
    object : DoubleClickListener() {
        override fun onDoubleClick(event: MouseEvent): Boolean {
            if(tableView.selectedRowCount != 1) return true
            val selectedRow = tableView.selectedRow
            val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
            ParadoxModDependencySettingsDialog(project, item).show()
            return true
        }
    }.installOn(tableView)
    
    val editButton = ParadoxModDependenciesToolbarActions.EditAction(project, tableView, tableModel)
    val importButton = ParadoxModDependenciesToolbarActions.ImportAction(project, tableView, tableModel)
    val exportButton = ParadoxModDependenciesToolbarActions.ExportAction(project, tableView, tableModel)
    
    //这里我们需要保证排序正确（基于表格中你的顺序）
    //始终将模组放到自身的模组依赖列表中，其排序可以调整
    //add, remove, move up, move down, edit, import, export
    val panel = ToolbarDecorator.createDecorator(tableView)
        .setAddAction { button ->
            //选择模组目录并添加作为依赖
            //TODO
        }
        .setRemoveActionUpdater updater@{
            if(modSettings == null) return@updater true
            if(modSettings.modDirectory.isNullOrEmpty()) return@updater true
            val selectedRows = tableView.selectedRows
            for(selectedRow in selectedRows) {
                val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
                if(item.modDirectory == modSettings.modDirectory) return@updater false
            }
            true
        }
        .addExtraAction(editButton)
        .addExtraAction(importButton)
        .addExtraAction(exportButton)
        .createPanel()
    val addButton = ToolbarDecorator.findAddButton(panel)!!
    val removeButton = ToolbarDecorator.findRemoveButton(panel)!!
    
    //右键弹出菜单，提供一些操作项
    val actionGroup = DefaultActionGroup()
    actionGroup.addAction(addButton)
    actionGroup.addAction(removeButton)
    actionGroup.addAction(editButton)
    actionGroup.addAction(importButton)
    actionGroup.addAction(exportButton)
    actionGroup.addAction(ParadoxModDependenciesPopupActions.OpenModPathAction(tableView, tableModel))
    actionGroup.addAction(ParadoxModDependenciesPopupActions.CopyModPathAction(tableView, tableModel))
    actionGroup.addAction(ParadoxModDependenciesPopupActions.OpenModPageOnSteamWebsiteAction(tableView, tableModel))
    actionGroup.addAction(ParadoxModDependenciesPopupActions.OpenModPageOnSteamAction(tableView, tableModel))
    PopupHandler.installPopupMenu(tableView, actionGroup, PlsToolsActions.MOD_DEPENDENCIES_POPUP)
    
    return panel
}
