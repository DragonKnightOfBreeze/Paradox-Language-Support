package icu.windea.pls.model.expression.complex

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*

data class ParadoxComplexExpressionError(
    val code: Int,
    val rangeInExpression: TextRange,
    val description: String,
    val highlightType: ProblemHighlightType? = null
)

object ParadoxComplexExpressionErrorCodes {
    const val MalformedScopeFieldExpression = 100
    const val MalformedValueFieldExpression = 101
    const val MalformedVariableFieldExpression = 102
    const val MalformedDynamicValueExpression = 103
    const val MalformedScriptValueExpression = 104
    const val MalformedGameObjectExpression = 105
    const val MalformedLocalisationCommandExpression = 106
    
    const val UnresolvedScopeLink = 200
    const val UnresolvedValueField = 201
    const val UnresolvedDataSource = 202
    const val UnresolvedScriptValue = 203
    const val UnresolvedDatabaseObjectType = 204
    const val UnresolvedDatabaseObject = 205
    const val UnresolvedCommandScope = 206
    const val UnresolvedCommandField = 207
    
    const val MissingScopeLink = 300
    const val MissingValueField = 301
    const val MissingVariable = 302
    const val MissingScopeLinkValue = 303
    const val MissingValueFieldValue = 304
    const val MissingScopeFieldExpression = 305
    const val MissingParameterValue = 306
}

fun ParadoxComplexExpressionError.isMalformedError() = this.code in 100..199

fun ParadoxComplexExpressionError.isUnresolvedError() = this.code in 200..299

fun ParadoxComplexExpressionError.isMissingError() = this.code in 300..399

fun ProblemsHolder.registerExpressionError(error: ParadoxComplexExpressionError, element: ParadoxExpressionElement) {
    val description = error.description
    val highlightType = when {
        error.highlightType != null -> error.highlightType
        error.isUnresolvedError() -> ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
        else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
    }
    val rangeInElement = error.rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
    registerProblem(element, description, highlightType, rangeInElement)
}

object ParadoxComplexExpressionErrors {
    //malformed
    
    fun malformedScopeFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MalformedScopeFieldExpression
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedScopeFieldExpression", text))
    }
    
    fun malformedValueFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MalformedValueFieldExpression
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedValueFieldExpression", text))
    }
    
    fun malformedVariableFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MalformedVariableFieldExpression
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedVariableFieldExpression", text))
    }
    
    fun malformedDynamicValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MalformedDynamicValueExpression
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedDynamicValueExpression", text))
    }
    
    fun malformedScriptValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MalformedScriptValueExpression
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedScriptValueExpression", text))
    }
    
    fun malformedGameObjectExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MalformedGameObjectExpression
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedGameObjectExpression", text))
    }
    
    fun malformedLocalisationCommandExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MalformedLocalisationCommandExpression
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedLocalisationCommandExpression", text))
    }
    
    //unresolved
    
    fun unresolvedScopeLink(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.UnresolvedScopeLink
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedScopeLink", value))
    }
    
    fun unresolvedValueField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.UnresolvedValueField
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedValueField", value))
    }
    
    fun unresolvedDataSource(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.UnresolvedDataSource
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDataSource", value, type))
    }
    
    fun unresolvedScriptValue(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.UnresolvedScriptValue
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedScriptValue", value))
    }
    
    fun unresolvedDatabaseObjectType(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.UnresolvedDatabaseObjectType
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDatabaseObjectType", value))
    }
    
    fun unresolvedDatabaseObject(rangeInExpression: TextRange, value: String, type: String?): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.UnresolvedDatabaseObject
        val description = when {
            type != null -> PlsBundle.message("script.expression.unresolvedDatabaseObject", value, type)
            else -> PlsBundle.message("script.expression.unresolvedDatabaseObject.1", value)
        }
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }
    
    fun unresolvedCommandScope(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.UnresolvedCommandScope
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedCommandScope", value))
    }
    
    fun unresolvedCommandField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.UnresolvedCommandField
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedCommandField", value))
    }
    
    //missing
    
    fun missingScopeLink(rangeInExpression: TextRange): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MissingScopeLink
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.missingScopeLink"))
    }
    
    fun missingValueField(rangeInExpression: TextRange): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MissingValueField
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.missingValueField"))
    }
    
    fun missingVariable(rangeInExpression: TextRange): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MissingVariable
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.missingVariable"))
    }
    
    fun missingScopeLinkValue(rangeInExpression: TextRange, type: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MissingScopeLinkValue
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.missingScopeLinkValue", type))
    }
    
    fun missingValueFieldValue(rangeInExpression: TextRange, type: String): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MissingValueFieldValue
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.missingValueFieldValue", type))
    }
    
    fun missingScopeFieldExpression(rangeInExpression: TextRange): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MissingScopeFieldExpression
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.missingScopeFieldExpression"))
    }
    
    fun missingParameterValue(rangeInExpression: TextRange): ParadoxComplexExpressionError {
        val code = ParadoxComplexExpressionErrorCodes.MissingParameterValue
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.missingParameterValue"))
    }
}
