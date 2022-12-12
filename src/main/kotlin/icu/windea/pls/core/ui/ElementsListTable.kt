package icu.windea.pls.core.ui

import com.intellij.openapi.*
import com.intellij.openapi.observable.util.*
import com.intellij.openapi.ui.*
import com.intellij.ui.table.*
import com.intellij.util.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import javax.swing.*

//com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase.ParametersListTable

class ElementsListTable(
	private val elementsTable: TableView<ElementDescriptor>,
	private val elementsTableModel: ElementTableModel,
	private val disposable: Disposable,
	private val context: ElementDescriptorContext,
	private val dialog: DialogWithValidation
): JBListTable(elementsTable, disposable) {
	
	val _rowRenderer = object : EditorTextFieldJBTableRowRenderer(context.project, ParadoxScriptLanguage, disposable) {
		override fun getText(table: JTable, row: Int): String {
			val item = getRowItem(row)
			return when(item) {
				is ValueDescriptor -> {
					item.name
				}
				is PropertyDescriptor -> {
					buildString { 
						append(item.name.quoteIfNecessary())
						append(" ")
						append(item.separator)
						if(item.value.isEmpty()) {
							append("\"\"").append("# ").append(PlsBundle.message("column.tooltip.editInTemplate"))
						} else {
							append(item.value.quoteIfNecessary())
						}
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
			private var nameComboBox: ComboBox<String>? = null
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
							if(item is ValueDescriptor){
								val nameComboBox = ComboBox(context.allValues)
								nameComboBox.selectedItem = item.name
								configureNameComboBox(nameComboBox)
								this.nameComboBox = nameComboBox
								panel.add(nameComboBox)
							} else if(item is PropertyDescriptor) {
								val nameComboBox = ComboBox(context.allKeys)
								nameComboBox.selectedItem = item.name
								configureNameComboBox(nameComboBox)
								this.nameComboBox = nameComboBox
								panel.add(nameComboBox)
							}
						}
						is ElementTableModel.SeparatorColumn -> {
							if(item is PropertyDescriptor) {
								val separatorComboBox = ComboBox(ParadoxSeparator.values())
								separatorComboBox.selectedItem = item.separator
								configureSeparatorComboBox(separatorComboBox)
								this.separatorComboBox = separatorComboBox
								panel.add(separatorComboBox)
							} 
						}
						is ElementTableModel.ValueColumn -> {
							if(item is PropertyDescriptor) {
								val constantValues = context.allKeyValuesMap[item.name].orEmpty()
								if(constantValues.isEmpty()) {
									val valueField = JTextField(item.value)
									valueField.isEditable = false
									this.valueField = valueField
									panel.add(valueField)
								} else {
									val valueComboBox = ComboBox(constantValues)
									valueComboBox.selectedItem = item.value
									this.valueComboBox = valueComboBox
									panel.add(valueComboBox)
								}
							}
						}
						else -> {
							continue
						}
					}
					add(panel)
				}
			}
			
			private fun configureNameComboBox(nameComboBox: ComboBox<String>) {
				val validator = {
					dialog.validate {
						when {
							nameComboBox.item.isEmpty() -> ValidationInfo(PlsBundle.message("column.validation.name.empty"), nameComboBox)
							else -> null
						}
					}
				}
				validator()
				nameComboBox.whenItemSelected { validator() }
				nameComboBox.setMinimumAndPreferredWidth(200)
				nameComboBox.maximumRowCount = 20
			}
			
			private fun configureSeparatorComboBox(separatorComboBox: ComboBox<ParadoxSeparator>) {
				separatorComboBox.setMinimumAndPreferredWidth(50)
			}
			
			override fun getValue(): JBTableRow {
				return JBTableRow { column ->
					val columnInfo = elementsTableModel.columnInfos[column]
					when(columnInfo) {
						is ElementTableModel.NameColumn -> nameComboBox?.item
						is ElementTableModel.SeparatorColumn -> separatorComboBox?.item
						is ElementTableModel.ValueColumn -> valueField?.text ?: valueComboBox?.item
						else -> null
					}
				}
			}
			
			override fun getPreferredFocusedComponent(): JComponent {
				return nameComboBox!!
			}
			
			override fun getFocusableComponents(): Array<JComponent> {
				return listOfNotNull(nameComboBox, separatorComboBox, valueField, valueComboBox).toTypedArray()
			}
		}
	}
	
	private fun getRowItem(row: Int): ElementDescriptor {
		return elementsTable.items.get(row)
	}
}