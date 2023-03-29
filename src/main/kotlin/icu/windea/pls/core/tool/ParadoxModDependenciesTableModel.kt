package icu.windea.pls.core.tool

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.ui.*
import com.intellij.ui.table.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.actions.*
import icu.windea.pls.core.ui.*
import java.awt.*
import java.awt.event.*
import javax.swing.*

//com.intellij.openapi.roots.ui.configuration.classpath.ClasspathTableModel

class ParadoxModDependenciesTableModel(
    val settings: ParadoxGameOrModSettingsState,
    val modDependencies: MutableList<ParadoxModDependencySettingsState>
) : ListTableModel<ParadoxModDependencySettingsState>(
    arrayOf(EnabledItem, NameItem, VersionItem, SupportedVersionItem),
    modDependencies
), EditableModel {
    val modDependencyDirectories = modDependencies.mapTo(mutableSetOf()) { it.modDirectory.orEmpty() }
    
    fun isCurrentAtLast(): Boolean {
        if(rowCount == 0) return false
        val currentModDirectory = settings.castOrNull<ParadoxModSettingsState>()?.modDirectory
        if(currentModDirectory == null) return false
        val lastRow = getItem(rowCount - 1)
        val lastModDirectory = lastRow.modDirectory
        return currentModDirectory == lastModDirectory
    }
    
    override fun removeRow(idx: Int) {
        //不允许移除模组自身对应的模组依赖配置
        if(!canRemoveRow(idx)) return
        modDependencyDirectories.remove(getItem(idx).modDirectory.orEmpty())
        super.removeRow(idx)
    }
    
    fun canRemoveRow(idx: Int): Boolean {
        if(settings !is ParadoxModSettingsState) return true
        if(settings.modDirectory.isNullOrEmpty()) return true
        val item = getItem(idx)
        return item.modDirectory != settings.modDirectory
    }
    
    fun insertRows(index: Int, items: Collection<ParadoxModDependencySettingsState>) {
        modDependencies.addAll(index, items)
        if(modDependencies.isNotEmpty()) {
            fireTableRowsInserted(index - items.size, index - 1)
        }
    }
    
    //注意这里的排序并不会实际改变modDependencies中模组依赖的排序
    
    object EnabledItem : ColumnInfo<ParadoxModDependencySettingsState, Boolean>(PlsBundle.message("mod.dependencies.column.name.enabled")) {
        const val columnIndex = 0
        
        override fun valueOf(item: ParadoxModDependencySettingsState): Boolean {
            return item.enabled
        }
        
        override fun setValue(item: ParadoxModDependencySettingsState, value: Boolean) {
            item.enabled = value
        }
        
        override fun isCellEditable(item: ParadoxModDependencySettingsState): Boolean {
            return true
        }
        
        override fun getColumnClass(): Class<*> {
            return Boolean::class.java
        }
    }
    
    object NameItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.dependencies.column.name.name")) {
        const val columnIndex = 1
        
        private val _comparator = compareBy<ParadoxModDependencySettingsState> { item -> item.name.orEmpty() }
        
        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            return item.name.orEmpty()
        }
        
        override fun getComparator(): Comparator<ParadoxModDependencySettingsState> {
            return _comparator
        }
    }
    
    object VersionItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.dependencies.column.version.name")) {
        const val columnIndex = 2
        
        private val _comparator = compareBy<ParadoxModDependencySettingsState> { item -> item.version.orEmpty() }
        
        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            return item.version.orEmpty()
        }
        
        override fun getComparator(): Comparator<ParadoxModDependencySettingsState> {
            return _comparator
        }
    }
    
    object SupportedVersionItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.dependencies.column.supportedVersion.name")) {
        const val columnIndex = 3
        
        private val _comparator = compareBy<ParadoxModDependencySettingsState> { item -> item.supportedVersion.orEmpty() }
        
        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            return item.supportedVersion.orEmpty()
        }
        
        override fun getComparator(): Comparator<ParadoxModDependencySettingsState> {
            return _comparator
        }
    }
}

//com.intellij.openapi.roots.ui.configuration.classpath.ClasspathPanelImpl.createTableWithButtons

