package icu.windea.pls.lang.actions.script

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.psi.util.PsiUtilBase
import com.intellij.psi.util.parentOfType
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.editor
import icu.windea.pls.core.findElementAt
import icu.windea.pls.lang.ui.script.ParadoxInlineMathEvaluatorDialog
import icu.windea.pls.lang.util.evaluators.ParadoxEvaluationService
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EvaluateInlineMathAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxScriptFile) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        e.presentation.isVisible = true
        if (!ParadoxEvaluationService.isEvaluableForInlineMath(element)) return
        e.presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxScriptFile) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        if (!ParadoxEvaluationService.isEvaluableForInlineMath(element)) return
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
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
