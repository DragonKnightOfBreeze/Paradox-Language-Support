package icu.windea.pls.csv.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

object ParadoxCsvAttributesKeys {
    @JvmField val SEPARATOR = create("PARADOX_CSV.SEPARATOR", DefaultLanguageHighlighterColors.SEMICOLON)
    @JvmField val COMMENT = create("PARADOX_CSV.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val HEADER = create("PARADOX_CSV.HEADER", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    @JvmField val KEYWORD = create("PARADOX_CSV.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val NUMBER = create("PARADOX_CSV.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val STRING = create("PARADOX_CSV.STRING", DefaultLanguageHighlighterColors.STRING)
    @JvmField val VALID_ESCAPE = create("PARADOX_CSV.VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    @JvmField val INVALID_ESCAPE = create("PARADOX_CSV.INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
    @JvmField val BAD_CHARACTER = create("PARADOX_CSV.BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

    @JvmField val DEFINITION_REFERENCE = create("PARADOX_CSV.DEFINITION_REFERENCE_KEY", ParadoxScriptAttributesKeys.DEFINITION_REFERENCE)
    @JvmField val ENUM_VALUE = create("PARADOX_CSV.ENUM_VALUE_KEY", ParadoxScriptAttributesKeys.ENUM_VALUE)
    @JvmField val COMPLEX_ENUM_VALUE = create("PARADOX_CSV.COMPLEX_ENUM_KEY", ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE)

    private fun create(name: String, fallback: TextAttributesKey? = null): TextAttributesKey {
        if (fallback == null) return TextAttributesKey.createTextAttributesKey(name)
        return TextAttributesKey.createTextAttributesKey(name, fallback)
    }
}
