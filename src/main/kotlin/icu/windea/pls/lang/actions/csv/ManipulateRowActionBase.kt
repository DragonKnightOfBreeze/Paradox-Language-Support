package icu.windea.pls.lang.actions.csv

import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.util.dataFlow.*
import icu.windea.pls.lang.util.manipulators.*
import java.util.function.*

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

    protected open fun findElements(e: AnActionEvent, file: PsiFile): ParadoxRowSequence {
        val editor = e.editor ?: return ParadoxRowSequence()
        return ParadoxCsvManipulator.buildSelectedRowSequence(editor, file)
    }

    protected open fun getTextProvider(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence): Supplier<String>? {
        return null
    }

    protected open fun isAvailable(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence): Boolean {
        return elements.any()
    }

    protected open fun isEnabled(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence): Boolean {
        return true
    }

    abstract fun doInvoke(e: AnActionEvent, file: PsiFile, elements: ParadoxRowSequence)
}
