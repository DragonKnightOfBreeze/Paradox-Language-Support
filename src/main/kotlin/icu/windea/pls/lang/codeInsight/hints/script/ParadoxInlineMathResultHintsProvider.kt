package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.util.evaluators.ParadoxEvaluationService
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathExpressionEvaluator
import icu.windea.pls.script.psi.ParadoxScriptInlineMath

/**
 * 通过内嵌提示显示内联数学表达式的评估结果（如果无需提供额外的传参信息）。
 *
 * @see ParadoxInlineMathExpressionEvaluator
 */
class ParadoxInlineMathResultHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptInlineMath) return
        if (!ParadoxEvaluationService.isEvaluableForInlineMath(element)) return

        val evaluator = ParadoxInlineMathExpressionEvaluator()
        val result = evaluator.evaluateOrNull(element) ?: return
        sink.addInlinePresentation(element.endOffset) {
            text("=> ${result.formatted()}".optimized())
        }
    }
}
