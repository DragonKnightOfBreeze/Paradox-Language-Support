package icu.windea.pls.core.ui

import com.intellij.openapi.ui.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*
import javax.swing.*
import javax.swing.table.*

//com.intellij.refactoring.changeSignature.ParameterTableModelBase

class ElementsTableModel(
    val context: ElementsContext
) : ListTableModel<ElementDescriptor>(
    arrayOf(NameColumn(context), SeparatorColumn(context), ValueColumn(context)),
    context.descriptorsInfo.resultDescriptors,
), EditableModel {
    override fun addRow() {
        addRow(PropertyDescriptor())
    }
    
    class NameColumn(private val context: ElementsContext) : ColumnInfo<ElementDescriptor, String>(PlsBundle.message("ui.table.element.column.name.name")) {
        override fun isCellEditable(item: ElementDescriptor): Boolean {
            return true
        }
        
        override fun valueOf(item: ElementDescriptor): String {
            return item.name
        }
        
        override fun setValue(item: ElementDescriptor, value: String?) {
            return when(item) {
                is ValueDescriptor -> item.name = value.orEmpty()
                is PropertyDescriptor -> item.name = value.orEmpty()
            }
        }
        
        override fun getRenderer(item: ElementDescriptor): TableCellRenderer {
            return when(item) {
                is ValueDescriptor -> ComboBoxTableRenderer(context.descriptorsInfo.allValues)
                is PropertyDescriptor -> ComboBoxTableRenderer(context.descriptorsInfo.allKeys)
            }
        }
        
        override fun getEditor(item: ElementDescriptor): TableCellEditor {
            return when(item) {
                is ValueDescriptor -> ComboBoxTableRenderer(context.descriptorsInfo.allValues)
                is PropertyDescriptor -> ComboBoxTableRenderer(context.descriptorsInfo.allKeys)
            }
        }
    }
    
    class SeparatorColumn(private val context: ElementsContext) : ColumnInfo<ElementDescriptor, ParadoxSeparatorType>(PlsBundle.message("ui.table.element.column.name.separator")) {
        override fun isCellEditable(item: ElementDescriptor): Boolean {
            return item is PropertyDescriptor
        }
        
        override fun valueOf(item: ElementDescriptor): ParadoxSeparatorType? {
            return when(item) {
                is ValueDescriptor -> null
                is PropertyDescriptor -> item.separator
            }
        }
        
        override fun setValue(item: ElementDescriptor, value: ParadoxSeparatorType) {
            when(item) {
                is ValueDescriptor -> pass()
                is PropertyDescriptor -> item.separator = value
            }
        }
        
        override fun getRenderer(item: ElementDescriptor): TableCellRenderer? {
            return when(item) {
                is ValueDescriptor -> null
                is PropertyDescriptor -> ComboBoxTableRenderer(ParadoxSeparatorType.values())
            }
        }
        
        override fun getEditor(item: ElementDescriptor): TableCellEditor? {
            return when(item) {
                is ValueDescriptor -> null
                is PropertyDescriptor -> ComboBoxTableRenderer(ParadoxSeparatorType.values())
            }
        }
    }
    
    class ValueColumn(private val context: ElementsContext) : ColumnInfo<ElementDescriptor, String>(PlsBundle.message("ui.table.element.column.name.value")) {
        override fun isCellEditable(item: ElementDescriptor): Boolean {
            return item is PropertyDescriptor
        }
        
        override fun valueOf(item: ElementDescriptor): String? {
            return when(item) {
                is ValueDescriptor -> null
                is PropertyDescriptor -> item.value
            }
        }
        
        override fun setValue(item: ElementDescriptor, value: String?) {
            when(item) {
                is ValueDescriptor -> pass()
                is PropertyDescriptor -> item.value = value.orEmpty()
            }
        }
        
        override fun getRenderer(item: ElementDescriptor): TableCellRenderer? {
            return when(item) {
                is ValueDescriptor -> null
                is PropertyDescriptor -> {
                    val constantValues = context.descriptorsInfo.allKeyValuesMap[item.name].orEmpty()
                    val items = constantValues.ifEmpty { arrayOf("") }
                    ComboBoxTableRenderer(items)
                }
            }
        }
        
        override fun getEditor(item: ElementDescriptor): TableCellEditor? {
            return when(item) {
                is ValueDescriptor -> null
                is PropertyDescriptor -> {
                    val constantValues = context.descriptorsInfo.allKeyValuesMap[item.name].orEmpty()
                    val items = constantValues.ifEmpty { arrayOf("") }
                    DefaultCellEditor(ComboBox(items))
                }
            }
        }
    }
}