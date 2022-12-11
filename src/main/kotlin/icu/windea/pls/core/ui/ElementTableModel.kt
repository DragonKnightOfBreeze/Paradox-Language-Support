package icu.windea.pls.core.ui

import com.intellij.openapi.ui.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import javax.swing.*
import javax.swing.table.*

//com.intellij.refactoring.changeSignature.ParameterTableModelBase

class ElementTableModel(
	dialog: ExpandClauseTemplateDialog
) : ListTableModel<ElementDescriptor>(), EditableModel {
	init {
		columnInfos = arrayOf(NameColumn(this), SeparatorColumn(this), ValueColumn(this))
		items = dialog.resultDescriptors
	}
	
	override fun addRow() {
		addRow(NewPropertyDescriptor(name = "key", value = "value"))
	}
	
	class NameColumn(private val tableModel: ElementTableModel) : ColumnInfo<ElementDescriptor, String>(PlsBundle.message("column.name.name")) {
		override fun isCellEditable(item: ElementDescriptor): Boolean {
			return when(item){
				is PropertyDescriptor -> false
				is ValueDescriptor -> false
				is NewPropertyDescriptor -> true 
			}
		}
		
		override fun valueOf(item: ElementDescriptor): String {
			return item.name
		}
		
		override fun getRenderer(item: ElementDescriptor): TableCellRenderer {
			return DefaultTableCellRenderer()
		}
		
		override fun getEditor(item: ElementDescriptor): TableCellEditor? {
			return when(item) {
				is PropertyDescriptor -> null
				is ValueDescriptor -> null
				is NewPropertyDescriptor -> DefaultCellEditor(JTextField())
			}
		}
	}
	
	class SeparatorColumn(private val tableModel: ElementTableModel) : ColumnInfo<ElementDescriptor, ParadoxSeparator>(PlsBundle.message("column.name.separator")) {
		override fun isCellEditable(item: ElementDescriptor): Boolean {
			return item is PropertyDescriptor
		}
		
		override fun valueOf(item: ElementDescriptor): ParadoxSeparator? {
			return when(item) {
				is PropertyDescriptor -> item.separator
				is ValueDescriptor -> null
				is NewPropertyDescriptor -> item.separator
			}
		}
		
		override fun setValue(item: ElementDescriptor, value: ParadoxSeparator) {
			when(item) {
				is PropertyDescriptor -> item.separator = value
				is ValueDescriptor -> pass()
				is NewPropertyDescriptor -> item.separator = value
			}
		}
		
		override fun getRenderer(item: ElementDescriptor): TableCellRenderer? {
			return when(item){
				is PropertyDescriptor -> ComboBoxTableRenderer(ParadoxSeparator.values())
				is ValueDescriptor -> null
				is NewPropertyDescriptor -> DefaultTableCellRenderer()
			} 
		}
		
		override fun getEditor(item: ElementDescriptor): TableCellEditor? {
			return when(item){
				is PropertyDescriptor -> DefaultCellEditor(ComboBox(ParadoxSeparator.values()))
				is ValueDescriptor -> null
				is NewPropertyDescriptor -> DefaultCellEditor(ComboBox(ParadoxSeparator.values()))
			}
		}
		
		override fun getWidth(table: JTable): Int {
			return table.getFontMetrics(table.font).stringWidth(name) + 8
		}
	}
	
	class ValueColumn(private val tableModel: ElementTableModel) : ColumnInfo<ElementDescriptor, String>(PlsBundle.message("column.name.value")) {
		override fun isCellEditable(item: ElementDescriptor): Boolean {
			return item is PropertyDescriptor && item.constantValues.isNotEmpty()
		}
		
		override fun valueOf(item: ElementDescriptor): String? {
			return when(item) {
				is PropertyDescriptor -> item.value
				is ValueDescriptor -> null
				is NewPropertyDescriptor -> item.value
			}
		}
		
		override fun setValue(item: ElementDescriptor, value: String?) {
			when(item) {
				is PropertyDescriptor -> item.value = value.orEmpty()
				is ValueDescriptor -> pass()
				is NewPropertyDescriptor -> item.value = value.orEmpty()
			}
		}
		
		override fun getRenderer(item: ElementDescriptor): TableCellRenderer? {
			return when(item) {
				is PropertyDescriptor -> {
					if(item.constantValues.isNotEmpty()) {
						ComboBoxTableRenderer(item.constantValueArray) 
					} else {
						DefaultTableCellRenderer()
					}
				}
				is ValueDescriptor -> null 
				is NewPropertyDescriptor -> DefaultTableCellRenderer()
			}
		}
		
		override fun getEditor(item: ElementDescriptor): TableCellEditor? {
			return when(item) {
				is PropertyDescriptor -> {
					if(item.constantValues.isNotEmpty()) {
						DefaultCellEditor(ComboBox(item.constantValueArray))
					} else {
						null
					}
				}
				is ValueDescriptor -> null
				is NewPropertyDescriptor -> DefaultCellEditor(JTextField())
			}
		}
	}
}