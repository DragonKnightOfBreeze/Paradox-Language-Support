package icu.windea.pls.lang.resolve.complexExpression.util

import com.intellij.openapi.util.TextRange
import icu.windea.pls.PlsBundle

object ParadoxComplexExpressionErrors {
    const val UNRESOLVED_TEMPLATE_SNIPPET = 100
    const val UNRESOLVED_SCOPE_LINK = 1
    const val UNRESOLVED_VALUE_FIELD = 2
    const val UNRESOLVED_DATA_SOURCE = 3
    const val UNRESOLVED_SCRIPT_VALUE = 4
    const val UNRESOLVED_DATABASE_OBJECT_TYPE = 5
    const val UNRESOLVED_DATABASE_OBJECT = 6
    const val UNRESOLVED_DEFINE_NAMESPACE = 7
    const val UNRESOLVED_DEFINE_VARIABLE = 8
    const val UNRESOLVED_NAME_PARTS_LIST = 9
    const val UNRESOLVED_NAME_FORMAT_LOCALISATION = 10
    const val UNRESOLVED_COMMAND_SCOPE = 51
    const val UNRESOLVED_COMMAND_FIELD = 52

    const val MALFORMED_SCOPE_FIELD_EXPRESSION = 101
    const val MALFORMED_VALUE_FIELD_EXPRESSION = 102
    const val MALFORMED_VARIABLE_FIELD_EXPRESSION = 103
    const val MALFORMED_DYNAMIC_VALUE_EXPRESSION = 104
    const val MALFORMED_SCRIPT_VALUE_REFERENCE_EXPRESSION = 105
    const val MALFORMED_DEFINE_REFERENCE_EXPRESSION = 106
    const val MALFORMED_ARRAY_DEFINE_REFERENCE_EXPRESSION = 107
    const val MALFORMED_TAGS_EXPRESSION = 108
    const val MALFORMED_DATABASE_OBJECT_EXPRESSION = 109
    const val MALFORMED_NAME_FORMAT_EXPRESSION = 110
    const val MALFORMED_COMMAND_EXPRESSION = 151

    const val ARRAY_DEFINE_REFERENCE_FORM = 190
    const val ARRAY_DEFINE_REFERENCE_INDEX = 191

    const val EXPRESSION_NOT_QUOTED = 201

    fun unresolvedTemplateSnippet(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_TEMPLATE_SNIPPET
        val description = PlsBundle.message("complexExpression.unresolvedTemplateSnippet", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedScopeLink(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_SCOPE_LINK
        val description = PlsBundle.message("complexExpression.unresolvedScopeLink", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedValueField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_VALUE_FIELD
        val description = PlsBundle.message("complexExpression.unresolvedValueField", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDataSource(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_DATA_SOURCE
        val description = PlsBundle.message("complexExpression.unresolvedDataSource", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedScriptValue(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_SCRIPT_VALUE
        val description = PlsBundle.message("complexExpression.unresolvedScriptValue", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDatabaseObjectType(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_DATABASE_OBJECT_TYPE
        val description = PlsBundle.message("complexExpression.unresolvedDatabaseObjectType", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDatabaseObject(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_DATABASE_OBJECT
        val description = PlsBundle.message("complexExpression.unresolvedDatabaseObject", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDefineNamespace(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_DEFINE_NAMESPACE
        val description = PlsBundle.message("complexExpression.unresolvedDefineNamespace", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDefineVariable(rangeInExpression: TextRange, value: String, namespace: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_DEFINE_VARIABLE
        val description = PlsBundle.message("complexExpression.unresolvedDefineVariable", value, namespace)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedNamePartsList(rangeInExpression: TextRange, value: String, definitionType: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_NAME_PARTS_LIST
        val description = PlsBundle.message("complexExpression.unresolvedNamePartsList", value, definitionType)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedNameFormatLocalisation(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_NAME_FORMAT_LOCALISATION
        val description = PlsBundle.message("complexExpression.unresolvedNameFormatLocalisation", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedCommandScope(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_COMMAND_SCOPE
        val description = PlsBundle.message("complexExpression.unresolvedCommandScope", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedCommandField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = UNRESOLVED_COMMAND_FIELD
        val description = PlsBundle.message("complexExpression.unresolvedCommandField", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedScopeFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_SCOPE_FIELD_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedScopeFieldExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedValueFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_VALUE_FIELD_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedValueFieldExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedVariableFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_VARIABLE_FIELD_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedVariableFieldExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedDynamicValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_DYNAMIC_VALUE_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedDynamicValueExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedScriptValueReferenceExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_SCRIPT_VALUE_REFERENCE_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedScriptValueReferenceExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedDatabaseObjectExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_DATABASE_OBJECT_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedDatabaseObjectExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedDefineReferenceExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_DEFINE_REFERENCE_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedDefineReferenceExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedArrayDefineReferenceExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_ARRAY_DEFINE_REFERENCE_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedArrayDefineReferenceExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedTagsExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_TAGS_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedTagsExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedNameFormatExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_NAME_FORMAT_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedNameFormatExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedCommandExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = MALFORMED_COMMAND_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedCommandExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun notArrayDefine(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = ARRAY_DEFINE_REFERENCE_FORM
        val description = PlsBundle.message("complexExpression.notArrayDefine", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun indexNotInt(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = ARRAY_DEFINE_REFERENCE_INDEX
        val description = PlsBundle.message("complexExpression.indexNotInt", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun indexOutOfBounds(rangeInExpression: TextRange, index: Int, length: Int?): ParadoxComplexExpressionError {
        val code = ARRAY_DEFINE_REFERENCE_INDEX
        val description = PlsBundle.message("complexExpression.indexOutOfBounds", index, length ?: "?")
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun notQuoted(rangeInExpression: TextRange): ParadoxComplexExpressionError {
        val code = EXPRESSION_NOT_QUOTED
        val description = PlsBundle.message("complexExpression.notQuoted")
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }
}
