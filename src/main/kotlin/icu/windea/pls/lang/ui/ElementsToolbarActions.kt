package icu.windea.pls.lang.ui

import com.intellij.openapi.actionSystem.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

interface ElementsToolbarActions {
    /**
     * 复制选中的所有描述符。
     */
    class DuplicateAction(
        private val elementsList: ElementsListTable
    ) : AnAction(PlsIcons.Actions.DuplicateDescriptor) {
        init {
            templatePresentation.text = PlsBundle.message("ui.dialog.expandClauseTemplate.actions.duplicate")
            registerCustomShortcutSet(CustomShortcutSet.fromString("alt C"), null)
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
    ) : AnAction(PlsIcons.Actions.SwitchToPrevDescriptor) {
        init {
            templatePresentation.text = PlsBundle.message("ui.dialog.expandClauseTemplate.actions.switchToPrev")
            registerCustomShortcutSet(CustomShortcutSet.fromString("alt P"), null)
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
            elementsList.elementsTableModel.items = descriptorsContext.descriptorsInfo.resultDescriptors
        }
    }
    
    /**
     * 切换到下一组描述符。（如果存在多组描述符）
     */
    class SwitchToNextAction(
        private val elementsList: ElementsListTable
    ) : AnAction(PlsIcons.Actions.SwitchToNextDescriptor) {
        init {
            templatePresentation.text = PlsBundle.message("ui.dialog.expandClauseTemplate.actions.switchToNext")
            registerCustomShortcutSet(CustomShortcutSet.fromString("alt N"), null)
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
            elementsList.elementsTableModel.items = descriptorsContext.descriptorsInfo.resultDescriptors
        }
    }
}