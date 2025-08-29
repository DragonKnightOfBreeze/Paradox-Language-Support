package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.Result
import com.intellij.codeInsight.template.TextResult
import com.intellij.openapi.util.TextRange
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.core.removeSurroundingOrNull

sealed class CwtConfigTemplateExpression(
    val context: ProcessingContext,
    val schemaExpression: CwtSchemaExpression,
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
        return lookupElements.toTypedArray()
    }

    override fun requiresCommittedPSI(): Boolean {
        return false
    }

    class Enum(
        context: ProcessingContext,
        schemaExpression: CwtSchemaExpression,
        range: TextRange,
        text: String,
        val name: String
    ) : CwtConfigTemplateExpression(context, schemaExpression, range, text)

    class Parameter(
        context: ProcessingContext,
        schemaExpression: CwtSchemaExpression,
        range: TextRange,
        text: String,
        val name: String
    ) : CwtConfigTemplateExpression(context, schemaExpression, range, text)

    companion object Resolver {
        fun resolve(context: ProcessingContext, schemaExpression: CwtSchemaExpression, range: TextRange, text: String): CwtConfigTemplateExpression? {
            run {
                val enumName = text.removeSurroundingOrNull("\$enum:", "$") ?: return@run
                return Enum(context, schemaExpression, range, text, enumName)
            }
            run {
                val parameterName = text.removeSurroundingOrNull("$", "$") ?: return@run
                return Parameter(context, schemaExpression, range, text, parameterName)
            }
            return null
        }
    }
}
