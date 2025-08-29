package icu.windea.pls.cwt.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.DOC_COMMENT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LINE_COMMENT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.OPERATION_SIGN
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
import com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

object CwtAttributesKeys {
    @JvmField
    val BRACES_KEY = createTextAttributesKey("CWT.BRACES", BRACES)
    @JvmField
    val OPERATOR_KEY = createTextAttributesKey("CWT.OPERATOR", OPERATION_SIGN)
    @JvmField
    val COMMENT_KEY = createTextAttributesKey("CWT.COMMENT", LINE_COMMENT)
    @JvmField
    val OPTION_COMMENT_KEY = createTextAttributesKey("CWT.OPTION_COMMENT", DOC_COMMENT)
    @JvmField
    val DOC_COMMENT_KEY = createTextAttributesKey("CWT.DOC_COMMENT", DOC_COMMENT)
    @JvmField
    val KEYWORD_KEY = createTextAttributesKey("CWT.KEYWORD", KEYWORD)
    @JvmField
    val PROPERTY_KEY_KEY = createTextAttributesKey("CWT.PROPERTY_KEY", INSTANCE_FIELD)
    @JvmField
    val OPTION_KEY_KEY = createTextAttributesKey("CWT.OPTION_KEY", DOC_COMMENT_TAG_VALUE)
    @JvmField
    val NUMBER_KEY = createTextAttributesKey("CWT.NUMBER", NUMBER)
    @JvmField
    val STRING_KEY = createTextAttributesKey("CWT.STRING", STRING)
    @JvmField
    val VALID_ESCAPE_KEY = createTextAttributesKey("CWT.VALID_ESCAPE", VALID_STRING_ESCAPE)
    @JvmField
    val INVALID_ESCAPE_KEY = createTextAttributesKey("CWT.INVALID_ESCAPE", INVALID_STRING_ESCAPE)
    @JvmField
    val BAD_CHARACTER_KEY = createTextAttributesKey("CWT.BAD_CHARACTER", BAD_CHARACTER)
}
