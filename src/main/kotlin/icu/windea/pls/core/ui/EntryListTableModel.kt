package icu.windea.pls.core.ui

import com.intellij.ui.*
import com.intellij.ui.table.*
import com.intellij.util.ui.*
import icu.windea.pls.core.util.*
import java.awt.*
import javax.swing.*

class EntryListTableModel<K, V>(
    val list: MutableList<Entry<K, V>>,
    val keyName: String,
    val valueName: String,
    val keyGetter: (K) -> String,
    val valueGetter: (V) -> String,
    val valueSetter: (String) -> V,
) : ListTableModel<Entry<K, V>>(
    arrayOf(
        object : ColumnInfo<Entry<K, V>, String>("") {
            override fun getName(): String {
                return keyName
            }
            
            override fun valueOf(item: Entry<K, V>): String {
                return keyGetter(item.key)
            }
        },
        object : ColumnInfo<Entry<K, V>, String>("") {
            override fun getName(): String {
                return keyName
            }
            
            override fun valueOf(item: Entry<K, V>): String {
                return valueGetter(item.value)
            }
            
            override fun setValue(item: Entry<K, V>, value: String) {
                item.value = valueSetter(value)
            }
            
            override fun isCellEditable(item: Entry<K, V>?): Boolean {
                return true
            }
        }
    ),
    list
) {
    override fun addRow(item: Entry<K, V>?) {
        super.addRow(item)
    }
    
    companion object {
        @JvmStatic
        fun createStringMapPanel(
            list: MutableList<Entry<String, String>>,
            keyName: String,
            valueName: String,
            customizer: (ToolbarDecorator) -> Unit = {}
        ): JPanel {
            return createMapPanel(list, keyName, valueName, { it }, { it }, { it }, customizer)
        }
        
        @JvmStatic
        fun <K, V> createMapPanel(
            list: MutableList<Entry<K, V>>,
            keyName: String,
            valueName: String,
            keyGetter: (K) -> String,
            valueGetter: (V) -> String,
            valueSetter: (String) -> V,
            customizer: (ToolbarDecorator) -> Unit = {}
        ): JPanel {
            val tableModel = EntryListTableModel(list, keyName, valueName, keyGetter, valueGetter, valueSetter)
            val tableView = TableView(tableModel)
            tableView.setShowGrid(false)
            tableView.rowSelectionAllowed = true
            tableView.columnSelectionAllowed = false
            tableView.intercellSpacing = Dimension(0, 0)
            tableView.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
            //快速搜索
            object : TableViewSpeedSearch<Entry<K, V>>(tableView) {
                override fun getItemText(element: Entry<K, V>): String {
                    return keyGetter(element.key)
                }
            }
            val decorator = ToolbarDecorator.createDecorator(tableView)
            customizer(decorator)
            val panel = decorator.createPanel()
            return panel
        }
    }
}