package icu.windea.pls.core.ui

import com.intellij.openapi.ui.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.core.*
import javax.swing.*
import javax.swing.table.*

//com.intellij.refactoring.changeSignature.ParameterTableModelBase

class ElementTableModel(
	context: ElementDescriptorContext
) : ListTableModel<ElementDescriptor>(), EditableModel {
	init {
		columnInfos = arrayOf(NameColumn(context), SeparatorColumn(context), ValueColumn(context))
		items = context.resultDescriptors
	}
	
	override fun addRow() {
		addRow(PropertyDescriptor())
	}
	
	class NameColumn(private val context: ElementDescriptorContext) : ColumnInfo<ElementDescriptor, String>(PlsBundle.message("column.name.name")) {
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
				is ValueDescriptor -> ComboBoxTableRenderer(context.allValues)
				is PropertyDescriptor -> ComboBoxTableRenderer(context.allKeys)
			}
		}
		
		override fun getEditor(item: ElementDescriptor): TableCellEditor {
			return when(item) {
				is ValueDescriptor -> DefaultCellEditor(ComboBox(context.allValues))
				is PropertyDescriptor -> DefaultCellEditor(ComboBox(context.allKeys))
			}
		}
	}
	
	class SeparatorColumn(private val context: ElementDescriptorContext) : ColumnInfo<ElementDescriptor, ParadoxSeparator>(PlsBundle.message("column.name.separator")) {
		override fun isCellEditable(item: ElementDescriptor): Boolean {
			return item is PropertyDescriptor
		}
		
		override fun valueOf(item: ElementDescriptor): ParadoxSeparator? {
			return when(item) {
				is ValueDescriptor -> null
				is PropertyDescriptor -> item.separator
			}
		}
		
		override fun setValue(item: ElementDescriptor, value: ParadoxSeparator) {
			when(item) {
				is ValueDescriptor -> pass()
				is PropertyDescriptor -> item.separator = value
			}
		}
		
		override fun getRenderer(item: ElementDescriptor): TableCellRenderer? {
			return when(item) {
				is ValueDescriptor -> null
				is PropertyDescriptor -> ComboBoxTableRenderer(ParadoxSeparator.values())
			}
		}
		
		override fun getEditor(item: ElementDescriptor): TableCellEditor? {
			return when(item) {
				is ValueDescriptor -> null
				is PropertyDescriptor -> DefaultCellEditor(ComboBox(ParadoxSeparator.values()))
			}
		}
		
		override fun getWidth(table: JTable): Int {
			return table.getFontMetrics(table.font).stringWidth(name) + 8
		}
	}
	
	class ValueColumn(private val context: ElementDescriptorContext) : ColumnInfo<ElementDescriptor, String>(PlsBundle.message("column.name.value")) {
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
					val constantValues = context.allKeyValuesMap[item.name].orEmpty()
					val items = constantValues.ifEmpty { arrayOf("") }
					ComboBoxTableRenderer(items)
				}
			}
		}
		
		override fun getEditor(item: ElementDescriptor): TableCellEditor? {
			return when(item) {
				is ValueDescriptor -> null
				is PropertyDescriptor -> {
					val constantValues = context.allKeyValuesMap[item.name].orEmpty()
					val items = constantValues.ifEmpty { arrayOf("") }
					DefaultCellEditor(ComboBox(items))
				}
			}
		}
	}
}