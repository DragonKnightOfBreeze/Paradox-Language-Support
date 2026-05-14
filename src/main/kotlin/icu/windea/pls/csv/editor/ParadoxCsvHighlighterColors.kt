package icu.windea.pls.csv.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors

object ParadoxCsvHighlighterColors {
    @JvmField val SEPARATOR = createTextAttributesKey("PARADOX_CSV.SEPARATOR", DefaultLanguageHighlighterColors.SEMICOLON)
    @JvmField val COMMENT = createTextAttributesKey("PARADOX_CSV.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val HEADER = createTextAttributesKey("PARADOX_CSV.HEADER", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    @JvmField val KEYWORD = createTextAttributesKey("PARADOX_CSV.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val NUMBER = createTextAttributesKey("PARADOX_CSV.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val STRING = createTextAttributesKey("PARADOX_CSV.STRING", DefaultLanguageHighlighterColors.STRING)
    @JvmField val VALID_ESCAPE = createTextAttributesKey("PARADOX_CSV.VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    @JvmField val INVALID_ESCAPE = createTextAttributesKey("PARADOX_CSV.INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
    @JvmField val BAD_CHARACTER = createTextAttributesKey("PARADOX_CSV.BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

    @JvmField val DEFINITION_REFERENCE = createTextAttributesKey("PARADOX_CSV.DEFINITION_REFERENCE_KEY", ParadoxScriptHighlighterColors.DEFINITION_REFERENCE)
    @JvmField val ENUM_VALUE = createTextAttributesKey("PARADOX_CSV.ENUM_VALUE_KEY", ParadoxScriptHighlighterColors.ENUM_VALUE)
    @JvmField val COMPLEX_ENUM_VALUE = createTextAttributesKey("PARADOX_CSV.COMPLEX_ENUM_KEY", ParadoxScriptHighlighterColors.COMPLEX_ENUM_VALUE)
    @JvmField val DYNAMIC_VALUE = createTextAttributesKey("PARADOX_CSV.DYNAMIC_VALUE", ParadoxScriptHighlighterColors.DYNAMIC_VALUE)
    @JvmField val VARIABLE = createTextAttributesKey("PARADOX_CSV.VARIABLE", ParadoxScriptHighlighterColors.VARIABLE)
}
