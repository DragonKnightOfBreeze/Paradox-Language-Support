package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Result
import com.intellij.codeInsight.template.TextResult
import com.intellij.openapi.util.TextRange
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.removeSurroundingOrNull

sealed class CwtConfigCompletionTemplateExpression(
    val context: CwtConfigCompletionContext,
    val range: TextRange,
    val text: String,
) : Expression() {
    override fun calculateResult(context: ExpressionContext?): Result? {
        return TextResult(text)
    }

    override fun calculateLookupItems(context: ExpressionContext): Array<out LookupElement>? {
        val lookupElements = mutableListOf<LookupElement>()
        CwtConfigCompletionManager.completeByTemplateExpression(this, context) {
            lookupElements.add(it)
            true
        }
        return lookupElements.toArray(LookupElement.EMPTY_ARRAY)
    }

    override fun requiresCommittedPSI(): Boolean {
        return false
    }

    class Enum(context: CwtConfigCompletionContext, range: TextRange, text: String, val name: String) : CwtConfigCompletionTemplateExpression(context, range, text)

    class Parameter(context: CwtConfigCompletionContext, range: TextRange, text: String, val name: String) : CwtConfigCompletionTemplateExpression(context, range, text)

    companion object {
        @JvmStatic
        fun resolve(context: CwtConfigCompletionContext, range: TextRange, text: String): CwtConfigCompletionTemplateExpression? {
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
