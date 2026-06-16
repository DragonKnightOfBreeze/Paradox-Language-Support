package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.math.MathResult
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathEvaluator
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInt

/**
 * 将内联数学块替换为其求值结果（如果无需提供额外的传参信息）。
 */
@Suppress("UnstableApiUsage")
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
