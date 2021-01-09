package com.windea.plugin.idea.paradox.script.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import com.windea.plugin.idea.paradox.message

object ParadoxScriptAttributesKeys {
	private val _separatorName = message("paradox.script.externalName.separator")
	private val _bracesName = message("paradox.script.externalName.braces")
	private val _variableName = message("paradox.script.externalName.variable")
	private val _propertyKeyName = message("paradox.script.externalName.propertyKey")
	private val _keywordName = message("paradox.script.externalName.keyword")
	private val _colorName = message("paradox.script.externalName.color")
	private val _numberName = message("paradox.script.externalName.number")
	private val _stringName = message("paradox.script.externalName.string")
	private val _codeName = message("paradox.script.externalName.code")
	private val _commentName = message("paradox.script.externalName.comment")
	private val _validEscapeName = message("paradox.script.externalName.validEscape")
	private val _invalidEscapeName = message("paradox.script.externalName.invalidEscape")
	private val _badCharacterName = message("paradox.script.externalName.badCharacter")
	private val _definitionName = message("paradox.script.externalName.definition")
	//private val _localisationPropertyReferenceName = message("paradox.script.externalName.localisationPropertyReference")
	//private val _scriptPropertyReferenceName = message("paradox.script.externalName.scriptPropertyReference")
	
	@JvmField val SEPARATOR_KEY = createTextAttributesKey(_separatorName, OPERATION_SIGN)
	@JvmField val BRACES_KEY = createTextAttributesKey(_bracesName, BRACES)
	@JvmField val VARIABLE_KEY = createTextAttributesKey(_variableName, STATIC_FIELD)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey(_propertyKeyName, INSTANCE_FIELD)
	@JvmField val KEYWORD_KEY = createTextAttributesKey(_keywordName, KEYWORD)
	@JvmField val COLOR_KEY = createTextAttributesKey(_colorName, FUNCTION_DECLARATION)
	@JvmField val NUMBER_KEY = createTextAttributesKey(_numberName, NUMBER)
	@JvmField val STRING_KEY = createTextAttributesKey(_stringName, STRING)
	@JvmField val CODE_KEY = createTextAttributesKey(_codeName, IDENTIFIER)
	@JvmField val COMMENT_KEY = createTextAttributesKey(_commentName, LINE_COMMENT)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey(_validEscapeName, VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey(_invalidEscapeName, INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey(_badCharacterName, BAD_CHARACTER)
	@JvmField val DEFINITION_KEY = createTextAttributesKey(_definitionName, PROPERTY_KEY_KEY)
	//@JvmField val LOCALISATION_PROPERTY_REFERENCE_KEY = createTextAttributesKey(_localisationPropertyReferenceName, STRING)
	//@JvmField val SCRIPT_PROPERTY_REFERENCE_KEY = createTextAttributesKey(_scriptPropertyReferenceName, STRING)
}
