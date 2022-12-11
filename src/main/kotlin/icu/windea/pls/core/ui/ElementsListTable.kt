package icu.windea.pls.core.ui

import com.intellij.openapi.observable.util.*
import com.intellij.openapi.ui.*
import com.intellij.util.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import java.awt.Dimension
import javax.swing.*

//com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase.ParametersListTable

class ElementsListTable(
	private val dialog: ExpandClauseTemplateDialog
): JBListTable(dialog.elementsTable, dialog.disposable) {
	
	val _rowRenderer = object : EditorTextFieldJBTableRowRenderer(dialog.project, ParadoxScriptLanguage, dialog.disposable) {
		override fun getText(table: JTable, row: Int): String {
			val item = getRowItem(row)
			return when(item) {
				is PropertyDescriptor -> {
					if(item.value.isEmpty()) {
						"${item.name} ${item.separator} \"\" # ${PlsBundle.message("column.tooltip.unset")}"
					} else {
						"${item.name} ${item.separator} ${item.value}"
					}
				}
				is ValueDescriptor -> {
					item.name
				}
				is NewPropertyDescriptor -> {
					if(item.value.isEmpty()) {
						"${item.name} ${item.separator} \"\" # ${PlsBundle.message("column.tooltip.unset")}"
					} else {
						"${item.name} ${item.separator} ${item.value}"
					}
				}
			}
		}
	}
	
	override fun getRowRenderer(row: Int): JBTableRowRenderer {
		return _rowRenderer
	}
	
	override fun getRowEditor(row: Int): JBTableRowEditor {
		return object : JBTableRowEditor() {
			private var nameField: JTextField? = null
			private var separatorComboBox: ComboBox<ParadoxSeparator>? = null
			private var valueField: JTextField? = null
			private var valueComboBox: ComboBox<String>? = null
			
			override fun prepareEditor(table: JTable, row: Int) {
				val item = getRowItem(row)
				layout = BoxLayout(this, BoxLayout.X_AXIS)
				for(columnInfo in dialog.elementsTableModel.columnInfos) {
					val panel = JPanel(VerticalFlowLayout(VerticalFlowLayout.TOP, 4, 2, true, false))
					when(columnInfo) {
						is ElementTableModel.NameColumn -> {
							val nameField = JTextField(item.name)
							if(item is NewPropertyDescriptor) {
								dialog.validateNameField(nameField)
								nameField.whenTextChanged { dialog.validateNameField(nameField) }
							} else {
								nameField.isEditable = false
							}
							this.nameField = nameField
							panel.add(nameField)
						}
						is ElementTableModel.SeparatorColumn -> {
							if(item is PropertyDescriptor) {
								val separatorComboBox = ComboBox(ParadoxSeparator.values())
								separatorComboBox.selectedItem = item.separator
								this.separatorComboBox = separatorComboBox
								panel.add(separatorComboBox)
							} else if(item is NewPropertyDescriptor) {
								val separatorComboBox = ComboBox(ParadoxSeparator.values())
								separatorComboBox.selectedItem = item.separator
								this.separatorComboBox = separatorComboBox
								panel.add(separatorComboBox)
							}
							panel.size = Dimension(panel.size.height * 2, panel.size.height)
						}
						is ElementTableModel.ValueColumn -> {
							if(item is PropertyDescriptor) {
								if(item.constantValues.isEmpty()) {
									val valueField = JTextField(item.value)
									valueField.isEditable = false
									this.valueField = valueField
									panel.add(valueField)
								} else {
									val valueComboBox = ComboBox(item.constantValueArray)
									valueComboBox.selectedItem = item.value
									this.valueComboBox = valueComboBox
									panel.add(valueComboBox)
								}
							} else if(item is NewPropertyDescriptor) {
								val valueField = JTextField(item.value)
								this.valueField = valueField
								panel.add(valueField)
							}
						}
						else -> {
							continue
						}
					}
					add(panel)
				}
			}
			
			override fun getValue(): JBTableRow {
				return JBTableRow { column ->
					val columnInfo = dialog.elementsTableModel.columnInfos[column]
					when(columnInfo) {
						is ElementTableModel.NameColumn -> nameField?.text
						is ElementTableModel.SeparatorColumn -> separatorComboBox?.item
						is ElementTableModel.ValueColumn -> valueField?.text ?: valueComboBox?.item
						else -> null
					}
				}
			}
			
			override fun getPreferredFocusedComponent(): JComponent? {
				return nameField
			}
			
			override fun getFocusableComponents(): Array<JComponent> {
				return listOfNotNull(nameField, separatorComboBox, valueField, valueComboBox).toTypedArray()
			}
		}
	}
	
	private fun getRowItem(row: Int): ElementDescriptor {
		return dialog.elementsTable.items.get(row)
	}
}