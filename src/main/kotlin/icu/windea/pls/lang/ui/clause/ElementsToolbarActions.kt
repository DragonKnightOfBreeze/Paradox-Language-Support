package icu.windea.pls.lang.ui.clause

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import icu.windea.pls.PlsBundle

interface ElementsToolbarActions {
    /**
     * 复制选中的所有描述符。
     */
    class DuplicateAction(
        private val elementsList: ElementsListTable
    ) : AnAction(AllIcons.Actions.Copy) {
        init {
            templatePresentation.text = PlsBundle.message("ui.dialog.expandClauseTemplate.actions.duplicate")
            registerCustomShortcutSet(CustomShortcutSet.fromString("alt C"), null)
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) {
            val selectedIndices = elementsList.table.selectionModel.selectedIndices
            val elementsTable = elementsList.elementsTable
            for (row in selectedIndices.reversed()) {
                elementsTable.listTableModel.insertRow(row + 1, elementsTable.getRow(row).copyDescriptor())
            }
        }
    }

    /**
     * 切换到上一组描述符。（如果存在多组描述符）
     */
    class SwitchToPrevAction(
        private val elementsList: ElementsListTable
    ) : AnAction(AllIcons.General.ArrowLeft) {
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
            if (descriptorsContext.index > 0) {
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
    ) : AnAction(AllIcons.General.ArrowRight) {
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
            if (descriptorsContext.index < descriptorsContext.descriptorsInfoList.lastIndex) {
                descriptorsContext.index++
            }
            elementsList.elementsTableModel.items = descriptorsContext.descriptorsInfo.resultDescriptors
        }
    }
}
