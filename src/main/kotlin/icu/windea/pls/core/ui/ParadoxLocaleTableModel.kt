package icu.windea.pls.core.ui

import com.intellij.ui.*
import com.intellij.ui.table.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import java.awt.*
import javax.swing.*
import javax.swing.table.*

class ParadoxLocaleTableModel(
    val locales: MutableList<CwtLocalisationLocaleConfig>
) : ListTableModel<CwtLocalisationLocaleConfig>() {
    init {
        columnInfos = arrayOf(Item(this))
        items = locales
    }
    
    class Item(val tableModel: ParadoxLocaleTableModel) : ColumnInfo<CwtLocalisationLocaleConfig, CwtLocalisationLocaleConfig>(PlsBundle.message("ui.table.locale.column.name")) {
        companion object {
            const val columnIndex = 0
        }
        
        override fun valueOf(item: CwtLocalisationLocaleConfig): CwtLocalisationLocaleConfig {
            return item
        }
        
        override fun setValue(item: CwtLocalisationLocaleConfig, value: CwtLocalisationLocaleConfig) {
            val index = tableModel.locales.indexOf(item)
            tableModel.locales[index] = value
        }
        
        override fun isCellEditable(item: CwtLocalisationLocaleConfig): Boolean {
            return true
        }
        
        override fun getColumnClass(): Class<*> {
            return CwtLocalisationLocaleConfig::class.java
        }
        
        override fun getEditor(item: CwtLocalisationLocaleConfig): TableCellEditor {
            val localesToSelect = getLocalesToSelect(tableModel.locales, item)
            return DefaultCellEditor(JComboBox(localesToSelect.toTypedArray()))
        }
    }
}

fun createLocaleTableModel(locales: MutableList<CwtLocalisationLocaleConfig>): JPanel {
    val tableModel = ParadoxLocaleTableModel(locales)
    val tableView = TableView(tableModel)
    tableView.setShowGrid(false)
    tableView.rowSelectionAllowed = true
    tableView.columnSelectionAllowed = false
    tableView.intercellSpacing = Dimension(0, 0)
    tableView.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
    val panel = ToolbarDecorator.createDecorator(tableView)
        .disableUpDownActions()
        .createPanel()
    return panel
}