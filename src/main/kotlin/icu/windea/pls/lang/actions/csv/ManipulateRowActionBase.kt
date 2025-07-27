package icu.windea.pls.lang.actions.csv

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.util.manipulators.ParadoxCsvManipulator

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
        val elements = ParadoxCsvManipulator.buildSelectedRowSequence(editor, file)
        if (elements.none()) return
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is ParadoxCsvFile) return
        val editor = e.editor ?: return
        val elements = ParadoxCsvManipulator.buildSelectedRowSequence(editor, file)
        if (elements.none()) return
        val project = e.project ?: return
        doInvoke(e, project, file, elements)
    }

    abstract fun doInvoke(e: AnActionEvent, project: Project, file: PsiFile, elements: Sequence<ParadoxCsvRow>)
}
