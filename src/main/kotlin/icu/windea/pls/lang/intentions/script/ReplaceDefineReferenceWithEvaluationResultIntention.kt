package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.evaluators.ParadoxDefineReferenceEvaluator
import icu.windea.pls.lang.util.evaluators.ParadoxEvaluationService
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 将定值引用表达式所在的表达式替换为其求值结果。
 *
 * @see ParadoxDefineReferenceEvaluator
 */
@Suppress("UnstableApiUsage")
class ReplaceDefineReferenceWithEvaluationResultIntention : PsiUpdateModCommandAction<ParadoxExpressionElement>(ParadoxExpressionElement::class.java) {
    override fun getFamilyName() = PlsBundle.message("intention.replaceDefineReferenceWithEvaluationResult")

    override fun invoke(context: ActionContext, element: ParadoxExpressionElement, updater: ModPsiUpdater) {
        val result = getResult(element) ?: return
        val newElement = ParadoxScriptElementFactory.createValue(context.project, result.text)
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxExpressionElement, context: ActionContext): Boolean {
        return getResult(element) != null
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxExpressionElement
    }

    private fun getResult(element: ParadoxExpressionElement): ParadoxScriptValue? {
        if (!ParadoxEvaluationService.isEvaluableForDefineReference(element)) return null

        val evaluator = ParadoxDefineReferenceEvaluator(resolve = false) // NOTE 2.1.10 do not resolve scripted variables here
        return evaluator.evaluate(element)
    }
}
