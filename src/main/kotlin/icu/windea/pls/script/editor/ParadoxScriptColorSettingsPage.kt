package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.highlighter.*

class ParadoxScriptColorSettingsPage : ColorSettingsPage {
	companion object {
		private val attributesDescriptors = arrayOf(
			AttributesDescriptor(PlsBundle.message("script.displayName.braces"), ParadoxScriptAttributesKeys.BRACES_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.operator"), ParadoxScriptAttributesKeys.OPERATOR_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.marker"), ParadoxScriptAttributesKeys.MARKER_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.parameterConditionBrackets"), ParadoxScriptAttributesKeys.PARAMETER_CONDITION_BRACKETS_KEYS),
			AttributesDescriptor(PlsBundle.message("script.displayName.parameterConditionExpressionBrackets"), ParadoxScriptAttributesKeys.PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS),
			AttributesDescriptor(PlsBundle.message("script.displayName.inlineMathBrackets"), ParadoxScriptAttributesKeys.INLINE_MATH_BRACES_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.inlineMathOperators"), ParadoxScriptAttributesKeys.INLINE_MATH_OPERATOR_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.comment"), ParadoxScriptAttributesKeys.COMMENT_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.keyword"), ParadoxScriptAttributesKeys.KEYWORD_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.variable"), ParadoxScriptAttributesKeys.VARIABLE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.parameter"), ParadoxScriptAttributesKeys.PARAMETER_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.argument"), ParadoxScriptAttributesKeys.ARGUMENT_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.propertyKey"), ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.color"), ParadoxScriptAttributesKeys.COLOR_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.number"), ParadoxScriptAttributesKeys.NUMBER_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.string"), ParadoxScriptAttributesKeys.STRING_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.validEscape"), ParadoxScriptAttributesKeys.VALID_ESCAPE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.invalidEscape"), ParadoxScriptAttributesKeys.INVALID_ESCAPE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.badCharacter"), ParadoxScriptAttributesKeys.BAD_CHARACTER_KEY),
			
			AttributesDescriptor(PlsBundle.message("script.displayName.definition"), ParadoxScriptAttributesKeys.DEFINITION_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.definitionReference"), ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.localisationReference"), ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.syncedLocalisationReference"), ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.pathReference"), ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.enumValue"), ParadoxScriptAttributesKeys.ENUM_VALUE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.complexEnumValue"), ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.valueSetValue"), ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.systemScope"), ParadoxScriptAttributesKeys.SYSTEM_SCOPE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.scope"), ParadoxScriptAttributesKeys.SCOPE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.scopeFieldPrefix"), ParadoxScriptAttributesKeys.SCOPE_FIELD_PREFIX_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.valueFieldPrefix"), ParadoxScriptAttributesKeys.VALUE_FIELD_PREFIX_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.modifier"), ParadoxScriptAttributesKeys.MODIFIER_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.tag"), ParadoxScriptAttributesKeys.TAG_KEY)
		)
	}
	
	private val tagToDescriptorMap = mapOf(
		"STRING" to ParadoxScriptAttributesKeys.STRING_KEY,
		"DEFINITION" to ParadoxScriptAttributesKeys.DEFINITION_KEY,
		"DEFINITION_REFERENCE" to ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY,
		"LOCALISATION_REFERENCE" to ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY,
		"SYNCED_LOCALISATION_REFERENCE" to ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY,
		"PATH_REFERENCE" to ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY,
		"ENUM_VALUE" to ParadoxScriptAttributesKeys.ENUM_VALUE_KEY,
		"COMPLEX_ENUM_VALUE" to ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY,
		"VALUE_SET_VALUE" to ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY,
		"SYSTEM_SCOPE" to ParadoxScriptAttributesKeys.SYSTEM_SCOPE_KEY,
		"SCOPE" to ParadoxScriptAttributesKeys.SCOPE_KEY,
		"SCOPE_FIELD_PREFIX" to ParadoxScriptAttributesKeys.SCOPE_FIELD_PREFIX_KEY,
		"VALUE_FIELD_PREFIX" to ParadoxScriptAttributesKeys.VALUE_FIELD_PREFIX_KEY,
		"MODIFIER" to ParadoxScriptAttributesKeys.MODIFIER_KEY,
		"TAG" to ParadoxScriptAttributesKeys.TAG_KEY
	)
	
	override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxScriptLanguage, null, null)
	
	override fun getAdditionalHighlightingTagToDescriptorMap() = tagToDescriptorMap
	
	override fun getIcon() = PlsIcons.ParadoxScriptFile
	
	override fun getAttributeDescriptors() = attributesDescriptors
	
	override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
	
	override fun getDisplayName() = paradoxScriptName
	
	override fun getDemoText() = paradoxScriptColorSettingsDemoText
}
