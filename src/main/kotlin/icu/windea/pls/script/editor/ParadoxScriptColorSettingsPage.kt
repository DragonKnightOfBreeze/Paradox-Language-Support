package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.script.ParadoxScriptConstants
import icu.windea.pls.script.ParadoxScriptLanguage

class ParadoxScriptColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(ChronicleBundle.message("script.displayName.braces"), ParadoxScriptHighlighterColors.BRACES),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.operator"), ParadoxScriptHighlighterColors.OPERATOR),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.marker"), ParadoxScriptHighlighterColors.MARKER),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.conditionalBlockBrackets"), ParadoxScriptHighlighterColors.CONDITIONAL_BLOCK_BRACKETS),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.conditionalBlockExpressionBrackets"), ParadoxScriptHighlighterColors.CONDITIONAL_BLOCK_EXPRESSION_BRACKETS),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.inlineMathBrackets"), ParadoxScriptHighlighterColors.INLINE_MATH_BRACKETS),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.inlineMathOperators"), ParadoxScriptHighlighterColors.INLINE_MATH_OPERATOR),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.comment"), ParadoxScriptHighlighterColors.COMMENT),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.keyword"), ParadoxScriptHighlighterColors.KEYWORD),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.atSign"), ParadoxScriptHighlighterColors.AT_SIGN),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.scriptedVariableName"), ParadoxScriptHighlighterColors.SCRIPTED_VARIABLE_NAME),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.scriptedVariableReference"), ParadoxScriptHighlighterColors.SCRIPTED_VARIABLE_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.parameter"), ParadoxScriptHighlighterColors.PARAMETER),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.conditionParameter"), ParadoxScriptHighlighterColors.CONDITION_PARAMETER),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.argument"), ParadoxScriptHighlighterColors.ARGUMENT),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.propertyKey"), ParadoxScriptHighlighterColors.PROPERTY_KEY),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.number"), ParadoxScriptHighlighterColors.NUMBER),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.string"), ParadoxScriptHighlighterColors.STRING),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.color"), ParadoxScriptHighlighterColors.COLOR),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.validEscape"), ParadoxScriptHighlighterColors.VALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.invalidEscape"), ParadoxScriptHighlighterColors.INVALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("script.displayName.badCharacter"), ParadoxScriptHighlighterColors.BAD_CHARACTER),

        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.operator"), ParadoxScriptHighlighterColors.SEMANTIC_OPERATOR),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.marker"), ParadoxScriptHighlighterColors.SEMANTIC_MARKER),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.keyword"), ParadoxScriptHighlighterColors.SEMANTIC_KEYWORD),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.argument"), ParadoxScriptHighlighterColors.SEMANTIC_ARGUMENT),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.string"), ParadoxScriptHighlighterColors.SEMANTIC_STRING),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.number"), ParadoxScriptHighlighterColors.SEMANTIC_NUMBER),

        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.definition"), ParadoxScriptHighlighterColors.DEFINITION),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.definitionName"), ParadoxScriptHighlighterColors.DEFINITION_NAME),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.definitionReference"), ParadoxScriptHighlighterColors.DEFINITION_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.localisationReference"), ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.defineNamespace"), ParadoxScriptHighlighterColors.DEFINE_NAMESPACE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.defineVariable"), ParadoxScriptHighlighterColors.DEFINE_VARIABLE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.enumValue"), ParadoxScriptHighlighterColors.ENUM_VALUE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.complexEnumValue"), ParadoxScriptHighlighterColors.COMPLEX_ENUM_VALUE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.dynamicValue"), ParadoxScriptHighlighterColors.DYNAMIC_VALUE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.variable"), ParadoxScriptHighlighterColors.VARIABLE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.modifier"), ParadoxScriptHighlighterColors.MODIFIER),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.trigger"), ParadoxScriptHighlighterColors.TRIGGER),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.effect"), ParadoxScriptHighlighterColors.EFFECT),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.systemScope"), ParadoxScriptHighlighterColors.SYSTEM_SCOPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.scope"), ParadoxScriptHighlighterColors.SCOPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.scopePrefix"), ParadoxScriptHighlighterColors.SCOPE_PREFIX),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.valueField"), ParadoxScriptHighlighterColors.VALUE_FIELD),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.valueFieldPrefix"), ParadoxScriptHighlighterColors.VALUE_FIELD_PREFIX),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.systemCommandScope"), ParadoxScriptHighlighterColors.SYSTEM_COMMAND_SCOPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.commandScope"), ParadoxScriptHighlighterColors.COMMAND_SCOPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.commandScopePrefix"), ParadoxScriptHighlighterColors.COMMAND_SCOPE_PREFIX),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.commandField"), ParadoxScriptHighlighterColors.COMMAND_FIELD),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.commandFieldPrefix"), ParadoxScriptHighlighterColors.COMMAND_FIELD_PREFIX),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.databaseObjectType"), ParadoxScriptHighlighterColors.DATABASE_OBJECT_TYPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.databaseObject"), ParadoxScriptHighlighterColors.DATABASE_OBJECT),

        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.tag"), ParadoxScriptHighlighterColors.TAG),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.macro"), ParadoxScriptHighlighterColors.MACRO),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.pathReference"), ParadoxScriptHighlighterColors.PATH_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.shaderEffectReference"), ParadoxScriptHighlighterColors.SHADER_EFFECT_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.meshLocatorReference"), ParadoxScriptHighlighterColors.MESH_LOCATOR_REFERENCE),
    )

    private val _tagToDescriptorMap = mapOf(
        "OPERATOR" to ParadoxScriptHighlighterColors.SEMANTIC_OPERATOR,
        "MARKER" to ParadoxScriptHighlighterColors.SEMANTIC_MARKER,
        "KEYWORD" to ParadoxScriptHighlighterColors.SEMANTIC_KEYWORD,
        "ARGUMENT" to ParadoxScriptHighlighterColors.SEMANTIC_ARGUMENT,
        "STRING" to ParadoxScriptHighlighterColors.SEMANTIC_STRING,
        "NUMBER" to ParadoxScriptHighlighterColors.SEMANTIC_NUMBER,

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

    override fun getIcon() = ChronicleIcons.FileTypes.ParadoxScript

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = ChronicleBundle.message("script.settings.name")

    override fun getDemoText() = ParadoxScriptConstants.colorSettingsText
}
