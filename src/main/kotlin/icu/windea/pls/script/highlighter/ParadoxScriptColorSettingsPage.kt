package icu.windea.pls.script.highlighter

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icu.windea.pls.*
import icu.windea.pls.script.*

class ParadoxScriptColorSettingsPage : ColorSettingsPage {
	companion object {
		private val _separatorName = PlsBundle.message("script.displayName.separator")
		private val _bracesName = PlsBundle.message("script.displayName.braces")
		private val _variableName = PlsBundle.message("script.displayName.variable")
		private val _propertyKeyName = PlsBundle.message("script.displayName.propertyKey")
		private val _keywordName = PlsBundle.message("script.displayName.keyword")
		private val _colorName = PlsBundle.message("script.displayName.color")
		private val _numberName = PlsBundle.message("script.displayName.number")
		private val _stringName = PlsBundle.message("script.displayName.string")
		private val _codeName = PlsBundle.message("script.displayName.code")
		private val _commentName = PlsBundle.message("script.displayName.comment")
		private val _validEscapeName = PlsBundle.message("script.displayName.validEscape")
		private val _invalidEscapeName = PlsBundle.message("script.displayName.invalidEscape")
		private val _badCharacterName = PlsBundle.message("script.displayName.badCharacter")
		private val _definitionName = PlsBundle.message("script.displayName.definition")
		private val _definitionReferenceName = PlsBundle.message("script.displayName.definitionReference")
		private val _localisationReferenceName = PlsBundle.message("script.displayName.localisationReference")
		private val _syncedLocalisationReferenceName = PlsBundle.message("script.displayName.syncedLocalisationReference")
		private val _enumName = PlsBundle.message("script.displayName.enumReference")
		
		private val attributesDescriptors = arrayOf(
			AttributesDescriptor(_separatorName, ParadoxScriptAttributesKeys.SEPARATOR_KEY),
			AttributesDescriptor(_bracesName, ParadoxScriptAttributesKeys.BRACES_KEY),
			AttributesDescriptor(_variableName, ParadoxScriptAttributesKeys.VARIABLE_KEY),
			AttributesDescriptor(_propertyKeyName, ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY),
			AttributesDescriptor(_keywordName, ParadoxScriptAttributesKeys.KEYWORD_KEY),
			AttributesDescriptor(_colorName, ParadoxScriptAttributesKeys.COLOR_KEY),
			AttributesDescriptor(_numberName, ParadoxScriptAttributesKeys.NUMBER_KEY),
			AttributesDescriptor(_stringName, ParadoxScriptAttributesKeys.STRING_KEY),
			AttributesDescriptor(_codeName, ParadoxScriptAttributesKeys.CODE_KEY),
			AttributesDescriptor(_commentName, ParadoxScriptAttributesKeys.COMMENT_KEY),
			AttributesDescriptor(_validEscapeName, ParadoxScriptAttributesKeys.VALID_ESCAPE_KEY),
			AttributesDescriptor(_invalidEscapeName, ParadoxScriptAttributesKeys.INVALID_ESCAPE_KEY),
			AttributesDescriptor(_badCharacterName, ParadoxScriptAttributesKeys.BAD_CHARACTER_KEY),
			AttributesDescriptor(_definitionName, ParadoxScriptAttributesKeys.DEFINITION_KEY),
			AttributesDescriptor(_definitionReferenceName, ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY),
			AttributesDescriptor(_localisationReferenceName, ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY),
			AttributesDescriptor(_syncedLocalisationReferenceName, ParadoxScriptAttributesKeys.SYNCED_LOCALISATION_REFERENCE_KEY),
			AttributesDescriptor(_enumName, ParadoxScriptAttributesKeys.BAD_CHARACTER_KEY)
		)
	}
	
	override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxScriptLanguage, null, null)
	
	override fun getAdditionalHighlightingTagToDescriptorMap() = null
	
	override fun getIcon() = PlsIcons.paradoxScriptFileIcon
	
	override fun getAttributeDescriptors() = attributesDescriptors
	
	override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
	
	override fun getDisplayName() = paradoxScriptName
	
	override fun getDemoText() = paradoxScriptDemoText
}
