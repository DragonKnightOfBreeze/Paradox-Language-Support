package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.model.constants.PlsPreviewTexts
import icu.windea.pls.script.ParadoxScriptLanguage

class ParadoxScriptColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(PlsBundle.message("script.displayName.braces"), ParadoxScriptAttributesKeys.BRACES),
        AttributesDescriptor(PlsBundle.message("script.displayName.operator"), ParadoxScriptAttributesKeys.OPERATOR),
        AttributesDescriptor(PlsBundle.message("script.displayName.marker"), ParadoxScriptAttributesKeys.MARKER),
        AttributesDescriptor(PlsBundle.message("script.displayName.parameterConditionBrackets"), ParadoxScriptAttributesKeys.PARAMETER_CONDITION_BRACKETS),
        AttributesDescriptor(PlsBundle.message("script.displayName.parameterConditionExpressionBrackets"), ParadoxScriptAttributesKeys.PARAMETER_CONDITION_EXPRESSION_BRACKETS),
        AttributesDescriptor(PlsBundle.message("script.displayName.inlineMathBrackets"), ParadoxScriptAttributesKeys.INLINE_MATH_BRACKETS),
        AttributesDescriptor(PlsBundle.message("script.displayName.inlineMathOperators"), ParadoxScriptAttributesKeys.INLINE_MATH_OPERATOR),
        AttributesDescriptor(PlsBundle.message("script.displayName.comment"), ParadoxScriptAttributesKeys.COMMENT),
        AttributesDescriptor(PlsBundle.message("script.displayName.keyword"), ParadoxScriptAttributesKeys.KEYWORD),
        AttributesDescriptor(PlsBundle.message("script.displayName.atSign"), ParadoxScriptAttributesKeys.AT_SIGN),
        AttributesDescriptor(PlsBundle.message("script.displayName.scriptedVariableName"), ParadoxScriptAttributesKeys.SCRIPTED_VARIABLE_NAME),
        AttributesDescriptor(PlsBundle.message("script.displayName.scriptedVariableReference"), ParadoxScriptAttributesKeys.SCRIPTED_VARIABLE_REFERENCE),
        AttributesDescriptor(PlsBundle.message("script.displayName.parameter"), ParadoxScriptAttributesKeys.PARAMETER),
        AttributesDescriptor(PlsBundle.message("script.displayName.conditionParameter"), ParadoxScriptAttributesKeys.CONDITION_PARAMETER),
        AttributesDescriptor(PlsBundle.message("script.displayName.argument"), ParadoxScriptAttributesKeys.ARGUMENT),
        AttributesDescriptor(PlsBundle.message("script.displayName.propertyKey"), ParadoxScriptAttributesKeys.PROPERTY_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.number"), ParadoxScriptAttributesKeys.NUMBER),
        AttributesDescriptor(PlsBundle.message("script.displayName.string"), ParadoxScriptAttributesKeys.STRING),
        AttributesDescriptor(PlsBundle.message("script.displayName.color"), ParadoxScriptAttributesKeys.COLOR),
        AttributesDescriptor(PlsBundle.message("script.displayName.validEscape"), ParadoxScriptAttributesKeys.VALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("script.displayName.invalidEscape"), ParadoxScriptAttributesKeys.INVALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("script.displayName.badCharacter"), ParadoxScriptAttributesKeys.BAD_CHARACTER),

        AttributesDescriptor(PlsBundle.message("script.displayName.definition"), ParadoxScriptAttributesKeys.DEFINITION),
        AttributesDescriptor(PlsBundle.message("script.displayName.definitionName"), ParadoxScriptAttributesKeys.DEFINITION_NAME),
        AttributesDescriptor(PlsBundle.message("script.displayName.definitionReference"), ParadoxScriptAttributesKeys.DEFINITION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("script.displayName.localisationReference"), ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("script.displayName.syncedLocalisationReference"), ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("script.displayName.defineNamespace"), ParadoxScriptAttributesKeys.DEFINE_NAMESPACE),
        AttributesDescriptor(PlsBundle.message("script.displayName.defineVariable"), ParadoxScriptAttributesKeys.DEFINE_VARIABLE),
        AttributesDescriptor(PlsBundle.message("script.displayName.enumValue"), ParadoxScriptAttributesKeys.ENUM_VALUE),
        AttributesDescriptor(PlsBundle.message("script.displayName.complexEnumValue"), ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE),
        AttributesDescriptor(PlsBundle.message("script.displayName.dynamicValue"), ParadoxScriptAttributesKeys.DYNAMIC_VALUE),
        AttributesDescriptor(PlsBundle.message("script.displayName.variable"), ParadoxScriptAttributesKeys.VARIABLE),
        AttributesDescriptor(PlsBundle.message("script.displayName.modifier"), ParadoxScriptAttributesKeys.MODIFIER),
        AttributesDescriptor(PlsBundle.message("script.displayName.trigger"), ParadoxScriptAttributesKeys.TRIGGER),
        AttributesDescriptor(PlsBundle.message("script.displayName.effect"), ParadoxScriptAttributesKeys.EFFECT),
        AttributesDescriptor(PlsBundle.message("script.displayName.systemScope"), ParadoxScriptAttributesKeys.SYSTEM_SCOPE),
        AttributesDescriptor(PlsBundle.message("script.displayName.scope"), ParadoxScriptAttributesKeys.SCOPE),
        AttributesDescriptor(PlsBundle.message("script.displayName.scopePrefix"), ParadoxScriptAttributesKeys.SCOPE_PREFIX),
        AttributesDescriptor(PlsBundle.message("script.displayName.valueField"), ParadoxScriptAttributesKeys.VALUE_FIELD),
        AttributesDescriptor(PlsBundle.message("script.displayName.valueFieldPrefix"), ParadoxScriptAttributesKeys.VALUE_FIELD_PREFIX),
        AttributesDescriptor(PlsBundle.message("script.displayName.systemCommandScope"), ParadoxScriptAttributesKeys.SYSTEM_COMMAND_SCOPE),
        AttributesDescriptor(PlsBundle.message("script.displayName.commandScope"), ParadoxScriptAttributesKeys.COMMAND_SCOPE),
        AttributesDescriptor(PlsBundle.message("script.displayName.commandScopePrefix"), ParadoxScriptAttributesKeys.COMMAND_SCOPE_PREFIX),
        AttributesDescriptor(PlsBundle.message("script.displayName.commandField"), ParadoxScriptAttributesKeys.COMMAND_FIELD),
        AttributesDescriptor(PlsBundle.message("script.displayName.commandFieldPrefix"), ParadoxScriptAttributesKeys.COMMAND_FIELD_PREFIX),
        AttributesDescriptor(PlsBundle.message("script.displayName.databaseObjectType"), ParadoxScriptAttributesKeys.DATABASE_OBJECT_TYPE),
        AttributesDescriptor(PlsBundle.message("script.displayName.databaseObject"), ParadoxScriptAttributesKeys.DATABASE_OBJECT),

        AttributesDescriptor(PlsBundle.message("script.displayName.tag"), ParadoxScriptAttributesKeys.TAG),
        AttributesDescriptor(PlsBundle.message("script.displayName.tag.typeKeyPrefix"), ParadoxScriptAttributesKeys.TAG_TYPE_KEY_PREFIX),

        AttributesDescriptor(PlsBundle.message("script.displayName.directive"), ParadoxScriptAttributesKeys.DIRECTIVE),
        AttributesDescriptor(PlsBundle.message("script.displayName.directive.inlineScript"), ParadoxScriptAttributesKeys.DIRECTIVE_INLINE_SCRIPT),
        AttributesDescriptor(PlsBundle.message("script.displayName.directive.definitionInjection"), ParadoxScriptAttributesKeys.DIRECTIVE_DEFINITION_INJECTION),

        AttributesDescriptor(PlsBundle.message("script.displayName.pathReference"), ParadoxScriptAttributesKeys.PATH_REFERENCE),
    )

    private val _tagToDescriptorMap = mapOf(
        "MARKER" to ParadoxScriptAttributesKeys.MARKER,
        "OPERATOR" to ParadoxScriptAttributesKeys.OPERATOR,
        "ARGUMENT" to ParadoxScriptAttributesKeys.ARGUMENT,
        "KEYWORD" to ParadoxScriptAttributesKeys.KEYWORD,

        "DEFINITION" to ParadoxScriptAttributesKeys.DEFINITION,
        "DEFINITION_NAME" to ParadoxScriptAttributesKeys.DEFINITION_NAME,
        "DEFINITION_REFERENCE" to ParadoxScriptAttributesKeys.DEFINITION_REFERENCE,
        "LOCALISATION_REFERENCE" to ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE,
        "SYNCED_LOCALISATION_REFERENCE" to ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE,
        "DEFINE_NAMESPACE" to ParadoxScriptAttributesKeys.DEFINE_NAMESPACE,
        "DEFINE_VARIABLE" to ParadoxScriptAttributesKeys.DEFINE_VARIABLE,
        "ENUM_VALUE" to ParadoxScriptAttributesKeys.ENUM_VALUE,
        "COMPLEX_ENUM_VALUE" to ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE,
        "DYNAMIC_VALUE" to ParadoxScriptAttributesKeys.DYNAMIC_VALUE,
        "VARIABLE" to ParadoxScriptAttributesKeys.VARIABLE,
        "MODIFIER" to ParadoxScriptAttributesKeys.MODIFIER,
        "TRIGGER" to ParadoxScriptAttributesKeys.TRIGGER,
        "EFFECT" to ParadoxScriptAttributesKeys.EFFECT,
        "SYSTEM_SCOPE" to ParadoxScriptAttributesKeys.SYSTEM_SCOPE,
        "SCOPE" to ParadoxScriptAttributesKeys.SCOPE,
        "SCOPE_PREFIX" to ParadoxScriptAttributesKeys.SCOPE_PREFIX,
        "VALUE_FIELD" to ParadoxScriptAttributesKeys.VALUE_FIELD,
        "VALUE_FIELD_PREFIX" to ParadoxScriptAttributesKeys.VALUE_FIELD_PREFIX,
        "SYSTEM_COMMAND_SCOPE" to ParadoxScriptAttributesKeys.SYSTEM_COMMAND_SCOPE,
        "COMMAND_SCOPE" to ParadoxScriptAttributesKeys.COMMAND_SCOPE,
        "COMMAND_SCOPE_PREFIX" to ParadoxScriptAttributesKeys.COMMAND_SCOPE_PREFIX,
        "COMMAND_FIELD" to ParadoxScriptAttributesKeys.COMMAND_FIELD,
        "COMMAND_FIELD_PREFIX" to ParadoxScriptAttributesKeys.COMMAND_FIELD_PREFIX,
        "DATABASE_OBJECT_TYPE" to ParadoxScriptAttributesKeys.DATABASE_OBJECT_TYPE,
        "DATABASE_OBJECT" to ParadoxScriptAttributesKeys.DATABASE_OBJECT,

        "TAG" to ParadoxScriptAttributesKeys.TAG,
        "TAG_TYPE_KEY_PREFIX" to ParadoxScriptAttributesKeys.TAG_TYPE_KEY_PREFIX,

        "DIRECTIVE" to ParadoxScriptAttributesKeys.DIRECTIVE,
        "DIRECTIVE_INLINE_SCRIPT" to ParadoxScriptAttributesKeys.DIRECTIVE_INLINE_SCRIPT,
        "DIRECTIVE_DEFINITION_INJECTION" to ParadoxScriptAttributesKeys.DIRECTIVE_DEFINITION_INJECTION,

        "PATH_REFERENCE" to ParadoxScriptAttributesKeys.PATH_REFERENCE,
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxScriptLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap

    override fun getIcon() = PlsIcons.FileTypes.ParadoxScript

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("script.settings.name")

    override fun getDemoText() = PlsPreviewTexts.scriptColorSettings
}
