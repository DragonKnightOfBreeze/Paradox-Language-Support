package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.util.evaluators.ParadoxArrayDefineReferenceExpressionEvaluator
import icu.windea.pls.lang.util.evaluators.ParadoxEvaluationService
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 将数组定值引用表达式所在的表达式替换为其求值结果。
 *
 * @see ParadoxArrayDefineReferenceExpressionEvaluator
 */
@Suppress("UnstableApiUsage")
class ReplaceArrayDefineReferenceWithEvaluationResultIntention : PsiUpdateModCommandAction<ParadoxScriptStringExpressionElement>(ParadoxScriptStringExpressionElement::class.java) {
    override fun getFamilyName() = PlsBundle.message("intention.replaceArrayDefineReferenceWithEvaluationResult")

    override fun invoke(context: ActionContext, element: ParadoxScriptStringExpressionElement, updater: ModPsiUpdater) {
        val result = getResult(element) ?: return
        val newElement = ParadoxScriptElementFactory.createValueFromText(context.project, result.text)
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxScriptStringExpressionElement, context: ActionContext): Boolean {
        return getResult(element) != null
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptStringExpressionElement
    }

    private fun getResult(element: ParadoxScriptStringExpressionElement): ParadoxScriptValue? {
        if (!ParadoxEvaluationService.isEvaluableForArrayDefineReference(element)) return null

        val evaluator = ParadoxArrayDefineReferenceExpressionEvaluator(resolve = false) // NOTE 2.1.10 do not resolve scripted variables here
        return runCatching { evaluator.evaluate(element) }.getOrNull()
    }
}
