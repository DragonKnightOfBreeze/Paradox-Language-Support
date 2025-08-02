package icu.windea.pls.lang.actions.csv

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.util.manipulators.*

/**
 * 用于处理行的一类动作。
 *
 * 某些实现兼容存在多个光标位置，或者多个光标选取范围的情况。
 */
abstract class ManipulateRowActionBase : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is ParadoxCsvFile) return
        val editor = e.editor ?: return
        val elements = findElements(editor, file)
        e.presentation.isVisible = isAvailable(e, file.project, file, elements)
        e.presentation.isEnabled = isEnabled(e, file.project, file, elements)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is ParadoxCsvFile) return
        val editor = e.editor ?: return
        val elements = findElements(editor, file)
        if (elements.none()) return
        val project = e.project ?: return
        doInvoke(e, project, file, elements)
    }

    protected open fun findElements(editor: Editor, file: ParadoxCsvFile): Sequence<ParadoxCsvRow> {
        return ParadoxCsvManipulator.buildSelectedRowSequence(editor, file)
    }

    protected open fun isAvailable(e: AnActionEvent, project: Project, file: PsiFile, elements: Sequence<ParadoxCsvRow>): Boolean {
        return elements.any()
    }

    protected open fun isEnabled(e: AnActionEvent, project: Project, file: PsiFile, elements: Sequence<ParadoxCsvRow>): Boolean {
        return true
    }

    abstract fun doInvoke(e: AnActionEvent, project: Project, file: PsiFile, elements: Sequence<ParadoxCsvRow>)
}
