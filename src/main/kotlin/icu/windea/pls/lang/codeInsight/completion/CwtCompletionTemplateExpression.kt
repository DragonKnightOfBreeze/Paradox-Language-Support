package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Result
import com.intellij.codeInsight.template.TextResult
import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.removeSurroundingOrNull

sealed class CwtCompletionTemplateExpression(
    val context: CwtCompletionContext,
    val range: TextRange,
    val text: String,
) : Expression() {
    override fun calculateResult(context: ExpressionContext?): Result? {
        return TextResult(text)
    }

    override fun calculateLookupItems(context: ExpressionContext): Array<out LookupElement>? {
        val lookupElements = mutableListOf<LookupElement>()
        CwtCompletionManager.completeFromTemplateExpression(this, context) {
            lookupElements.add(it)
            true
        }
        return lookupElements.toArray(LookupElement.EMPTY_ARRAY)
    }

    override fun requiresCommittedPSI(): Boolean {
        return false
    }

    class Enum(context: CwtCompletionContext, range: TextRange, text: String, val name: String) : CwtCompletionTemplateExpression(context, range, text)

    class Parameter(context: CwtCompletionContext, range: TextRange, text: String, val name: String) : CwtCompletionTemplateExpression(context, range, text)

    companion object {
        @JvmStatic
        fun resolve(context: CwtCompletionContext, range: TextRange, text: String): CwtCompletionTemplateExpression? {
            run {
                val enumName = text.removeSurroundingOrNull("\$enum:", "$") ?: return@run
                return Enum(context, range, text, enumName)
            }
            run {
                val parameterName = text.removeSurroundingOrNull("$", "$") ?: return@run
                return Parameter(context, range, text, parameterName)
            }
            return null
        }
    }
}
