package icu.windea.pls.cwt.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import icu.windea.pls.*

object CwtAttributeKeys {
	private val _separatorName = PlsBundle.message("cwt.externalName.separator")
	private val _bracesName = PlsBundle.message("cwt.externalName.braces")
	private val _propertyKeyName = PlsBundle.message("cwt.externalName.propertyKey")
	private val _optionKeyName = PlsBundle.message("cwt.externalName.optionKey")
	private val _keywordName = PlsBundle.message("cwt.externalName.keyword")
	private val _numberName = PlsBundle.message("cwt.externalName.number")
	private val _stringName = PlsBundle.message("cwt.externalName.string")
	private val _commentName = PlsBundle.message("cwt.externalName.comment")
	private val _optionCommentName = PlsBundle.message("cwt.externalName.optionComment")
	private val _documentationCommentName = PlsBundle.message("cwt.externalName.documentationComment")
	private val _validEscapeName = PlsBundle.message("cwt.externalName.validEscape")
	private val _invalidEscapeName = PlsBundle.message("cwt.externalName.invalidEscape")
	private val _badCharacterName = PlsBundle.message("cwt.externalName.badCharacter")
	
	@JvmField val SEPARATOR_KEY = createTextAttributesKey(_separatorName, OPERATION_SIGN)
	@JvmField val BRACES_KEY = createTextAttributesKey(_bracesName, BRACES)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey(_propertyKeyName, INSTANCE_FIELD)
	@JvmField val OPTION_KEY_KEY = createTextAttributesKey(_optionKeyName, DOC_COMMENT_TAG_VALUE)
	@JvmField val KEYWORD_KEY = createTextAttributesKey(_keywordName, KEYWORD)
	@JvmField val NUMBER_KEY = createTextAttributesKey(_numberName, NUMBER)
	@JvmField val STRING_KEY = createTextAttributesKey(_stringName, STRING)
	@JvmField val COMMENT_KEY = createTextAttributesKey(_commentName, LINE_COMMENT)
	@JvmField val OPTION_COMMENT_KEY = createTextAttributesKey(_optionCommentName, DOC_COMMENT)
	@JvmField val DOCUMENTATION_COMMENT_KEY = createTextAttributesKey(_documentationCommentName, DOC_COMMENT)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey(_validEscapeName, VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey(_invalidEscapeName, INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey(_badCharacterName, BAD_CHARACTER)
}

