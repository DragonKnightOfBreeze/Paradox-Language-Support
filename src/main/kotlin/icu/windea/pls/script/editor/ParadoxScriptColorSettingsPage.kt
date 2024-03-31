package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.highlighter.*

class ParadoxScriptColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(PlsBundle.message("script.displayName.braces"), ParadoxScriptAttributesKeys.BRACES_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.operator"), ParadoxScriptAttributesKeys.OPERATOR_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.marker"), ParadoxScriptAttributesKeys.MARKER_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.parameterConditionBrackets"), ParadoxScriptAttributesKeys.PARAMETER_CONDITION_BRACKETS_KEYS),
        AttributesDescriptor(PlsBundle.message("script.displayName.parameterConditionExpressionBrackets"), ParadoxScriptAttributesKeys.PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS),
        AttributesDescriptor(PlsBundle.message("script.displayName.inlineMathBrackets"), ParadoxScriptAttributesKeys.INLINE_MATH_BRACES_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.inlineMathOperators"), ParadoxScriptAttributesKeys.INLINE_MATH_OPERATOR_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.keyword"), ParadoxScriptAttributesKeys.KEYWORD_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.comment"), ParadoxScriptAttributesKeys.COMMENT_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.scriptedVariable"), ParadoxScriptAttributesKeys.SCRIPTED_VARIABLE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.parameter"), ParadoxScriptAttributesKeys.PARAMETER_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.conditionParameter"), ParadoxScriptAttributesKeys.CONDITION_PARAMETER_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.argument"), ParadoxScriptAttributesKeys.ARGUMENT_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.propertyKey"), ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.color"), ParadoxScriptAttributesKeys.COLOR_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.number"), ParadoxScriptAttributesKeys.NUMBER_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.string"), ParadoxScriptAttributesKeys.STRING_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.validEscape"), ParadoxScriptAttributesKeys.VALID_ESCAPE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.invalidEscape"), ParadoxScriptAttributesKeys.INVALID_ESCAPE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.badCharacter"), ParadoxScriptAttributesKeys.BAD_CHARACTER_KEY),
        
        AttributesDescriptor(PlsBundle.message("script.displayName.definition"), ParadoxScriptAttributesKeys.DEFINITION_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.definitionName"), ParadoxScriptAttributesKeys.DEFINITION_NAME_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.definitionReference"), ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.localisationReference"), ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.syncedLocalisationReference"), ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.pathReference"), ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.enumValue"), ParadoxScriptAttributesKeys.ENUM_VALUE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.complexEnumValue"), ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.variable"), ParadoxScriptAttributesKeys.VARIABLE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.dynamicValue"), ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.systemLink"), ParadoxScriptAttributesKeys.SYSTEM_LINK_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.scope"), ParadoxScriptAttributesKeys.SCOPE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.scopeLinkPrefix"), ParadoxScriptAttributesKeys.SCOPE_LINK_PREFIX_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.scopeLinkDataSource"), ParadoxScriptAttributesKeys.SCOPE_LINK_DATA_SOURCE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.valueLinkValue"), ParadoxScriptAttributesKeys.VALUE_LINK_VALUE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.valueLinkPrefix"), ParadoxScriptAttributesKeys.VALUE_LINK_PREFIX_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.valueLinkDataSource"), ParadoxScriptAttributesKeys.VALUE_LINK_DATA_SOURCE_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.modifier"), ParadoxScriptAttributesKeys.MODIFIER_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.trigger"), ParadoxScriptAttributesKeys.TRIGGER_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.effect"), ParadoxScriptAttributesKeys.EFFECT_KEY),
        AttributesDescriptor(PlsBundle.message("script.displayName.tag"), ParadoxScriptAttributesKeys.TAG_KEY),
    )
    
    private val _tagToDescriptorMap = mapOf(
        "DEFINITION" to ParadoxScriptAttributesKeys.DEFINITION_KEY,
        "DEFINITION_NAME" to ParadoxScriptAttributesKeys.DEFINITION_NAME_KEY,
        "DEFINITION_REFERENCE" to ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY,
        "LOCALISATION_REFERENCE" to ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY,
        "SYNCED_LOCALISATION_REFERENCE" to ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY,
        "PATH_REFERENCE" to ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY,
        "ENUM_VALUE" to ParadoxScriptAttributesKeys.ENUM_VALUE_KEY,
        "COMPLEX_ENUM_VALUE" to ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY,
        "DYNAMIC_VALUE" to ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY,
        "SYSTEM_LINK" to ParadoxScriptAttributesKeys.SYSTEM_LINK_KEY,
        "SCOPE" to ParadoxScriptAttributesKeys.SCOPE_KEY,
        "SCOPE_LINK_PREFIX" to ParadoxScriptAttributesKeys.SCOPE_LINK_PREFIX_KEY,
        "SCOPE_LINK_DATA_SOURCE" to ParadoxScriptAttributesKeys.SCOPE_LINK_DATA_SOURCE_KEY,
        "VALUE_LINK_VALUE" to ParadoxScriptAttributesKeys.VALUE_LINK_VALUE_KEY,
        "VALUE_LINK_PREFIX" to ParadoxScriptAttributesKeys.VALUE_LINK_PREFIX_KEY,
        "VALUE_LINK_DATA_SOURCE" to ParadoxScriptAttributesKeys.VALUE_LINK_DATA_SOURCE_KEY,
        "TAG" to ParadoxScriptAttributesKeys.TAG_KEY,
        "MODIFIER" to ParadoxScriptAttributesKeys.MODIFIER_KEY,
        "TRIGGER" to ParadoxScriptAttributesKeys.TRIGGER_KEY,
        "EFFECT" to ParadoxScriptAttributesKeys.EFFECT_KEY,
        "VARIABLE" to ParadoxScriptAttributesKeys.VARIABLE_KEY
    )
    
    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxScriptLanguage, null, null)
    
    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap
    
    override fun getIcon() = PlsIcons.FileTypes.ParadoxScript
    
    override fun getAttributeDescriptors() = _attributesDescriptors
    
    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
    
    override fun getDisplayName() = PlsBundle.message("options.script.displayName")
    
    override fun getDemoText() = PlsConstants.paradoxScriptColorSettingsSample
}
