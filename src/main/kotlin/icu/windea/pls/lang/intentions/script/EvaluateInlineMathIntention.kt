@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.Presentation
import com.intellij.modcommand.PsiBasedModCommandAction
import com.intellij.openapi.application.EDT
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.ui.script.ParadoxInlineMathEvaluatorDialog
import icu.windea.pls.lang.util.evaluators.ParadoxEvaluationService
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathEvaluator
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 求值内联数学表达式。
 *
 * @see ParadoxInlineMathEvaluator
 */
class EvaluateInlineMathIntention : PsiBasedModCommandAction<ParadoxScriptInlineMath>(ParadoxScriptInlineMath::class.java) {
    override fun getFamilyName() = PlsBundle.message("intention.evaluateInlineMath")

    override fun getPresentation(context: ActionContext, element: ParadoxScriptInlineMath): Presentation {
        return Presentation.of(familyName).withPriority(PriorityAction.Priority.HIGH)
    }

    override fun perform(context: ActionContext, element: ParadoxScriptInlineMath): ModCommand {
        if (!ParadoxEvaluationService.isEvaluableForInlineMath(element)) return ModCommand.nop()
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
        return IntentionPreviewInfo.Html(PlsBundle.message("intention.evaluateInlineMath.preview"))
    }

    override fun isElementApplicable(element: ParadoxScriptInlineMath, context: ActionContext): Boolean {
        return ParadoxEvaluationService.isEvaluableForInlineMath(element)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptInlineMath
    }
}
