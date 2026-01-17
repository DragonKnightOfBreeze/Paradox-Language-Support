package icu.windea.pls.lang.actions.script

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.psi.util.PsiUtilBase
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.findElementAt
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.ui.evaluators.ParadoxInlineMathEvaluatorDialog
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EvaluateInlineMathAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxScriptFile) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        presentation.isVisible = true
        if (element.expression.isEmpty()) return
        presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxScriptFile) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        if (element.expression.isEmpty()) return
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            withContext(Dispatchers.EDT) {
                val dialog = ParadoxInlineMathEvaluatorDialog(project, element)
                dialog.show()
            }
        }
    }

    private fun findElement(file: ParadoxScriptFile, offset: Int): ParadoxScriptInlineMath? {
        return file.findElementAt(offset) { it.parentOfType<ParadoxScriptInlineMath>() }
    }
}
