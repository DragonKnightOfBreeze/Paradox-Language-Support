package icu.windea.pls.cwt.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object CwtAttributesKeys {
    @JvmField val BRACES = create("CWT.BRACES", DefaultLanguageHighlighterColors.BRACES)
    @JvmField val OPERATOR = create("CWT.OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val COMMENT = create("CWT.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val OPTION_COMMENT = create("CWT.OPTION_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
    @JvmField val DOC_COMMENT = create("CWT.DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
    @JvmField val KEYWORD = create("CWT.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val PROPERTY_KEY = create("CWT.PROPERTY_KEY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    @JvmField val OPTION_KEY = create("CWT.OPTION_KEY", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE)
    @JvmField val NUMBER = create("CWT.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val STRING = create("CWT.STRING", DefaultLanguageHighlighterColors.STRING)
    @JvmField val VALID_ESCAPE = create("CWT.VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    @JvmField val INVALID_ESCAPE = create("CWT.INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
    @JvmField val BAD_CHARACTER = create("CWT.BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

    private fun create(name: String, fallback: TextAttributesKey? = null): TextAttributesKey {
        if (fallback == null) return TextAttributesKey.createTextAttributesKey(name)
        return TextAttributesKey.createTextAttributesKey(name, fallback)
    }
}
