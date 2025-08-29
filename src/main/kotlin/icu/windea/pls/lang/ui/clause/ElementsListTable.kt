package icu.windea.pls.lang.ui.clause

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.table.TableView
import com.intellij.util.ui.table.EditorTextFieldJBTableRowRenderer
import com.intellij.util.ui.table.JBListTable
import com.intellij.util.ui.table.JBTableRow
import com.intellij.util.ui.table.JBTableRowEditor
import com.intellij.util.ui.table.JBTableRowRenderer
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.model.ParadoxSeparatorType
import icu.windea.pls.script.ParadoxScriptLanguage
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable

//com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase.ParametersListTable

class ElementsListTable(
    val elementsTable: TableView<ElementDescriptor>,
    val elementsTableModel: ElementsTableModel,
    val disposable: Disposable,
    val context: ElementsContext
) : JBListTable(elementsTable, disposable) {
    val _rowRenderer = object : EditorTextFieldJBTableRowRenderer(context.project, ParadoxScriptLanguage, disposable) {
        override fun getText(table: JTable, row: Int): String {
            val item = getRowItem(row)
            return when (item) {
                is ValueDescriptor -> {
                    item.name
                }
                is PropertyDescriptor -> {
                    buildString {
                        append(item.name.quoteIfNecessary())
                        append(" ")
                        append(item.separator)
                        append(" ")
                        if (item.value.isEmpty()) {
                            append("\"\"").append(" # ").append(PlsBundle.message("ui.table.element.column.tooltip.editInTemplate"))
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
            private var separatorComboBox: ComboBox<ParadoxSeparatorType>? = null
            private var valueComboBox: ComboBox<String>? = null

            override fun prepareEditor(table: JTable, row: Int) {
                val item = getRowItem(row)
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                for (columnInfo in elementsTableModel.columnInfos) {
                    val panel = JPanel(VerticalFlowLayout(VerticalFlowLayout.TOP, 4, 2, true, false))
                    when (columnInfo) {
                        is ElementsTableModel.NameColumn -> {
                            if (item is ValueDescriptor) {
                                val nameComboBox = ComboBox(context.descriptorsInfo.allValues)
                                nameComboBox.selectedItem = item.name
                                configureNameComboBox(nameComboBox)
                                this.nameComboBox = nameComboBox
                                panel.add(nameComboBox)
                            } else if (item is PropertyDescriptor) {
                                val nameComboBox = ComboBox(context.descriptorsInfo.allKeys)
                                nameComboBox.selectedItem = item.name
                                configureNameComboBox(nameComboBox)
                                this.nameComboBox = nameComboBox
                                panel.add(nameComboBox)
                            }
                        }
                        is ElementsTableModel.SeparatorColumn -> {
                            if (item is PropertyDescriptor) {
                                val separatorComboBox = ComboBox(ParadoxSeparatorType.entries.toTypedArray())
                                separatorComboBox.selectedItem = item.separator
                                configureSeparatorComboBox(separatorComboBox)
                                this.separatorComboBox = separatorComboBox
                                panel.add(separatorComboBox)
                            }
                        }
                        is ElementsTableModel.ValueColumn -> {
                            if (item is PropertyDescriptor) {
                                val constantValues = context.descriptorsInfo.allKeyValuesMap[item.name].orEmpty()
                                val items = constantValues
                                val valueComboBox = ComboBox(items)
                                if (constantValues.isEmpty()) valueComboBox.isEnabled = false
                                valueComboBox.selectedItem = item.value
                                configureValueComboBox(valueComboBox)
                                this.valueComboBox = valueComboBox
                                panel.add(valueComboBox)
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
                nameComboBox.setMinimumAndPreferredWidth(240)
                nameComboBox.maximumRowCount = 20
            }

            private fun configureSeparatorComboBox(separatorComboBox: ComboBox<ParadoxSeparatorType>) {
                separatorComboBox.setMinimumAndPreferredWidth(80)
            }

            private fun configureValueComboBox(valueComboBox: ComboBox<String>) {
                valueComboBox.setMinimumAndPreferredWidth(240)
                valueComboBox.maximumRowCount = 20
            }

            override fun getValue(): JBTableRow {
                return JBTableRow { column ->
                    val columnInfo = elementsTableModel.columnInfos[column]
                    when (columnInfo) {
                        is ElementsTableModel.NameColumn -> nameComboBox?.item
                        is ElementsTableModel.SeparatorColumn -> separatorComboBox?.item
                        is ElementsTableModel.ValueColumn -> valueComboBox?.item
                        else -> null
                    }
                }
            }

            override fun getPreferredFocusedComponent(): JComponent {
                return nameComboBox!!
            }

            override fun getFocusableComponents(): Array<JComponent> {
                return listOfNotNull(nameComboBox, separatorComboBox, valueComboBox).toTypedArray()
            }
        }
    }

    private fun getRowItem(row: Int): ElementDescriptor {
        return elementsTable.items.get(row)
    }
}
