package icu.windea.pls.lang.actions.csv

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiFile
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.psi.select.ParadoxPsiSequenceBuilder
import java.util.function.Supplier

/**
 * 用于处理行的一类动作。
 *
 * 某些实现支持批量处理。
 */
abstract class ManipulateRowActionBase : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is ParadoxCsvFile) return
        val elements = findElements(e, file)
        getTextProvider(e, file, elements)?.let { e.presentation.setText(it) }
        e.presentation.isVisible = isAvailable(e, file, elements)
        e.presentation.isEnabled = isEnabled(e, file, elements)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is ParadoxCsvFile) return
        val elements = findElements(e, file)
        doInvoke(e, file, elements)
    }

    protected open fun findElements(e: AnActionEvent, file: PsiFile): WalkingSequence<ParadoxCsvRow> {
        val editor = e.editor ?: return WalkingSequence()
        return ParadoxPsiSequenceBuilder.selectedRows(editor, file)
    }

    protected open fun getTextProvider(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>): Supplier<String>? {
        return null
    }

    protected open fun isAvailable(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>): Boolean {
        return elements.any()
    }

    protected open fun isEnabled(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>): Boolean {
        return true
    }

    abstract fun doInvoke(e: AnActionEvent, file: PsiFile, elements: WalkingSequence<ParadoxCsvRow>)
}
