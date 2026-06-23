package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.util.evaluators.ParadoxDefineReferenceEvaluator
import icu.windea.pls.lang.util.evaluators.ParadoxEvaluationService
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 通过内嵌提示显示定值引用的评估结果。
 *
 * @see ParadoxDefineReferenceEvaluator
 */
class ParadoxDefineReferenceResultHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptStringExpressionElement) return
        if (!ParadoxEvaluationService.isEvaluableForDefineReference(element)) return

        val evaluator = ParadoxDefineReferenceEvaluator()
        val result = runCatching { evaluator.evaluateFromRoot(element) }.getOrNull() ?: return
        val value = result.value
        sink.addInlinePresentation(element.endOffset) {
            text("=> ${value}".optimized())
        }
    }
}
