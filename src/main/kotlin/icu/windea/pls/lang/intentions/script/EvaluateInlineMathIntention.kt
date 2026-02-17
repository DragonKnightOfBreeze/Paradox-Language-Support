package icu.windea.pls.lang.intentions.script

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.PsiBasedModCommandAction
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.ui.evaluators.ParadoxInlineMathEvaluatorDialog
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 求职内联数学表达式。
 */
class EvaluateInlineMathIntention : PsiBasedModCommandAction<ParadoxScriptInlineMath>(ParadoxScriptInlineMath::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.evaluateInlineMath")

    override fun perform(context: ActionContext, element: ParadoxScriptInlineMath): ModCommand {
        if (element.expression.isEmpty()) return ModCommand.nop()
        val project = context.project
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            withContext(Dispatchers.EDT) {
                val dialog = ParadoxInlineMathEvaluatorDialog(project, element)
                dialog.show()
            }
        }
        return ModCommand.nop()
    }

    override fun generatePreview(context: ActionContext?, element: ParadoxScriptInlineMath?): IntentionPreviewInfo {
        return IntentionPreviewInfo.Html(PlsBundle.message("intention.evaluateInlineMath.desc"))
    }
}
