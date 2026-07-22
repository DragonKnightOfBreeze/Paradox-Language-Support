package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.letIf
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.psi.formattedValue
import icu.windea.pls.lang.psi.values
import icu.windea.pls.lang.util.evaluators.ParadoxDefineReferenceExpressionEvaluator
import icu.windea.pls.lang.util.evaluators.ParadoxEvaluationService
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue


/**
 * 通过内嵌提示显示定值引用的评估结果。
 *
 * @see ParadoxDefineReferenceExpressionEvaluator
 */
class ParadoxDefineReferenceResultHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptStringExpressionElement) return
        if (!ParadoxEvaluationService.isEvaluableForDefineReference(element)) return

        val evaluator = ParadoxDefineReferenceExpressionEvaluator()
        val result = runCatching { evaluator.evaluate(element) }.getOrNull() ?: return
        val value = formatValue(result) ?: return
        sink.addInlinePresentation(element.endOffset) {
            text("=> $value".optimized())
        }
    }

    private fun formatValue(element: ParadoxScriptValue): String? {
        return when (element) {
            is ParadoxScriptBlock -> formatArrayValue(element)
            else -> element.formattedValue()
        }
    }

    private fun formatArrayValue(element: ParadoxScriptBlock): String? {
        val settings = ParadoxDeclarativeHintsSettings.getInstance(element.project)
        if (!settings.showArrayValueForDefines) return null
        val limit = settings.truncateArrayValueForDefines
        val values = element.values().letIf(limit >= 0) { it.take(limit) }.toList()
        if (values.isEmpty()) return "{}"
        return values.joinToString(" ", "{ ", " }") { it.formattedValue().or.unresolved() }
    }
}
