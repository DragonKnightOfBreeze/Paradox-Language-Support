package icu.windea.pls.core.ui

import com.intellij.openapi.ui.*
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import javax.swing.*
import javax.swing.table.*

//com.intellij.refactoring.changeSignature.ParameterTableModelBase

class ElementTableModel(
	val descriptors: MutableList<ElementDescriptor>
) : ListTableModel<ElementDescriptor>(
	NameColumn(),
	SeparatorColumn(),
	ValueColumn()
), EditableModel {
	class NameColumn : ColumnInfo<ElementDescriptor, String>(PlsBundle.message("column.name.name")) {
		override fun isCellEditable(item: ElementDescriptor): Boolean {
			return false
		}
		
		override fun valueOf(item: ElementDescriptor): String {
			return item.name
		}
		
		override fun getRenderer(item: ElementDescriptor?): TableCellRenderer {
			return object : ColoredTableCellRenderer() {
				override fun customizeCellRenderer(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
					if(value == null) return
					append(value as String, SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, null))
				}
			}
		}
	}
	
	class SeparatorColumn : ColumnInfo<ElementDescriptor, ParadoxSeparator>(PlsBundle.message("column.name.separator")) {
		override fun isCellEditable(item: ElementDescriptor): Boolean {
			return item is PropertyDescriptor
		}
		
		override fun valueOf(item: ElementDescriptor): ParadoxSeparator? {
			return when(item) {
				is PropertyDescriptor -> item.separator
				is ValueDescriptor -> null
			}
		}
		
		override fun setValue(item: ElementDescriptor, value: ParadoxSeparator) {
			when(item) {
				is PropertyDescriptor -> item.separator = value
				is ValueDescriptor -> pass()
			}
		}
		
		override fun getRenderer(item: ElementDescriptor): TableCellRenderer? {
			return when(item){
				is PropertyDescriptor -> ComboBoxTableRenderer(ParadoxSeparator.values())
				is ValueDescriptor -> null
			} 
		}
		
		override fun getEditor(item: ElementDescriptor): TableCellEditor? {
			return when(item){
				is PropertyDescriptor -> DefaultCellEditor(ComboBox<ElementDescriptor>())
				is ValueDescriptor -> null
			}
		}
		
		override fun getWidth(table: JTable): Int {
			return table.getFontMetrics(table.font).stringWidth(name) + 8
		}
	}
	
	class ValueColumn : ColumnInfo<ElementDescriptor, String>(PlsBundle.message("column.name.value")) {
		override fun isCellEditable(item: ElementDescriptor): Boolean {
			return item is PropertyDescriptor && item.constantValues.isNotEmpty()
		}
		
		override fun valueOf(item: ElementDescriptor): String? {
			return when(item) {
				is PropertyDescriptor -> item.value
				is ValueDescriptor -> null
			}
		}
		
		override fun setValue(item: ElementDescriptor, value: String) {
			when(item) {
				is PropertyDescriptor -> item.value = value
				is ValueDescriptor -> pass()
			}
		}
		
		override fun getRenderer(item: ElementDescriptor): TableCellRenderer? {
			return when(item) {
				is PropertyDescriptor -> {
					if(item.constantValues.isNotEmpty()) {
						ValueComboBoxTableRender(item.constantValues) 
					} else {
						ValueTableRender()
					}
				}
				is ValueDescriptor -> null 
			}
		}
		
		override fun getEditor(item: ElementDescriptor): TableCellEditor? {
			return when(item) {
				is PropertyDescriptor -> {
					if(item.constantValues.isNotEmpty()) {
						DefaultCellEditor(ComboBox<ElementDescriptor>())
					} else {
						null
					}
				}
				is ValueDescriptor -> null
			}
		}
	}
	
	class ValueComboBoxTableRender(values: List<String>): ComboBoxTableRenderer<String>(values.toTypedArray()) {
		override fun getTextFor(value: String): String {
			return value.ifEmpty { PlsConstants.unsetString }
		}
	}
	
	class ValueTableRender: DefaultTableCellRenderer() {
		override fun getText(): String {
			return super.getText().ifEmpty { PlsConstants.unsetString }
		}
	}
	
	init {
		items = descriptors
	}
}