package icu.windea.pls.csv.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*
import com.intellij.openapi.editor.HighlighterColors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

object ParadoxCsvAttributesKeys {
    @JvmField val SEPARATOR_KEY = createTextAttributesKey("PARADOX_CSV.SEPARATOR", SEMICOLON)
    @JvmField val COMMENT_KEY = createTextAttributesKey("PARADOX_CSV.COMMENT", LINE_COMMENT)
    @JvmField val HEADER_KEY = createTextAttributesKey("PARADOX_CSV.HEADER", INSTANCE_FIELD)
    @JvmField val KEYWORD_KEY = createTextAttributesKey("PARADOX_CSV.KEYWORD", KEYWORD)
    @JvmField val NUMBER_KEY = createTextAttributesKey("PARADOX_CSV.NUMBER", NUMBER)
    @JvmField val STRING_KEY = createTextAttributesKey("PARADOX_CSV.STRING", STRING)
    @JvmField val VALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_CSV.VALID_ESCAPE", VALID_STRING_ESCAPE)
    @JvmField val INVALID_ESCAPE_KEY = createTextAttributesKey("PARADOX_CSV.INVALID_ESCAPE", INVALID_STRING_ESCAPE)
    @JvmField val BAD_CHARACTER_KEY = createTextAttributesKey("PARADOX_CSV.BAD_CHARACTER", BAD_CHARACTER)

    @JvmField val DEFINITION_REFERENCE_KEY = createTextAttributesKey("PARADOX_CSV.DEFINITION_REFERENCE_KEY", ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY)
    @JvmField val ENUM_VALUE_KEY = createTextAttributesKey("PARADOX_CSV.ENUM_VALUE_KEY", ParadoxScriptAttributesKeys.ENUM_VALUE_KEY)
    @JvmField val COMPLEX_ENUM_VALUE_KEY = createTextAttributesKey("PARADOX_CSV.COMPLEX_ENUM_KEY", ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY)
}
