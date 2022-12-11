package icu.windea.pls.core.ui

import com.intellij.openapi.*
import com.intellij.openapi.observable.util.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.table.*
import com.intellij.util.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import java.util.function.Supplier
import javax.swing.*

//com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase.ParametersListTable

class ElementsListTable(
	private val project: Project,
	private val elementsTable: TableView<ElementDescriptor>,
	private val elementsTableModel: ElementTableModel,
	private val disposable: Disposable
): JBListTable(elementsTable, disposable) {
	val _rowRenderer = object : EditorTextFieldJBTableRowRenderer(project, ParadoxScriptLanguage, disposable) {
		override fun getText(table: JTable, row: Int): String {
			val item = getRowItem(row)
			return when(item) {
				is PropertyDescriptor -> {
					if(item.value.isEmpty()) {
						"${item.name} ${item.separator} \"\" # ${PlsBundle.message("column.tooltip.unset")}"
					} else {
						"${item.name} ${item.separator} ${item.value}}}"
					}
				}
				is ValueDescriptor -> {
					item.name
				}
				is NewPropertyDescriptor -> {
					if(item.value.isEmpty()) {
						"${item.name} ${item.separator} \"\" # ${PlsBundle.message("column.tooltip.unset")}"
					} else {
						"${item.name} ${item.separator} ${item.value}}}"
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
				for(columnInfo in elementsTableModel.columnInfos) {
					val panel = JPanel(VerticalFlowLayout(VerticalFlowLayout.TOP, 4, 2, true, false))
					when(columnInfo) {
						is ElementTableModel.NameColumn -> {
							val nameField = JTextField(item.name)
							if(item is NewPropertyDescriptor) {
								validateNameField(nameField)
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
			
			private fun validateNameField(nameField: JTextField) {
				val validator = ComponentValidator(disposable)
				validator.withValidator(Supplier {
					if(nameField.text.isEmpty()) {
						ValidationInfo(PlsBundle.message("column.message.nameCannotBeEmpty"), nameField)
					} else {
						null
					}
				}).installOn(nameField)
				nameField.whenTextChanged { validator.revalidate() }
			}
			
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
			
			override fun getPreferredFocusedComponent(): JComponent? {
				return nameField
			}
			
			override fun getFocusableComponents(): Array<JComponent> {
				return listOfNotNull(nameField, separatorComboBox, valueField, valueComboBox).toTypedArray()
			}
		}
	}
	
	private fun getRowItem(row: Int): ElementDescriptor {
		return elementsTable.items.get(row)
	}
}