package icu.windea.pls.core.ui

import com.intellij.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.table.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import java.awt.*
import javax.swing.*

class ParadoxLocaleTableModel(
    val locales: MutableSet<String>
) : ListTableModel<String>() {
    init {
        columnInfos = arrayOf(SelectedItem(this), LocaleItem())
        items = getCwtConfig().core.localisationLocalesNoDefault.keys.toList()
    }
    
    class SelectedItem(val tableModel: ParadoxLocaleTableModel) : ColumnInfo<String, Boolean>(name) {
        companion object {
            const val columnIndex = 0
            const val name = ""
        }
        
        override fun valueOf(item: String): Boolean {
            return item in tableModel.locales
        }
    
        override fun setValue(item: String, value: Boolean) {
            if(value) {
                tableModel.locales.add(item)
            } else {
                tableModel.locales.remove(item)
            }
        }
    
        override fun isCellEditable(item: String?): Boolean {
            return true
        }
    
        override fun getColumnClass(): Class<*> {
            return Boolean::class.java
        }
    }
    
    class LocaleItem : ColumnInfo<String, String>(name) {
        companion object {
            const val columnIndex = 1
            val name = PlsBundle.message("ui.table.locale.column.name")
        }
        
        
        override fun valueOf(item: String): String {
            return item
        }
    }
}

fun createLocaleTableModel(locales: MutableSet<String>): JPanel {
    val tableModel = ParadoxLocaleTableModel(locales)
    val tableView = TableView(tableModel)
    tableView.setShowGrid(false)
    tableView.rowSelectionAllowed = true
    tableView.columnSelectionAllowed = false
    tableView.intercellSpacing = Dimension(0, 0)
    tableView.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
    tableView.setFixedColumnWidth(ParadoxLocaleTableModel.SelectedItem.columnIndex, "   ")
    val scrollPane = JBScrollPane(tableView)
    scrollPane.border = JBUI.Borders.empty()
    scrollPane.viewportBorder = JBUI.Borders.empty()
    scrollPane.preferredSize = Dimension(240, 120)
    scrollPane.minimumSize = Dimension(240, 120)
    val panel = JPanel(BorderLayout())
    panel.add(scrollPane, BorderLayout.CENTER)
    panel.border = IdeBorderFactory.createBorder()
    return panel
}