package icu.windea.pls.lang.expression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.PlsBundle

object ParadoxComplexExpressionErrorBuilder {
    private const val CODE_UNRESOLVED_TEMPLATE_SNIPPET = 100
    private const val CODE_UNRESOLVED_SCOPE_LINK = 1
    private const val CODE_UNRESOLVED_VALUE_FIELD = 2
    private const val CODE_UNRESOLVED_DATA_SOURCE = 3
    private const val CODE_UNRESOLVED_SCRIPT_VALUE = 4
    private const val CODE_UNRESOLVED_DATABASE_OBJECT_TYPE = 5
    private const val CODE_UNRESOLVED_DATABASE_OBJECT = 6
    private const val CODE_UNRESOLVED_DEFINE_NAMESPACE = 7
    private const val CODE_UNRESOLVED_DEFINE_VARIABLE = 8
    private const val CODE_UNRESOLVED_COMMAND_SCOPE = 9
    private const val CODE_UNRESOLVED_COMMAND_FIELD = 10

    private const val CODE_UNRESOLVED_SCOPE_FIELD_EXPRESSION = 101
    private const val CODE_UNRESOLVED_VALUE_FIELD_EXPRESSION = 102
    private const val CODE_UNRESOLVED_VARIABLE_FIELD_EXPRESSION = 103
    private const val CODE_UNRESOLVED_DYNAMIC_VALUE_EXPRESSION = 104
    private const val CODE_UNRESOLVED_SCRIPT_VALUE_EXPRESSION = 105
    private const val CODE_UNRESOLVED_DATABASE_OBJECT_EXPRESSION = 106
    private const val CODE_UNRESOLVED_DEFINE_REFERENCE_EXPRESSION = 107
    private const val CODE_UNRESOLVED_COMMAND_EXPRESSION = 108

    fun unresolvedTemplateSnippet(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_TEMPLATE_SNIPPET
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedTemplateSnippet", value, type))
    }

    fun unresolvedScopeLink(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_SCOPE_LINK
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedScopeLink", value))
    }

    fun unresolvedValueField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_VALUE_FIELD
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedValueField", value))
    }

    fun unresolvedDataSource(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DATA_SOURCE
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDataSource", value, type))
    }

    fun unresolvedScriptValue(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_SCRIPT_VALUE
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedScriptValue", value))
    }

    fun unresolvedDatabaseObjectType(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DATABASE_OBJECT_TYPE
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDatabaseObjectType", value))
    }

    fun unresolvedDatabaseObject(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DATABASE_OBJECT
        val description = PlsBundle.message("script.expression.unresolvedDatabaseObject", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDefineNamespace(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DEFINE_NAMESPACE
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDefineNamespace", value))
    }

    fun unresolvedDefineVariable(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DEFINE_VARIABLE
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedDefineVariable", value))
    }

    fun unresolvedCommandScope(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_COMMAND_SCOPE
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedCommandScope", value))
    }

    fun unresolvedCommandField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_COMMAND_FIELD
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.unresolvedCommandField", value))
    }

    fun malformedScopeFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_SCOPE_FIELD_EXPRESSION
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedScopeFieldExpression", text))
    }

    fun malformedValueFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_VALUE_FIELD_EXPRESSION
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedValueFieldExpression", text))
    }

    fun malformedVariableFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_VARIABLE_FIELD_EXPRESSION
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedVariableFieldExpression", text))
    }

    fun malformedDynamicValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DYNAMIC_VALUE_EXPRESSION
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedDynamicValueExpression", text))
    }

    fun malformedScriptValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_SCRIPT_VALUE_EXPRESSION
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedScriptValueExpression", text))
    }

    fun malformedDatabaseObjectExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DATABASE_OBJECT_EXPRESSION
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedDatabaseObjectExpression", text))
    }

    fun malformedDefineReferenceExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DEFINE_REFERENCE_EXPRESSION
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedDefineReferenceExpression", text))
    }

    fun malformedCommandExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_COMMAND_EXPRESSION
        return ParadoxComplexExpressionError(code, rangeInExpression, PlsBundle.message("script.expression.malformedCommandExpression", text))
    }
}
