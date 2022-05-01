package icu.windea.pls.cwt.highlighter

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import icu.windea.pls.*

object CwtAttributesKeys {
	@JvmField val SEPARATOR_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.separator"), OPERATION_SIGN)
	@JvmField val BRACES_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.braces"), BRACES)
	@JvmField val PROPERTY_KEY_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.propertyKey"), INSTANCE_FIELD)
	@JvmField val OPTION_KEY_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.optionKey"), DOC_COMMENT_TAG_VALUE)
	@JvmField val KEYWORD_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.keyword"), KEYWORD)
	@JvmField val NUMBER_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.number"), NUMBER)
	@JvmField val STRING_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.string"), STRING)
	@JvmField val COMMENT_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.comment"), LINE_COMMENT)
	@JvmField val OPTION_COMMENT_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.optionComment"), DOC_COMMENT)
	@JvmField val DOCUMENTATION_COMMENT_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.documentationComment"), DOC_COMMENT)
	@JvmField val VALID_ESCAPE_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.validEscape"), VALID_STRING_ESCAPE)
	@JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.invalidEscape"), INVALID_STRING_ESCAPE)
	@JvmField val BAD_CHARACTER_KEY = createTextAttributesKey(PlsBundle.message("cwt.externalName.badCharacter"), BAD_CHARACTER)
}

