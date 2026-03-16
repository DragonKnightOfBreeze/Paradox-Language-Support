package icu.windea.pls.lang.resolve.complexExpression.util

import com.intellij.openapi.util.TextRange
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrorCodes as Codes

object ParadoxComplexExpressionErrorBuilder {
    fun unresolvedTemplateSnippet(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_TEMPLATE_SNIPPET
        val description = PlsBundle.message("complexExpression.unresolvedTemplateSnippet", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedScopeLink(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_SCOPE_LINK
        val description = PlsBundle.message("complexExpression.unresolvedScopeLink", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedValueField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_VALUE_FIELD
        val description = PlsBundle.message("complexExpression.unresolvedValueField", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDataSource(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_DATA_SOURCE
        val description = PlsBundle.message("complexExpression.unresolvedDataSource", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedScriptValue(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_SCRIPT_VALUE
        val description = PlsBundle.message("complexExpression.unresolvedScriptValue", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDatabaseObjectType(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_DATABASE_OBJECT_TYPE
        val description = PlsBundle.message("complexExpression.unresolvedDatabaseObjectType", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDatabaseObject(rangeInExpression: TextRange, value: String, type: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_DATABASE_OBJECT
        val description = PlsBundle.message("complexExpression.unresolvedDatabaseObject", value, type)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDefineNamespace(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_DEFINE_NAMESPACE
        val description = PlsBundle.message("complexExpression.unresolvedDefineNamespace", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedDefineVariable(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_DEFINE_VARIABLE
        val description = PlsBundle.message("complexExpression.unresolvedDefineVariable", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedStellarisNamePartsList(rangeInExpression: TextRange, value: String, definitionType: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_STELLARIS_NAME_PARTS_LIST
        val description = PlsBundle.message("complexExpression.unresolvedStellarisNamePartsList", value, definitionType)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedStellarisNameFormatLocalisation(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_STELLARIS_NAME_FORMAT_LOCALISATION
        val description = PlsBundle.message("complexExpression.unresolvedStellarisNameFormatLocalisation", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedCommandScope(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_COMMAND_SCOPE
        val description = PlsBundle.message("complexExpression.unresolvedCommandScope", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun unresolvedCommandField(rangeInExpression: TextRange, value: String): ParadoxComplexExpressionError {
        val code = Codes.UNRESOLVED_COMMAND_FIELD
        val description = PlsBundle.message("complexExpression.unresolvedCommandField", value)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedScopeFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = Codes.MALFORMED_SCOPE_FIELD_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedScopeFieldExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedValueFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = Codes.MALFORMED_VALUE_FIELD_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedValueFieldExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedVariableFieldExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = Codes.MALFORMED_VARIABLE_FIELD_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedVariableFieldExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedDynamicValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = Codes.MALFORMED_DYNAMIC_VALUE_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedDynamicValueExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedScriptValueExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = Codes.MALFORMED_SCRIPT_VALUE_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedScriptValueExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedDatabaseObjectExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = Codes.MALFORMED_DATABASE_OBJECT_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedDatabaseObjectExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedDefineReferenceExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = Codes.MALFORMED_DEFINE_REFERENCE_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedDefineReferenceExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedStellarisNameFormatExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = Codes.MALFORMED_STELLARIS_NAME_FORMAT_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedStellarisNameFormatExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun malformedCommandExpression(rangeInExpression: TextRange, text: String): ParadoxComplexExpressionError {
        val code = Codes.MALFORMED_COMMAND_EXPRESSION
        val description = PlsBundle.message("complexExpression.malformedCommandExpression", text)
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }

    fun notQuotedNestedWithPrefixes(rangeInExpression: TextRange): ParadoxComplexExpressionError {
        val code = Codes.NOT_QUOTED_NESTED_LINKED
        val description = PlsBundle.message("complexExpression.notQuotedNestedWithPrefixes")
        return ParadoxComplexExpressionError(code, rangeInExpression, description)
    }
}
