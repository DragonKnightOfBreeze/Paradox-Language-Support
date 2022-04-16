package icu.windea.pls.script.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import icu.windea.pls.*
import icu.windea.pls.localisation.highlighter.*

object ParadoxScriptAttributesKeys {
	private val _separatorName = PlsBundle.message("script.externalName.separator")
	private val _bracesName = PlsBundle.message("script.externalName.braces")
	private val _variableName = PlsBundle.message("script.externalName.variable")
	private val _propertyKeyName = PlsBundle.message("script.externalName.propertyKey")
	private val _keywordName = PlsBundle.message("script.externalName.keyword")
	private val _colorName = PlsBundle.message("script.externalName.color")
	private val _numberName = PlsBundle.message("script.externalName.number")
	private val _stringName = PlsBundle.message("script.externalName.string")
	private val _codeName = PlsBundle.message("script.externalName.code")
	private val _commentName = PlsBundle.message("script.externalName.comment")
	private val _validEscapeName = PlsBundle.message("script.externalName.validEscape")
	private val _invalidEscapeName = PlsBundle.message("script.externalName.invalidEscape")
	private val _badCharacterName = PlsBundle.message("script.externalName.badCharacter")
	private val _definitionName = PlsBundle.message("script.externalName.definition")
	private val _definitionReferenceName = PlsBundle.message("script.externalName.definitionReference")
	private val _localisationReferenceName = PlsBundle.message("script.externalName.localisationReference")
	private val _syncedLocalisationReferenceName = PlsBundle.message("script.externalName.syncedLocalisationReference")
	private val _enumReferenceName = PlsBundle.message("script.externalName.enumReference")
	
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
	@JvmField val DEFINITION_REFERENCE_KEY = createTextAttributesKey(_definitionReferenceName, PROPERTY_KEY_KEY)
	@JvmField val LOCALISATION_REFERENCE_KEY = createTextAttributesKey(_localisationReferenceName, ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY)
	@JvmField val SYNCED_LOCALISATION_REFERENCE_KEY = createTextAttributesKey(_syncedLocalisationReferenceName, ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY)
	@JvmField val ENUM_REFERENCE_KEY = createTextAttributesKey(_enumReferenceName, STATIC_FIELD)
}
