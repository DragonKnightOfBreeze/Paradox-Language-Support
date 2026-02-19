@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.Presentation
import com.intellij.modcommand.PsiBasedModCommandAction
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.application.EDT
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.ui.evaluators.ParadoxInlineMathEvaluatorDialog
import icu.windea.pls.lang.util.evaluators.MathResult
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathEvaluator
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 求值内联数学表达式。
 */
class EvaluateInlineMathIntention : PsiBasedModCommandAction<ParadoxScriptInlineMath>(ParadoxScriptInlineMath::class.java) {
    override fun getFamilyName() = PlsBundle.message("intention.evaluateInlineMath")

    override fun getPresentation(context: ActionContext, element: ParadoxScriptInlineMath): Presentation {
        return Presentation.of(familyName).withPriority(PriorityAction.Priority.HIGH)
    }

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

    override fun isElementApplicable(element: ParadoxScriptInlineMath, context: ActionContext): Boolean {
        return element.expression.isNotEmpty()
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptInlineMath
    }
}

/**
 * 将内联数学块替换为其表达式的求值结果（如果无需提供额外的传参信息）。
 */
class ReplaceInlineMathWithEvaluatedValueIntention : PsiUpdateModCommandAction<ParadoxScriptInlineMath>(ParadoxScriptInlineMath::class.java) {
    override fun getFamilyName() = PlsBundle.message("intention.replaceInlineMathWithEvaluatedValue")

    override fun invoke(context: ActionContext, element: ParadoxScriptInlineMath, updater: ModPsiUpdater) {
        if (element.expression.isEmpty()) return
        val result = getResult(element) ?: return
        val newElement = ParadoxScriptElementFactory.createValue(context.project, result.formatted())
        if (newElement !is ParadoxScriptInt && newElement !is ParadoxScriptFloat) return // post check
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxScriptInlineMath, context: ActionContext): Boolean {
        return element.expression.isNotEmpty() && getResult(element) != null
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptInlineMath
    }

    private fun getResult(element: ParadoxScriptInlineMath): MathResult? {
        val evaluator = ParadoxInlineMathEvaluator()
        return runCatchingCancelable { evaluator.evaluate(element) }.getOrNull()
    }
}