fun createModDependenciesPanel(project: Project, settings: ParadoxGameOrModSettingsState, modDependencies: MutableList<ParadoxModDependencySettingsState>): JPanel {
    val tableModel = ParadoxModDependenciesTableModel(settings, modDependencies)
    val tableView = TableView(tableModel)
    tableView.setShowGrid(false)
    tableView.rowSelectionAllowed = true
    tableView.columnSelectionAllowed = false
    tableView.intercellSpacing = Dimension(0, 0)
    tableView.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
    //调整列的宽度
    tableView.setFixedColumnWidth(ParadoxModDependenciesTableModel.EnabledItem.columnIndex, ParadoxModDependenciesTableModel.EnabledItem.name)
    tableView.tableHeader.columnModel.getColumn(ParadoxModDependenciesTableModel.NameItem.columnIndex).preferredWidth = 10000 // consume all available space
    tableView.setFixedColumnWidth(ParadoxModDependenciesTableModel.VersionItem.columnIndex, ParadoxModDependenciesTableModel.VersionItem.name)
    tableView.setFixedColumnWidth(ParadoxModDependenciesTableModel.SupportedVersionItem.columnIndex, ParadoxModDependenciesTableModel.SupportedVersionItem.name)
    //快速搜索
    object : TableViewSpeedSearch<ParadoxModDependencySettingsState>(tableView) {
        override fun getItemText(element: ParadoxModDependencySettingsState): String {
            val modDirectory = element.modDirectory.orEmpty()
            val modDescriptorSettings = getProfilesSettings().modDescriptorSettings.getValue(modDirectory)
            return modDescriptorSettings.name.orEmpty()
        }
    }
    //双击打开模组依赖信息对话框
    object : DoubleClickListener() {
        override fun onDoubleClick(event: MouseEvent): Boolean {
            if(tableView.selectedRowCount != 1) return true
            val selectedRow = tableView.selectedRow
            val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
            ParadoxModDependencySettingsDialog(project, item, tableView).show()
            return true
        }
    }.installOn(tableView)
    
    val enableAllButton = ParadoxModDependenciesToolbarActions.EnableAllAction(project, tableView, tableModel)
    val disableAllButton = ParadoxModDependenciesToolbarActions.DisableAllAction(project, tableView, tableModel)
    val editButton = ParadoxModDependenciesToolbarActions.EditAction(project, tableView, tableModel)
    val importButton = ParadoxModDependenciesToolbarActions.ImportAction(project, tableView, tableModel)
    val exportButton = ParadoxModDependenciesToolbarActions.ExportAction(project, tableView, tableModel)
    
    //这里我们需要保证排序正确（基于表格中你的顺序）
    //始终将模组放到自身的模组依赖列表中，其排序可以调整
    //add, remove, move up, move down, edit, import, export
    val panel = ToolbarDecorator.createDecorator(tableView)
        .setAddAction {
            val dialog = ParadoxModDependencyAddDialog(project, tableView, tableModel)
            dialog.show()
        }
        .setRemoveActionUpdater updater@{
            //不允许移除模组自身对应的模组依赖配置
            val selectedRow = tableView.selectedRows.singleOrNull() ?: return@updater true
            tableModel.canRemoveRow(tableView.convertRowIndexToModel(selectedRow))
        }
        .addExtraAction(enableAllButton)
        .addExtraAction(disableAllButton)
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
    actionGroup.addAction(enableAllButton)
    actionGroup.addAction(disableAllButton)
    actionGroup.addAction(editButton)
    actionGroup.addAction(importButton)
    actionGroup.addAction(exportButton)
    actionGroup.addSeparator()
    actionGroup.addAction(ParadoxModDependenciesPopupActions.OpenModPathAction(tableView, tableModel))
    actionGroup.addAction(ParadoxModDependenciesPopupActions.CopyModPathAction(tableView, tableModel))
    actionGroup.addAction(ParadoxModDependenciesPopupActions.OpenModPageOnSteamWebsiteAction(tableView, tableModel))
    actionGroup.addAction(ParadoxModDependenciesPopupActions.OpenModPageOnSteamAction(tableView, tableModel))
    PopupHandler.installPopupMenu(tableView, actionGroup, PlsToolsActions.MOD_DEPENDENCIES_POPUP)
    return panel
}
