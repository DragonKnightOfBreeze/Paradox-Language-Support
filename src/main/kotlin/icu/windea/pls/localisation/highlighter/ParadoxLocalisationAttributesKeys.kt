package icu.windea.pls.localisation.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import icu.windea.pls.*

@Suppress("DEPRECATION")
object ParadoxLocalisationAttributesKeys {
	private val _separatorName = PlsBundle.message("localisation.externalName.separator")
	private val _numberName = PlsBundle.message("localisation.externalName.number")
	private val _localeName = PlsBundle.message("localisation.externalName.locale")
	private val _propertyKeyName = PlsBundle.message("localisation.externalName.propertyKey")
	private val _stringName = PlsBundle.message("localisation.externalName.string")
	private val _commentName = PlsBundle.message("localisation.externalName.comment")
	private val _markerName = PlsBundle.message("localisation.externalName.marker")
	private val _propertyReferenceName = PlsBundle.message("localisation.externalName.propertyReference")
	private val _parameterName = PlsBundle.message("localisation.externalName.parameter")
	private val _iconName = PlsBundle.message("localisation.externalName.icon")
	private val _sequentialNumberName = PlsBundle.message("localisation.externalName.sequentialNumber")
	private val _commandScopeName = PlsBundle.message("localisation.externalName.commandScope")
	private val _commandFieldName = PlsBundle.message("localisation.externalName.commandField")
	private val _colorName = PlsBundle.message("localisation.externalName.color")
	private val _validEscapeName = PlsBundle.message("localisation.externalName.validEscape")
	private val _invalidEscapeName = PlsBundle.message("localisation.externalName.invalidEscape")
	private val _badCharacterName = PlsBundle.message("localisation.externalName.badCharacter")
	private val _localisationName = PlsBundle.message("localisation.externalName.localisation")
	private val _syncedLocalisationName = PlsBundle.message("localisation.externalName.syncedLocalisation")
	
	@JvmField val SEPARATOR_KEY = createTextAttributesKey(_separatorName, OPERATION_SIGN)
	@JvmField val NUMBER_KEY = createTextAttributesKey(_numberName, NUMBER)
	@JvmField val LOCALE_KEY = createTextAttributesKey(_localeName, KEYWORD)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey(_propertyKeyName, KEYWORD)
	@JvmField val STRING_KEY = createTextAttributesKey(_stringName, STRING)
	@JvmField val COMMENT_KEY = createTextAttributesKey(_commentName, LINE_COMMENT)
	@JvmField val MARKER_KEY = createTextAttributesKey(_markerName, KEYWORD)
	@JvmField val PROPERTY_REFERENCE_KEY = createTextAttributesKey(_propertyReferenceName, KEYWORD)
	@JvmField val PARAMETER_KEY = createTextAttributesKey(_parameterName, IDENTIFIER)
	@JvmField val ICON_KEY = createTextAttributesKey(_iconName, IDENTIFIER)
	@JvmField val SEQUENTIAL_NUMBER_KEY = createTextAttributesKey(_sequentialNumberName, IDENTIFIER)
	@JvmField val COMMAND_SCOPE_KEY = createTextAttributesKey(_commandScopeName, IDENTIFIER)
	@JvmField val COMMAND_FIELD_KEY = createTextAttributesKey(_commandFieldName, IDENTIFIER)
	@JvmField val COLOR_KEY = createTextAttributesKey(_colorName, IDENTIFIER)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey(_validEscapeName, VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey(_invalidEscapeName, INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey(_badCharacterName, BAD_CHARACTER)
	@JvmField val LOCALISATION_KEY = createTextAttributesKey(_localisationName, PROPERTY_KEY_KEY)
	@JvmField val SYNCED_LOCALISATION_KEY = createTextAttributesKey(_syncedLocalisationName, PROPERTY_KEY_KEY)
	
	val COLOR_KEYS by lazy {
		getConfig().colorMap.mapValues { (_, color) ->
			createTextAttributesKey("${_colorName}_${color.name}", IDENTIFIER.defaultAttributes.clone().apply {
				foregroundColor = color.color
			})
		}
	}
}
