package icu.windea.pls.lang.ui

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
    val keySetter: ((String) -> K)?,
    val valueGetter: (V) -> String,
    val valueSetter: ((String) -> V)?,
    val valueAdder: (() -> Entry<K, V>)?,
) : ListTableModel<Entry<K, V>>(
    arrayOf(
        object : ColumnInfo<Entry<K, V>, String>("") {
            override fun getName(): String {
                return keyName
            }

            override fun valueOf(item: Entry<K, V>): String {
                return keyGetter(item.key)
            }

            override fun setValue(item: Entry<K, V>, value: String) {
                if (keySetter == null) return
                item.key = keySetter(value)
            }

            override fun isCellEditable(item: Entry<K, V>?): Boolean {
                return keySetter != null
            }
        },
        object : ColumnInfo<Entry<K, V>, String>("") {
            override fun getName(): String {
                return valueName
            }

            override fun valueOf(item: Entry<K, V>): String {
                return valueGetter(item.value)
            }

            override fun setValue(item: Entry<K, V>, value: String) {
                if (valueSetter == null) return
                item.value = valueSetter(value)
            }

            override fun isCellEditable(item: Entry<K, V>?): Boolean {
                return valueSetter != null
            }
        }
    ),
    list
) {
    override fun addRow() {
        if (valueAdder == null) return
        addRow(valueAdder.invoke())
    }

    companion object {
        @JvmStatic
        fun createStringMapPanel(
            list: MutableList<Entry<String, String>>,
            keyName: String,
            valueName: String,
            keyGetter: (String) -> String = { it },
            keySetter: ((String) -> String)? = { it },
            valueGetter: (String) -> String = { it },
            valueSetter: ((String) -> String)? = { it },
            valueAdder: (() -> Entry<String, String>)? = { Entry("", "") },
            customizer: (ToolbarDecorator) -> Unit = {}
        ): JPanel {
            return createMapPanel(list, keyName, valueName, keyGetter, keySetter, valueGetter, valueSetter, valueAdder, customizer)
        }

        @JvmStatic
        fun <K, V> createMapPanel(
            list: MutableList<Entry<K, V>>,
            keyName: String,
            valueName: String,
            keyGetter: (K) -> String,
            keySetter: ((String) -> K)?,
            valueGetter: (V) -> String,
            valueSetter: ((String) -> V)?,
            valueAdder: (() -> Entry<K, V>)?,
            customizer: (ToolbarDecorator) -> Unit = {}
        ): JPanel {
            val tableModel = EntryListTableModel(list, keyName, valueName, keyGetter, keySetter, valueGetter, valueSetter, valueAdder)
            val tableView = TableView(tableModel)
            tableView.setShowGrid(false)
            tableView.rowSelectionAllowed = true
            tableView.columnSelectionAllowed = false
            tableView.intercellSpacing = Dimension(0, 0)
            tableView.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
            tableView.updateColumnSizes()
            //快速搜索
            val speedSearch = object : TableViewSpeedSearch<Entry<K, V>>(tableView, null) {
                override fun getItemText(element: Entry<K, V>): String {
                    return keyGetter(element.key)
                }
            }
            speedSearch.setupListeners()
            val decorator = ToolbarDecorator.createDecorator(tableView)
            if (valueAdder == null) decorator.disableAddAction()
            customizer(decorator)
            val panel = decorator.createPanel()
            return panel
        }
    }
}
