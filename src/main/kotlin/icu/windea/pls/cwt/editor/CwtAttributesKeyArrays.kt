package icu.windea.pls.cwt.editor

import com.intellij.openapi.editor.colors.TextAttributesKey
import icu.windea.pls.cwt.editor.CwtAttributesKeys as K

object CwtAttributesKeyArrays {
    @JvmField
    val BRACES_KEYS = arrayOf(K.BRACES_KEY)
    @JvmField
    val OPERATOR_KEYS = arrayOf(K.OPERATOR_KEY)
    @JvmField
    val COMMENT_KEYS = arrayOf(K.COMMENT_KEY)
    @JvmField
    val OPTION_COMMENT_KEYS = arrayOf(K.OPTION_COMMENT_KEY)
    @JvmField
    val DOC_COMMENT_KEYS = arrayOf(K.DOC_COMMENT_KEY)
    @JvmField
    val KEYWORD_KEYS = arrayOf(K.KEYWORD_KEY)
    @JvmField
    val PROPERTY_KEY_KEYS = arrayOf(K.PROPERTY_KEY_KEY)
    @JvmField
    val OPTION_KEY_KEYS = arrayOf(K.OPTION_KEY_KEY)
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
