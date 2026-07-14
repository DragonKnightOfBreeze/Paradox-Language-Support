package icu.windea.pls.lang.actions.csv

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.editor
import icu.windea.pls.core.psiFile
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.lang.manipulation.ParadoxCsvFileManipulationService

/**
 * 用于处理行的一类动作。
 *
 * 某些实现支持批量处理。
 */
abstract class ManipulateRowActionBase : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val file = e.psiFile ?: return
        if (file !is ParadoxCsvFile) return
        val elements = findElements(e, file)
        e.presentation.isVisible = isAvailable(e, file, elements)
        e.presentation.isEnabled = isEnabled(e, file, elements)
        if (!e.presentation.isVisible) return
        doUpdate(e, file, elements)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.psiFile ?: return
        if (file !is ParadoxCsvFile) return
        val elements = findElements(e, file)
        doInvoke(e, file, elements)
    }

    protected open fun findElements(e: AnActionEvent, file: PsiFile): WalkingSequence<ParadoxCsvRow> {
        val editor = e.editor ?: return WalkingSequence()
        return ParadoxCsvFileManipulationService.selectedRows(editor, file)
    }

    protected open fun isAvailable(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>): Boolean {
        return elements.any()
    }

    protected open fun isEnabled(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>): Boolean {
        return true
    }

    protected open fun doUpdate(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>) {}

    protected abstract fun doInvoke(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>)
}
