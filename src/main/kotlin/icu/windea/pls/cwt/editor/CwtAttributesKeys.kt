package icu.windea.pls.cwt.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.*

object CwtAttributesKeys {
    @JvmField val BRACES = createTextAttributesKey("CWT.BRACES", DefaultLanguageHighlighterColors.BRACES)
    @JvmField val OPERATOR = createTextAttributesKey("CWT.OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val COMMENT = createTextAttributesKey("CWT.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val OPTION_COMMENT = createTextAttributesKey("CWT.OPTION_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
    @JvmField val DOC_COMMENT = createTextAttributesKey("CWT.DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
    @JvmField val KEYWORD = createTextAttributesKey("CWT.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val PROPERTY_KEY = createTextAttributesKey("CWT.PROPERTY_KEY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    @JvmField val OPTION_KEY = createTextAttributesKey("CWT.OPTION_KEY", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE)
    @JvmField val NUMBER = createTextAttributesKey("CWT.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val STRING = createTextAttributesKey("CWT.STRING", DefaultLanguageHighlighterColors.STRING)
    @JvmField val VALID_ESCAPE = createTextAttributesKey("CWT.VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    @JvmField val INVALID_ESCAPE = createTextAttributesKey("CWT.INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
    @JvmField val BAD_CHARACTER = createTextAttributesKey("CWT.BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)
}
