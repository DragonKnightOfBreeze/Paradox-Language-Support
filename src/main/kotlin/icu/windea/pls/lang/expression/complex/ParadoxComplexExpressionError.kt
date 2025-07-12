package icu.windea.pls.lang.expression.complex

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.*

data class ParadoxComplexExpressionError(
    val code: Int,
    val rangeInExpression: TextRange,
    val description: String,
    val highlightType: ProblemHighlightType? = null
) {
    fun isUnresolvedError() = this.code in 1..100

    fun register(element: ParadoxExpressionElement, holder: ProblemsHolder, vararg fixes: LocalQuickFix) {
        val description = description
        val highlightType = when {
            highlightType != null -> highlightType
            isUnresolvedError() -> ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
            else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        }
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        holder.registerProblem(element, description, highlightType, rangeInElement, *fixes)
    }

    object Codes {
        const val UnresolvedTemplateSnippet = 100
        const val UnresolvedScopeLink = 1
        const val UnresolvedValueField = 2
        const val UnresolvedDataSource = 3
        const val UnresolvedScriptValue = 4
        const val UnresolvedDatabaseObjectType = 5
        const val UnresolvedDatabaseObject = 6
        const val UnresolvedDefineNamespace = 7
        const val UnresolvedDefineVariable = 8
        const val UnresolvedCommandScope = 9
        const val UnresolvedCommandField = 10

        const val MalformedScopeFieldExpression = 101
        const val MalformedValueFieldExpression = 102
        const val MalformedVariableFieldExpression = 103
        const val MalformedDynamicValueExpression = 104
        const val MalformedScriptValueExpression = 105
        const val MalformedDatabaseObjectExpression = 106
        const val MalformedDefineReferenceExpression = 107
        const val MalformedCommandExpression = 108
    }

    object Builder {
        fun unresolvedTemplateSnippet(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedTemplateSnippet
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedTemplateSnippet", value, type))
        }

        fun unresolvedScopeLink(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedScopeLink
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedScopeLink", value))
        }

        fun unresolvedValueField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedValueField
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedValueField", value))
        }

        fun unresolvedDataSource(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedDataSource
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDataSource", value, type))
        }

        fun unresolvedScriptValue(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedScriptValue
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedScriptValue", value))
        }

        fun unresolvedDatabaseObjectType(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedDatabaseObjectType
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDatabaseObjectType", value))
        }

        fun unresolvedDatabaseObject(rangeInExpression: TextRange, value: String, type: String?): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedDatabaseObject
            val description = when {
                type != null -> PlsBundle.message("script.expression.unresolvedDatabaseObject", value, type)
                else -> PlsBundle.message("script.expression.unresolvedDatabaseObject.1", value)
            }
            return ParadoxComplexExpressionError(code, rangeInExpression, description)
        }

        fun unresolvedDefineNamespace(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedDefineNamespace
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDefineNamespace", value))
        }

        fun unresolvedDefineVariable(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedDefineVariable
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDefineVariable", value))
        }

        fun unresolvedCommandScope(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedCommandScope
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedCommandScope", value))
        }

        fun unresolvedCommandField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
            val code = Codes.UnresolvedCommandField
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedCommandField", value))
        }

        fun malformedScopeFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
            val code = Codes.MalformedScopeFieldExpression
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedScopeFieldExpression", text))
        }

        fun malformedValueFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
            val code = Codes.MalformedValueFieldExpression
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedValueFieldExpression", text))
        }

        fun malformedVariableFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
            val code = Codes.MalformedVariableFieldExpression
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedVariableFieldExpression", text))
        }

        fun malformedDynamicValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
            val code = Codes.MalformedDynamicValueExpression
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedDynamicValueExpression", text))
        }

        fun malformedScriptValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
            val code = Codes.MalformedScriptValueExpression
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedScriptValueExpression", text))
        }

        fun malformedDatabaseObjectExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
            val code = Codes.MalformedDatabaseObjectExpression
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedDatabaseObjectExpression", text))
        }

        fun malformedDefineReferenceExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
            val code = Codes.MalformedDefineReferenceExpression
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedDefineReferenceExpression", text))
        }

        fun malformedCommandExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
            val code = Codes.MalformedCommandExpression
            return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedCommandExpression", text))
        }
    }
}
