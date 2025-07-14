package icu.windea.pls.csv.editor

import com.intellij.openapi.editor.colors.*
import icu.windea.pls.csv.editor.ParadoxCsvAttributesKeys as K

object ParadoxCsvAttributesKeyArrays {
    @JvmField
    val SEPARATOR_KEYS = arrayOf(K.SEPARATOR_KEY)
    @JvmField
    val COMMENT_KEYS = arrayOf(K.COMMENT_KEY)
    @JvmField
    val KEYWORD_KEYS = arrayOf(K.KEYWORD_KEY)
    @JvmField
    val NUMBER_KEYS = arrayOf(K.NUMBER_KEY)
    @JvmField
    val STRING_KEYS = arrayOf(K.STRING_KEY)
    @JvmField
    val VALID_ESCAPE_KEYS = arrayOf(K.VALID_ESCAPE_KEY)
    @JvmField
    val INVALID_ESCAPE_KEYS = arrayOf(K.INVALID_ESCAPE_KEY)
    @JvmField
    val BAD_CHARACTER_KEYS = arrayOf(K.BAD_CHARACTER_KEY)
    @JvmField
    val EMPTY_KEYS = TextAttributesKey.EMPTY_ARRAY
}
