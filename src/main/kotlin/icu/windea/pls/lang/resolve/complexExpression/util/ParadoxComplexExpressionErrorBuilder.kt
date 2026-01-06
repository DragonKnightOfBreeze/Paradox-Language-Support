package icu.windea.pls.lang.resolve.complexExpression.util

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
    private const val CODE_UNRESOLVED_STELLARIS_NAME_PARTS_LIST = 9
    private const val CODE_UNRESOLVED_STELLARIS_NAME_FORMAT_LOCALISATION = 10
    private const val CODE_UNRESOLVED_COMMAND_SCOPE = 51
    private const val CODE_UNRESOLVED_COMMAND_FIELD = 52

    private const val CODE_MALFORMED_SCOPE_FIELD_EXPRESSION = 101
    private const val CODE_MALFORMED_VALUE_FIELD_EXPRESSION = 102
    private const val CODE_MALFORMED_VARIABLE_FIELD_EXPRESSION = 103
    private const val CODE_MALFORMED_DYNAMIC_VALUE_EXPRESSION = 104
    private const val CODE_MALFORMED_SCRIPT_VALUE_EXPRESSION = 105
    private const val CODE_MALFORMED_DATABASE_OBJECT_EXPRESSION = 106
    private const val CODE_MALFORMED_DEFINE_REFERENCE_EXPRESSION = 107
    private const val CODE_MALFORMED_STELLARIS_NAME_FORMAT_EXPRESSION = 108
    private const val CODE_MALFORMED_COMMAND_EXPRESSION = 151

    fun unresolvedTemplateSnippet(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_TEMPLATE_SNIPPET
        val description = PlsBundle.message("script.expression.unresolvedTemplateSnippet", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedScopeLink(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_SCOPE_LINK
        val description = PlsBundle.message("script.expression.unresolvedScopeLink", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedValueField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_VALUE_FIELD
        val description = PlsBundle.message("script.expression.unresolvedValueField", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDataSource(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DATA_SOURCE
        val description = PlsBundle.message("script.expression.unresolvedDataSource", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedScriptValue(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_SCRIPT_VALUE
        val description = PlsBundle.message("script.expression.unresolvedScriptValue", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDatabaseObjectType(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DATABASE_OBJECT_TYPE
        val description = PlsBundle.message("script.expression.unresolvedDatabaseObjectType", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDatabaseObject(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DATABASE_OBJECT
        val description = PlsBundle.message("script.expression.unresolvedDatabaseObject", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDefineNamespace(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DEFINE_NAMESPACE
        val description = PlsBundle.message("script.expression.unresolvedDefineNamespace", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDefineVariable(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_DEFINE_VARIABLE
        val description = PlsBundle.message("script.expression.unresolvedDefineVariable", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedStellarisNamePartsList(rangeInExpression: TextRange, value: String, definitionType: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_STELLARIS_NAME_PARTS_LIST
        val description = PlsBundle.message("script.expression.unresolvedStellarisNamePartsList", value, definitionType)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedStellarisNameFormatLocalisation(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_STELLARIS_NAME_FORMAT_LOCALISATION
        val description = PlsBundle.message("script.expression.unresolvedStellarisNameFormatLocalisation", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedCommandScope(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_COMMAND_SCOPE
        val description = PlsBundle.message("script.expression.unresolvedCommandScope", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedCommandField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = CODE_UNRESOLVED_COMMAND_FIELD
        val description = PlsBundle.message("script.expression.unresolvedCommandField", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedScopeFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_MALFORMED_SCOPE_FIELD_EXPRESSION
        val description = PlsBundle.message("script.expression.malformedScopeFieldExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedValueFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_MALFORMED_VALUE_FIELD_EXPRESSION
        val description = PlsBundle.message("script.expression.malformedValueFieldExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedVariableFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_MALFORMED_VARIABLE_FIELD_EXPRESSION
        val description = PlsBundle.message("script.expression.malformedVariableFieldExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedDynamicValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_MALFORMED_DYNAMIC_VALUE_EXPRESSION
        val description = PlsBundle.message("script.expression.malformedDynamicValueExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedScriptValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_MALFORMED_SCRIPT_VALUE_EXPRESSION
        val description = PlsBundle.message("script.expression.malformedScriptValueExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedDatabaseObjectExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_MALFORMED_DATABASE_OBJECT_EXPRESSION
        val description = PlsBundle.message("script.expression.malformedDatabaseObjectExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedDefineReferenceExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_MALFORMED_DEFINE_REFERENCE_EXPRESSION
        val description = PlsBundle.message("script.expression.malformedDefineReferenceExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedStellarisNameFormatExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_MALFORMED_STELLARIS_NAME_FORMAT_EXPRESSION
        val description = PlsBundle.message("script.expression.malformedStellarisNameFormatExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedCommandExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = CODE_MALFORMED_COMMAND_EXPRESSION
        val description = PlsBundle.message("script.expression.malformedCommandExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }
}
