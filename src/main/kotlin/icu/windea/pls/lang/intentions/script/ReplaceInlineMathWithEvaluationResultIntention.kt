package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.math.MathResult
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.util.evaluators.ParadoxEvaluationService
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathExpressionEvaluator
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptNumberExpressionElement

/**
 * 将内联数学块替换为评估结果（如果无需提供额外的传参信息）。
 *
 * @see ParadoxInlineMathExpressionEvaluator
 */
@Suppress("UnstableApiUsage")
class ReplaceInlineMathWithEvaluationResultIntention : PsiUpdateModCommandAction<ParadoxScriptInlineMath>(ParadoxScriptInlineMath::class.java) {
    override fun getFamilyName() = ChronicleBundle.message("intention.replaceInlineMathWithEvaluationResult")

    override fun invoke(context: ActionContext, element: ParadoxScriptInlineMath, updater: ModPsiUpdater) {
        val result = getResult(element) ?: return
        val newElement = ParadoxScriptElementFactory.createValueFromText(context.project, result.formatted())
        if (newElement !is ParadoxScriptNumberExpressionElement) return // post check
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxScriptInlineMath, context: ActionContext): Boolean {
        return getResult(element) != null
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptInlineMath
    }

    private fun getResult(element: ParadoxScriptInlineMath): MathResult? {
        if (!ParadoxEvaluationService.isEvaluableForInlineMath(element)) return null

        val evaluator = ParadoxInlineMathExpressionEvaluator()
        return runCatchingCancelable { evaluator.evaluate(element) }.getOrNull()
    }
}
