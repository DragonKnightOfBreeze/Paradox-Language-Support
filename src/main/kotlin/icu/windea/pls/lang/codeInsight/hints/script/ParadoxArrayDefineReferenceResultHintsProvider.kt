package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.util.evaluators.ParadoxArrayDefineReferenceEvaluator
import icu.windea.pls.lang.util.evaluators.ParadoxEvaluationService
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 通过内嵌提示显示数组定值引用的评估结果。
 *
 * @see ParadoxArrayDefineReferenceEvaluator
 */
class ParadoxArrayDefineReferenceResultHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptStringExpressionElement) return
        if (!ParadoxEvaluationService.isEvaluableForArrayDefineReference(element)) return

        val evaluator = ParadoxArrayDefineReferenceEvaluator()
        val result = runCatching { evaluator.evaluateFromRoot(element) }.getOrNull() ?: return
        val value = result.value
        sink.addInlinePresentation(element.endOffset) {
            text("=> ${value}".optimized())
        }
    }
}
