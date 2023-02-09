package icu.windea.pls.core.ui

import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ui.*
import com.intellij.ui.table.*
import com.intellij.util.ui.table.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import javax.swing.*

//com.intellij.refactoring.changeSignature.ChangeSignatureDialogBase.ParametersListTable

class ElementsListTable(
    val elementsTable: TableView<ElementDescriptor>,
    val elementsTableModel: ElementTableModel,
    val disposable: Disposable,
    val context: ElementDescriptorsContext,
    val dialog: DialogWithValidation
) : JBListTable(elementsTable, disposable) {
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
                        append(" ")
                        if(item.value.isEmpty()) {
                            append("\"\"").append(" # ").append(PlsBundle.message("column.tooltip.editInTemplate"))
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
            private var valueComboBox: ComboBox<String>? = null
            
            override fun prepareEditor(table: JTable, row: Int) {
                val item = getRowItem(row)
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                for(columnInfo in elementsTableModel.columnInfos) {
                    val panel = JPanel(VerticalFlowLayout(VerticalFlowLayout.TOP, 4, 2, true, false))
                    when(columnInfo) {
                        is ElementTableModel.NameColumn -> {
                            if(item is ValueDescriptor) {
                                val nameComboBox = ComboBox(context.descriptorsInfo.allValues)
                                nameComboBox.selectedItem = item.name
                                configureNameComboBox(nameComboBox)
                                this.nameComboBox = nameComboBox
                                panel.add(nameComboBox)
                            } else if(item is PropertyDescriptor) {
                                val nameComboBox = ComboBox(context.descriptorsInfo.allKeys)
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
                                val constantValues = context.descriptorsInfo.allKeyValuesMap[item.name].orEmpty()
                                val items = constantValues.ifEmpty { arrayOf("") }
                                val valueComboBox = ComboBox(items)
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
                nameComboBox.setMinimumAndPreferredWidth(200)
                nameComboBox.maximumRowCount = 20
            }
            
            private fun configureSeparatorComboBox(separatorComboBox: ComboBox<ParadoxSeparator>) {
                separatorComboBox.setMinimumAndPreferredWidth(60)
            }
            
            private fun configureValueComboBox(valueComboBox: ComboBox<String>) {
                valueComboBox.setMinimumAndPreferredWidth(200)
                valueComboBox.maximumRowCount = 20
            }
            
            override fun getValue(): JBTableRow {
                return JBTableRow { column ->
                    val columnInfo = elementsTableModel.columnInfos[column]
                    when(columnInfo) {
                        is ElementTableModel.NameColumn -> nameComboBox?.item
                        is ElementTableModel.SeparatorColumn -> separatorComboBox?.item
                        is ElementTableModel.ValueColumn -> valueComboBox?.item
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
    
    /**
     * 复制选中的所有描述符。
     */
    class DuplicateAction(
        private val elementsList: ElementsListTable
    ) : AnAction(PlsBundle.message("ui.dialog.expandClauseTemplate.actions.duplicate"), null, PlsIcons.Actions.SwitchToPrevDescriptor) {
        init {
            shortcutSet = CustomShortcutSet.fromString("alt C")
        }
        
        override fun getActionUpdateThread() = ActionUpdateThread.EDT
        
        override fun actionPerformed(e: AnActionEvent) {
            val selectedIndices = elementsList.table.selectionModel.selectedIndices
            val elementsTable = elementsList.elementsTable
            for(row in selectedIndices.reversed()) {
                elementsTable.listTableModel.insertRow(row + 1, elementsTable.getRow(row).copyDescriptor())
            }
        }
    }
    
    
    /**
     * 切换到上一组描述符。（如果存在多组描述符）
     */
    class SwitchToPrevAction(
        private val elementsList: ElementsListTable
    ) : AnAction(PlsBundle.message("ui.dialog.expandClauseTemplate.actions.switchToPrev"), null, PlsIcons.Actions.SwitchToPrevDescriptor) {
        init {
            shortcutSet = CustomShortcutSet.fromString("alt P")
        }
        
        override fun getActionUpdateThread() = ActionUpdateThread.EDT
        
        override fun update(e: AnActionEvent) {
            val descriptorsContext = elementsList.context
            e.presentation.isEnabled = descriptorsContext.index > 0
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val descriptorsContext = elementsList.context
            if(descriptorsContext.index > 0) {
                descriptorsContext.index--
            }
        }
    }
    
    /**
     * 切换到下一组描述符。（如果存在多组描述符）
     */
    class SwitchToNextAction(
        private val elementsList: ElementsListTable
    ) : AnAction(PlsBundle.message("ui.dialog.expandClauseTemplate.actions.switchToNext"), null, PlsIcons.Actions.SwitchToNextDescriptor) {
        init {
            shortcutSet = CustomShortcutSet.fromString("alt N")
        }
        
        override fun getActionUpdateThread() = ActionUpdateThread.EDT
        
        override fun update(e: AnActionEvent) {
            val descriptorsContext = elementsList.context
            e.presentation.isEnabled = descriptorsContext.index < descriptorsContext.descriptorsInfoList.lastIndex
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val descriptorsContext = elementsList.context
            if(descriptorsContext.index < descriptorsContext.descriptorsInfoList.lastIndex) {
                descriptorsContext.index++
            }
        }
    }
}