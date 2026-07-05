package icu.windea.pls.lang.ui.clause

import com.intellij.openapi.ui.ComboBoxTableRenderer
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.EditableModel
import com.intellij.util.ui.ListTableModel
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.pass
import icu.windea.pls.model.type.ParadoxSeparatorType
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

// com.intellij.refactoring.changeSignature.ParameterTableModelBase

class ElementsTableModel(
    val context: ElementsContext
) : ListTableModel<ElementDescriptor>(
    arrayOf(NameColumn(context), SeparatorColumn(), ValueColumn(context)),
    context.descriptorsInfo.resultDescriptors
), EditableModel {
    override fun addRow() {
        addRow(ElementDescriptors.Property())
    }

    class NameColumn(private val context: ElementsContext) : ColumnInfo<ElementDescriptor, String>(ChronicleBundle.message("ui.table.element.column.name.name")) {
        override fun isCellEditable(item: ElementDescriptor): Boolean {
            return true
        }

        override fun valueOf(item: ElementDescriptor): String {
            return item.name
        }

        override fun setValue(item: ElementDescriptor, value: String?) {
            return when (item) {
                is ElementDescriptors.Value -> item.name = value.orEmpty()
                is ElementDescriptors.Property -> item.name = value.orEmpty()
            }
        }

        override fun getRenderer(item: ElementDescriptor): TableCellRenderer {
            return when (item) {
                is ElementDescriptors.Value -> ComboBoxTableRenderer(context.descriptorsInfo.allValues)
                is ElementDescriptors.Property -> ComboBoxTableRenderer(context.descriptorsInfo.allKeys)
            }
        }

        override fun getEditor(item: ElementDescriptor): TableCellEditor {
            return when (item) {
                is ElementDescriptors.Value -> ComboBoxTableRenderer(context.descriptorsInfo.allValues)
                is ElementDescriptors.Property -> ComboBoxTableRenderer(context.descriptorsInfo.allKeys)
            }
        }
    }

    class SeparatorColumn : ColumnInfo<ElementDescriptor, ParadoxSeparatorType>(ChronicleBundle.message("ui.table.element.column.name.separator")) {
        override fun isCellEditable(item: ElementDescriptor): Boolean {
            return item is ElementDescriptors.Property
        }

        override fun valueOf(item: ElementDescriptor): ParadoxSeparatorType? {
            return when (item) {
                is ElementDescriptors.Value -> null
                is ElementDescriptors.Property -> item.separator
            }
        }

        override fun setValue(item: ElementDescriptor, value: ParadoxSeparatorType) {
            when (item) {
                is ElementDescriptors.Value -> pass()
                is ElementDescriptors.Property -> item.separator = value
            }
        }

        override fun getRenderer(item: ElementDescriptor): TableCellRenderer? {
            return when (item) {
                is ElementDescriptors.Value -> null
                is ElementDescriptors.Property -> ComboBoxTableRenderer(ParadoxSeparatorType.entries.toTypedArray())
            }
        }

        override fun getEditor(item: ElementDescriptor): TableCellEditor? {
            return when (item) {
                is ElementDescriptors.Value -> null
                is ElementDescriptors.Property -> ComboBoxTableRenderer(ParadoxSeparatorType.entries.toTypedArray())
            }
        }
    }

    class ValueColumn(private val context: ElementsContext) : ColumnInfo<ElementDescriptor, String>(ChronicleBundle.message("ui.table.element.column.name.value")) {
        override fun isCellEditable(item: ElementDescriptor): Boolean {
            return item is ElementDescriptors.Property && item.constantValues.isNotEmpty()
        }

        override fun valueOf(item: ElementDescriptor): String? {
            return when (item) {
                is ElementDescriptors.Value -> null
                is ElementDescriptors.Property -> item.value
            }
        }

        override fun setValue(item: ElementDescriptor, value: String?) {
            when (item) {
                is ElementDescriptors.Value -> pass()
                is ElementDescriptors.Property -> item.value = value.orEmpty()
            }
        }

        override fun getRenderer(item: ElementDescriptor): TableCellRenderer? {
            return when (item) {
                is ElementDescriptors.Value -> null
                is ElementDescriptors.Property -> {
                    val constantValues = context.descriptorsInfo.allKeyValuesMap[item.name].orEmpty()
                    val items = constantValues.ifEmpty { arrayOf("") }
                    ComboBoxTableRenderer(items)
                }
            }
        }

        override fun getEditor(item: ElementDescriptor): TableCellEditor? {
            return when (item) {
                is ElementDescriptors.Value -> null
                is ElementDescriptors.Property -> {
                    val constantValues = context.descriptorsInfo.allKeyValuesMap[item.name].orEmpty()
                    val items = constantValues.ifEmpty { arrayOf("") }
                    ComboBoxTableRenderer(items)
                }
            }
        }
    }
}
