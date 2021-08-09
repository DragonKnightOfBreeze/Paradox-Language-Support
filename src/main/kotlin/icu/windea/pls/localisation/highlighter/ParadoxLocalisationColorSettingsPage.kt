package icu.windea.pls.localisation.highlighter

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*

class ParadoxLocalisationColorSettingsPage : ColorSettingsPage {
	companion object {
		private val _separatorName = message("localisation.displayName.separator")
		private val _numberName = message("localisation.displayName.number")
		private val _localeName = message("localisation.displayName.locale")
		private val _propertyKeyName = message("localisation.displayName.propertyKey")
		private val _stringName = message("localisation.displayName.string")
		private val _commentName = message("localisation.displayName.comment")
		private val _markerName = message("localisation.displayName.marker")
		private val _parameterName = message("localisation.displayName.parameter")
		private val _propertyReferenceName = message("localisation.displayName.propertyReference")
		private val _iconName = message("localisation.displayName.icon")
		private val _sequentialNumberName = message("localisation.displayName.sequentialNumber")
		private val _commandScopeName = message("localisation.displayName.commandScope")
		private val _commandFieldName = message("localisation.displayName.commandField")
		private val _colorName = message("localisation.displayName.color")
		private val _validEscapeName = message("localisation.displayName.validEscape")
		private val _invalidEscapeName = message("localisation.displayName.invalidEscape")
		private val _badCharacterName = message("localisation.displayName.badCharacter")
		private val _localisationName = message("localisation.displayName.localisation")
		private val _syncedLocalisationName = message("localisation.displayName.syncedLocalisation")
		
		private val attributesDescriptors = arrayOf(
			AttributesDescriptor(_separatorName, ParadoxLocalisationAttributesKeys.SEPARATOR_KEY),
			AttributesDescriptor(_numberName, ParadoxLocalisationAttributesKeys.NUMBER_KEY),
			AttributesDescriptor(_localeName, ParadoxLocalisationAttributesKeys.LOCALE_KEY),
			AttributesDescriptor(_propertyKeyName, ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY),
			AttributesDescriptor(_stringName, ParadoxLocalisationAttributesKeys.STRING_KEY),
			AttributesDescriptor(_commentName, ParadoxLocalisationAttributesKeys.COMMENT_KEY),
			AttributesDescriptor(_markerName, ParadoxLocalisationAttributesKeys.MARKER_KEY),
			AttributesDescriptor(_parameterName, ParadoxLocalisationAttributesKeys.PARAMETER_KEY),
			AttributesDescriptor(_propertyReferenceName, ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_KEY),
			AttributesDescriptor(_iconName, ParadoxLocalisationAttributesKeys.ICON_KEY),
			AttributesDescriptor(_sequentialNumberName, ParadoxLocalisationAttributesKeys.SEQUENTIAL_NUMBER_KEY),
			AttributesDescriptor(_commandScopeName, ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_KEY),
			AttributesDescriptor(_commandFieldName, ParadoxLocalisationAttributesKeys.COMMAND_FIELD_KEY),
			AttributesDescriptor(_colorName, ParadoxLocalisationAttributesKeys.COLOR_KEY),
			AttributesDescriptor(_validEscapeName, ParadoxLocalisationAttributesKeys.VALID_ESCAPE_KEY),
			AttributesDescriptor(_invalidEscapeName, ParadoxLocalisationAttributesKeys.INVALID_ESCAPE_KEY),
			AttributesDescriptor(_badCharacterName, ParadoxLocalisationAttributesKeys.BAD_CHARACTER_KEY),
			AttributesDescriptor(_localisationName, ParadoxLocalisationAttributesKeys.LOCALISATION_KEY),
			AttributesDescriptor(_syncedLocalisationName, ParadoxLocalisationAttributesKeys.SYNCED_LOCALISATION_KEY)
		)
	}

	override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxLocalisationLanguage, null, null)

	override fun getAdditionalHighlightingTagToDescriptorMap() = null

	override fun getIcon() = paradoxLocalisationFileIcon

	override fun getAttributeDescriptors() = attributesDescriptors

	override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

	override fun getDisplayName() = paradoxLocalisationName

	override fun getDemoText() = paradoxLocalisationDemoText
}

