package com.windea.plugin.idea.paradox.localisation.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import com.windea.plugin.idea.paradox.*

@Suppress("DEPRECATION")
object ParadoxLocalisationAttributesKeys {
	private val _separatorName = message("paradox.localisation.externalName.separator")
	private val _numberName = message("paradox.localisation.externalName.number")
	private val _localeName = message("paradox.localisation.externalName.locale")
	private val _propertyKeyName = message("paradox.localisation.externalName.propertyKey")
	private val _stringName = message("paradox.localisation.externalName.string")
	private val _commentName = message("paradox.localisation.externalName.comment")
	private val _markerName = message("paradox.localisation.externalName.marker")
	private val _propertyReferenceName = message("paradox.localisation.externalName.propertyReference")
	private val _parameterName = message("paradox.localisation.externalName.parameter")
	private val _commandKeyName = message("paradox.localisation.externalName.commandKey")
	private val _iconName = message("paradox.localisation.externalName.icon")
	private val _serialNumberName = message("paradox.localisation.externalName.serialNumber")
	private val _colorName = message("paradox.localisation.externalName.color")
	private val _validEscapeName = message("paradox.localisation.externalName.validEscape")
	private val _invalidEscapeName = message("paradox.localisation.externalName.invalidEscape")
	private val _badCharacterName = message("paradox.localisation.externalName.badCharacter")
	
	@JvmField val SEPARATOR_KEY = createTextAttributesKey(_separatorName, OPERATION_SIGN)
	@JvmField val NUMBER_KEY = createTextAttributesKey(_numberName, NUMBER)
	@JvmField val LOCALE_KEY = createTextAttributesKey(_localeName, KEYWORD)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey(_propertyKeyName, KEYWORD)
	@JvmField val STRING_KEY = createTextAttributesKey(_stringName, STRING)
	@JvmField val COMMENT_KEY = createTextAttributesKey(_commentName, LINE_COMMENT)
	@JvmField val MARKER_KEY = createTextAttributesKey(_markerName, KEYWORD)
	@JvmField val PROPERTY_REFERENCE_KEY = createTextAttributesKey(_propertyReferenceName, KEYWORD)
	@JvmField val PARAMETER_KEY = createTextAttributesKey(_parameterName, IDENTIFIER)
	@JvmField val COMMAND_KEY_KEY = createTextAttributesKey(_commandKeyName, IDENTIFIER)
	@JvmField val ICON_KEY = createTextAttributesKey(_iconName, IDENTIFIER)
	@JvmField val SERIAL_NUMBER_KEY = createTextAttributesKey(_serialNumberName, IDENTIFIER)
	@JvmField val COLOR_KEY = createTextAttributesKey(_colorName, IDENTIFIER)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey(_validEscapeName, VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey(_invalidEscapeName, INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey(_badCharacterName, BAD_CHARACTER)

	@JvmField val COLOR_KEYS = ParadoxColor.map.mapValues { (_, value) ->
		createTextAttributesKey("${_colorName}_${value.key}", IDENTIFIER.defaultAttributes.clone().apply {
			foregroundColor = value.color
		})
	}
}
