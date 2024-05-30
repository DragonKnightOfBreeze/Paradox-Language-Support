package icu.windea.pls.lang.tools

import com.intellij.ui.table.*
import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import java.awt.*
import javax.swing.*
import javax.swing.event.*

class ParadoxModDependenciesTableView(
    tableModel: ListTableModel<ParadoxModDependencySettingsState>
): TableView<ParadoxModDependencySettingsState>(tableModel) {
    init {
        setShowGrid(false)
        rowSelectionAllowed = true
        columnSelectionAllowed = false
        intercellSpacing = Dimension(0, 0)
        selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        setColumnSizes()
    }
    
    override fun tableChanged(e: TableModelEvent) {
        super.tableChanged(e)
        if(e.type == TableModelEvent.INSERT) setColumnSizes() //防止插入条目到空表格后，列的宽度被重置
    }
    
    private fun setColumnSizes() {
        //调整列的宽度
        setFixedColumnWidth(ParadoxModDependenciesTableModel.EnabledItem.columnIndex, ParadoxModDependenciesTableModel.EnabledItem.name)
        setFixedColumnWidth(ParadoxModDependenciesTableModel.VersionItem.columnIndex, ParadoxModDependenciesTableModel.VersionItem.name)
        setFixedColumnWidth(ParadoxModDependenciesTableModel.SupportedVersionItem.columnIndex, ParadoxModDependenciesTableModel.SupportedVersionItem.name)
        tableHeader.columnModel.getColumn(ParadoxModDependenciesTableModel.NameItem.columnIndex).preferredWidth = 10000 // consume all available space
    }
}
