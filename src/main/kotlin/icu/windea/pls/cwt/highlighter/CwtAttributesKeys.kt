package icu.windea.pls.cwt.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*

object CwtAttributesKeys {
	@JvmField val BRACES_KEY = createTextAttributesKey("CWT.BRACES", BRACES)
	@JvmField val OPERATOR_KEY = createTextAttributesKey("CWT.OPERATOR", OPERATION_SIGN)
	@JvmField val COMMENT_KEY = createTextAttributesKey("CWT.COMMENT", LINE_COMMENT)
	@JvmField val OPTION_COMMENT_KEY = createTextAttributesKey("CWT.OPTION_COMMENT", DOC_COMMENT)
	@JvmField val DOCUMENTATION_COMMENT_KEY = createTextAttributesKey("CWT.DOCUMENTATION_COMMENT", DOC_COMMENT)
	@JvmField val KEYWORD_KEY = createTextAttributesKey("CWT.KEYWORD", KEYWORD)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey("CWT.PROPERTY_KEY", INSTANCE_FIELD)
	@JvmField val OPTION_KEY_KEY = createTextAttributesKey("CWT.OPTION_KEY", DOC_COMMENT_TAG_VALUE)
	@JvmField val NUMBER_KEY = createTextAttributesKey("CWT.NUMBER", NUMBER)
	@JvmField val STRING_KEY = createTextAttributesKey("CWT.STRING", STRING)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey("CWT.VALID_ESCAPE", VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey("CWT.INVALID_ESCAPE", INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey("CWT.BAD_CHARACTER", BAD_CHARACTER)
}
