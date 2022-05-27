package icu.windea.pls.script.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
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
			AttributesDescriptor(PlsBundle.message("script.displayName.inputParameter"), ParadoxScriptAttributesKeys.INPUT_PARAMETER_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.parameter"), ParadoxScriptAttributesKeys.PARAMETER_KEY),
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
			AttributesDescriptor(PlsBundle.message("script.displayName.enumValueReference"), ParadoxScriptAttributesKeys.ENUM_VALUE_REFERENCE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.pathReference"), ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY),
			AttributesDescriptor(PlsBundle.message("script.displayName.tag"), ParadoxScriptAttributesKeys.TAG_KEY)
		)
	}
	
	private val tagToDescriptorMap = mapOf(
		"definition" to ParadoxScriptAttributesKeys.DEFINITION_KEY,
		"definition-reference" to ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY,
		"localisation-reference" to ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY,
		"synced-localisation-reference" to ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY,
		"enum-value-reference" to ParadoxScriptAttributesKeys.ENUM_VALUE_REFERENCE_KEY,
		"path-reference" to ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY,
		"tag" to ParadoxScriptAttributesKeys.TAG_KEY
	)
	
	override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxScriptLanguage, null, null)
	
	override fun getAdditionalHighlightingTagToDescriptorMap() = tagToDescriptorMap
	
	override fun getIcon() = PlsIcons.paradoxScriptFileIcon
	
	override fun getAttributeDescriptors() = attributesDescriptors
	
	override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
	
	override fun getDisplayName() = paradoxScriptName
	
	override fun getDemoText() = paradoxScriptColorSettingsDemoText
}
