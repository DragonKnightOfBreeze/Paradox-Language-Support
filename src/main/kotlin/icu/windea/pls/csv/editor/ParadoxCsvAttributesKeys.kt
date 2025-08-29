package icu.windea.pls.csv.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LINE_COMMENT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.SEMICOLON
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
import com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

object ParadoxCsvAttributesKeys {
    @JvmField
    val SEPARATOR_KEY = createTextAttributesKey("PARADOX_CSV.SEPARATOR", SEMICOLON)
    @JvmField
    val COMMENT_KEY = createTextAttributesKey("PARADOX_CSV.COMMENT", LINE_COMMENT)
    @JvmField
    val HEADER_KEY = createTextAttributesKey("PARADOX_CSV.HEADER", INSTANCE_FIELD)
    @JvmField
    val KEYWORD_KEY = createTextAttributesKey("PARADOX_CSV.KEYWORD", KEYWORD)
    @JvmField
    val NUMBER_KEY = createTextAttributesKey("PARADOX_CSV.NUMBER", NUMBER)
    @JvmField
    val STRING_KEY = createTextAttributesKey("PARADOX_CSV.STRING", STRING)
    @JvmField
    val VALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_CSV.VALID_ESCAPE", VALID_STRING_ESCAPE)
    @JvmField
    val INVALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_CSV.INVALID_ESCAPE", INVALID_STRING_ESCAPE)
    @JvmField
    val BAD_CHARACTER_KEY = createTextAttributesKey("PARADOX_CSV.BAD_CHARACTER", BAD_CHARACTER)
}
