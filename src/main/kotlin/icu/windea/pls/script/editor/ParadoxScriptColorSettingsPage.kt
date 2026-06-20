package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.script.ParadoxScriptConstants
import icu.windea.pls.script.ParadoxScriptLanguage

class ParadoxScriptColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(PlsBundle.message("script.displayName.braces"), ParadoxScriptHighlighterColors.BRACES),
        AttributesDescriptor(PlsBundle.message("script.displayName.operator"), ParadoxScriptHighlighterColors.OPERATOR),
        AttributesDescriptor(PlsBundle.message("script.displayName.marker"), ParadoxScriptHighlighterColors.MARKER),
        AttributesDescriptor(PlsBundle.message("script.displayName.conditionalBlockBrackets"), ParadoxScriptHighlighterColors.CONDITIONAL_BLOCK_BRACKETS),
        AttributesDescriptor(PlsBundle.message("script.displayName.conditionalBlockExpressionBrackets"), ParadoxScriptHighlighterColors.CONDITIONAL_BLOCK_EXPRESSION_BRACKETS),
        AttributesDescriptor(PlsBundle.message("script.displayName.inlineMathBrackets"), ParadoxScriptHighlighterColors.INLINE_MATH_BRACKETS),
        AttributesDescriptor(PlsBundle.message("script.displayName.inlineMathOperators"), ParadoxScriptHighlighterColors.INLINE_MATH_OPERATOR),
        AttributesDescriptor(PlsBundle.message("script.displayName.comment"), ParadoxScriptHighlighterColors.COMMENT),
        AttributesDescriptor(PlsBundle.message("script.displayName.keyword"), ParadoxScriptHighlighterColors.KEYWORD),
        AttributesDescriptor(PlsBundle.message("script.displayName.atSign"), ParadoxScriptHighlighterColors.AT_SIGN),
        AttributesDescriptor(PlsBundle.message("script.displayName.scriptedVariableName"), ParadoxScriptHighlighterColors.SCRIPTED_VARIABLE_NAME),
        AttributesDescriptor(PlsBundle.message("script.displayName.scriptedVariableReference"), ParadoxScriptHighlighterColors.SCRIPTED_VARIABLE_REFERENCE),
        AttributesDescriptor(PlsBundle.message("script.displayName.parameter"), ParadoxScriptHighlighterColors.PARAMETER),
        AttributesDescriptor(PlsBundle.message("script.displayName.conditionParameter"), ParadoxScriptHighlighterColors.CONDITION_PARAMETER),
        AttributesDescriptor(PlsBundle.message("script.displayName.argument"), ParadoxScriptHighlighterColors.ARGUMENT),
        AttributesDescriptor(PlsBundle.message("script.displayName.propertyKey"), ParadoxScriptHighlighterColors.PROPERTY_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.number"), ParadoxScriptHighlighterColors.NUMBER),
        AttributesDescriptor(PlsBundle.message("script.displayName.string"), ParadoxScriptHighlighterColors.STRING),
        AttributesDescriptor(PlsBundle.message("script.displayName.color"), ParadoxScriptHighlighterColors.COLOR),
        AttributesDescriptor(PlsBundle.message("script.displayName.validEscape"), ParadoxScriptHighlighterColors.VALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("script.displayName.invalidEscape"), ParadoxScriptHighlighterColors.INVALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("script.displayName.badCharacter"), ParadoxScriptHighlighterColors.BAD_CHARACTER),

        AttributesDescriptor(PlsBundle.message("semantic.displayName.operator"), ParadoxScriptHighlighterColors.SEMANTIC_OPERATOR),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.marker"), ParadoxScriptHighlighterColors.SEMANTIC_MARKER),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.keyword"), ParadoxScriptHighlighterColors.SEMANTIC_KEYWORD),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.argument"), ParadoxScriptHighlighterColors.SEMANTIC_ARGUMENT),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.string"), ParadoxScriptHighlighterColors.SEMANTIC_STRING),

        AttributesDescriptor(PlsBundle.message("semantic.displayName.definition"), ParadoxScriptHighlighterColors.DEFINITION),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.definitionName"), ParadoxScriptHighlighterColors.DEFINITION_NAME),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.definitionReference"), ParadoxScriptHighlighterColors.DEFINITION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.localisationReference"), ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.defineNamespace"), ParadoxScriptHighlighterColors.DEFINE_NAMESPACE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.defineVariable"), ParadoxScriptHighlighterColors.DEFINE_VARIABLE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.enumValue"), ParadoxScriptHighlighterColors.ENUM_VALUE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.complexEnumValue"), ParadoxScriptHighlighterColors.COMPLEX_ENUM_VALUE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.dynamicValue"), ParadoxScriptHighlighterColors.DYNAMIC_VALUE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.variable"), ParadoxScriptHighlighterColors.VARIABLE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.modifier"), ParadoxScriptHighlighterColors.MODIFIER),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.trigger"), ParadoxScriptHighlighterColors.TRIGGER),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.effect"), ParadoxScriptHighlighterColors.EFFECT),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.systemScope"), ParadoxScriptHighlighterColors.SYSTEM_SCOPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.scope"), ParadoxScriptHighlighterColors.SCOPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.scopePrefix"), ParadoxScriptHighlighterColors.SCOPE_PREFIX),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.valueField"), ParadoxScriptHighlighterColors.VALUE_FIELD),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.valueFieldPrefix"), ParadoxScriptHighlighterColors.VALUE_FIELD_PREFIX),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.systemCommandScope"), ParadoxScriptHighlighterColors.SYSTEM_COMMAND_SCOPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandScope"), ParadoxScriptHighlighterColors.COMMAND_SCOPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandScopePrefix"), ParadoxScriptHighlighterColors.COMMAND_SCOPE_PREFIX),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandField"), ParadoxScriptHighlighterColors.COMMAND_FIELD),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandFieldPrefix"), ParadoxScriptHighlighterColors.COMMAND_FIELD_PREFIX),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.databaseObjectType"), ParadoxScriptHighlighterColors.DATABASE_OBJECT_TYPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.databaseObject"), ParadoxScriptHighlighterColors.DATABASE_OBJECT),

        AttributesDescriptor(PlsBundle.message("semantic.displayName.tag"), ParadoxScriptHighlighterColors.TAG),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.macro"), ParadoxScriptHighlighterColors.MACRO),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.pathReference"), ParadoxScriptHighlighterColors.PATH_REFERENCE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.shaderEffectReference"), ParadoxScriptHighlighterColors.SHADER_EFFECT_REFERENCE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.meshLocatorReference"), ParadoxScriptHighlighterColors.MESH_LOCATOR_REFERENCE),
    )

    private val _tagToDescriptorMap = mapOf(
        "OPERATOR" to ParadoxScriptHighlighterColors.SEMANTIC_OPERATOR,
        "MARKER" to ParadoxScriptHighlighterColors.SEMANTIC_MARKER,
        "KEYWORD" to ParadoxScriptHighlighterColors.SEMANTIC_KEYWORD,
        "ARGUMENT" to ParadoxScriptHighlighterColors.SEMANTIC_ARGUMENT,
        "STRING" to ParadoxScriptHighlighterColors.SEMANTIC_STRING,

        "DEFINITION" to ParadoxScriptHighlighterColors.DEFINITION,
        "DEFINITION_NAME" to ParadoxScriptHighlighterColors.DEFINITION_NAME,
        "DEFINITION_REFERENCE" to ParadoxScriptHighlighterColors.DEFINITION_REFERENCE,
        "LOCALISATION_REFERENCE" to ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE,
        "DEFINE_NAMESPACE" to ParadoxScriptHighlighterColors.DEFINE_NAMESPACE,
        "DEFINE_VARIABLE" to ParadoxScriptHighlighterColors.DEFINE_VARIABLE,
        "ENUM_VALUE" to ParadoxScriptHighlighterColors.ENUM_VALUE,
        "COMPLEX_ENUM_VALUE" to ParadoxScriptHighlighterColors.COMPLEX_ENUM_VALUE,
        "DYNAMIC_VALUE" to ParadoxScriptHighlighterColors.DYNAMIC_VALUE,
        "VARIABLE" to ParadoxScriptHighlighterColors.VARIABLE,
        "MODIFIER" to ParadoxScriptHighlighterColors.MODIFIER,
        "TRIGGER" to ParadoxScriptHighlighterColors.TRIGGER,
        "EFFECT" to ParadoxScriptHighlighterColors.EFFECT,
        "SYSTEM_SCOPE" to ParadoxScriptHighlighterColors.SYSTEM_SCOPE,
        "SCOPE" to ParadoxScriptHighlighterColors.SCOPE,
        "SCOPE_PREFIX" to ParadoxScriptHighlighterColors.SCOPE_PREFIX,
        "VALUE_FIELD" to ParadoxScriptHighlighterColors.VALUE_FIELD,
        "VALUE_FIELD_PREFIX" to ParadoxScriptHighlighterColors.VALUE_FIELD_PREFIX,
        "SYSTEM_COMMAND_SCOPE" to ParadoxScriptHighlighterColors.SYSTEM_COMMAND_SCOPE,
        "COMMAND_SCOPE" to ParadoxScriptHighlighterColors.COMMAND_SCOPE,
        "COMMAND_SCOPE_PREFIX" to ParadoxScriptHighlighterColors.COMMAND_SCOPE_PREFIX,
        "COMMAND_FIELD" to ParadoxScriptHighlighterColors.COMMAND_FIELD,
        "COMMAND_FIELD_PREFIX" to ParadoxScriptHighlighterColors.COMMAND_FIELD_PREFIX,
        "DATABASE_OBJECT_TYPE" to ParadoxScriptHighlighterColors.DATABASE_OBJECT_TYPE,
        "DATABASE_OBJECT" to ParadoxScriptHighlighterColors.DATABASE_OBJECT,

        "TAG" to ParadoxScriptHighlighterColors.TAG,
        "MACRO" to ParadoxScriptHighlighterColors.MACRO,
        "PATH_REFERENCE" to ParadoxScriptHighlighterColors.PATH_REFERENCE,
        "SHADER_EFFECT_REFERENCE" to ParadoxScriptHighlighterColors.SHADER_EFFECT_REFERENCE,
        "MESH_LOCATOR_REFERENCE" to ParadoxScriptHighlighterColors.MESH_LOCATOR_REFERENCE,
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxScriptLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap

    override fun getIcon() = PlsIcons.FileTypes.ParadoxScript

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("script.settings.name")

    override fun getDemoText() = ParadoxScriptConstants.colorSettingsText
}
