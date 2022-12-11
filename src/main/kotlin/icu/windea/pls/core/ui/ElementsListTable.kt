package icu.windea.pls.core.ui

import com.intellij.openapi.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.*
import com.intellij.ui.table.*
import com.intellij.util.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import javax.swing.*

//com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase.ParametersListTable

class ElementsListTable(
	private val project: Project,
	private val elementsTable: TableView<ElementDescriptor>,
	private val elementsTableModel: ElementTableModel,
	disposable: Disposable
): JBListTable(elementsTable, disposable) {
	val _rowRenderer = object : EditorTextFieldJBTableRowRenderer(project, ParadoxScriptLanguage, disposable) {
		override fun getText(table: JTable, row: Int): String {
			val item = getRowItem(row)
			return when(item) {
				is PropertyDescriptor -> {
					if(item.value.isEmpty()) {
						"${item.name} ${item.separator} \"\" # ${PlsBundle.message("column.tooltip.unset")}"
					}  else {
						"${item.name} ${item.separator} ${item.value}}}"
					}
				}
				is ValueDescriptor -> {
					item.name
				}
			}
		}
	}
	
	override fun getRowRenderer(row: Int): JBTableRowRenderer {
		return _rowRenderer
	}
	
	val _rowEditor = object : JBTableRowEditor() {
		private val components = ArrayList<JComponent>()
		
		override fun prepareEditor(table: JTable, row: Int) {
			layout = BoxLayout(this, BoxLayout.X_AXIS)
			for((index, columnInfo) in elementsTableModel.columnInfos.withIndex()) {
				val panel = JPanel(VerticalFlowLayout(VerticalFlowLayout.TOP, 4, 2, true, false))
				val component: JComponent
				
				when(columnInfo) {
					is ElementTableModel.NameColumn -> {
						val nameField = JTextField()
						nameField.isEditable = false
						component = nameField
					}
					is ElementTableModel.SeparatorColumn -> {
						val separatorComboBox = ComboBox(ParadoxSeparator.values())
						component = separatorComboBox
					}
					is ElementTableModel.ValueColumn -> {
						val descriptor = elementsTableModel.descriptors[index] as PropertyDescriptor
						if(descriptor.constantValues.isNotEmpty()) {
							val valueField = JTextField()
							valueField.isEditable = false
							component = valueField
						} else {
							val valueComboBox = ComboBox(descriptor.constantValues.toTypedArray())
							component = valueComboBox
						}
					}
					else -> {
						continue
					}
				}
				components.add(component)
				panel.add(component)
				add(panel)
			}
		}
		
		@Suppress("UNCHECKED_CAST")
		override fun getValue(): JBTableRow {
			return JBTableRow { column -> 
				val columnInfo = elementsTableModel.columnInfos[column]
				when(columnInfo) {
					is ElementTableModel.NameColumn -> components[0].let {
						it.castOrNull<JTextField>()?.text
					}
					is ElementTableModel.SeparatorColumn -> components[1].let { 
						it.castOrNull<ComboBox<ParadoxSeparator>>()?.item
					}
					is ElementTableModel.ValueColumn -> components[2].let { 
						it.castOrNull<JTextField>()?.text ?: it.castOrNull<ComboBox<String>>()?.item
					}
					else -> null
				}
			}
		}
		
		override fun getPreferredFocusedComponent(): JComponent {
			return components.first()
		}
		
		override fun getFocusableComponents(): Array<JComponent> {
			return Array(components.size) {
				val component = components[it]
				(component as? EditorTextField)?.focusTarget ?: component
			}
		}
	}
	
	override fun getRowEditor(row: Int): JBTableRowEditor {
		return _rowEditor
	}
	
	private fun getRowItem(row: Int): ElementDescriptor {
		return elementsTable.items.get(row)
	}
}