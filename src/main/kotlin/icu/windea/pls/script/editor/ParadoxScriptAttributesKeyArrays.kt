package icu.windea.pls.script.editor

import com.intellij.openapi.editor.colors.TextAttributesKey
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys as K

object ParadoxScriptAttributesKeyArrays {
    @JvmField
    val BRACES_KEYS = arrayOf(K.BRACES_KEY)
    @JvmField
    val OPERATOR_KEYS = arrayOf(K.OPERATOR_KEY)
    @JvmField
    val MARKER_KEYS = arrayOf(K.MARKER_KEY)
    @JvmField
    val PARAMETER_CONDITION_BRACKETS_KEYS = arrayOf(K.PARAMETER_CONDITION_BRACKETS_KEYS)
    @JvmField
    val PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS = arrayOf(K.PARAMETER_CONDITION_EXPRESSION_BRACKETS_KEYS)
    @JvmField
    val INLINE_MATH_BRACES_KEYS = arrayOf(K.INLINE_MATH_BRACES_KEY)
    @JvmField
    val INLINE_MATH_OPERATOR_KEYS = arrayOf(K.INLINE_MATH_OPERATOR_KEY)
    @JvmField
    val KEYWORD_KEYS = arrayOf(K.KEYWORD_KEY)
    @JvmField
    val COMMENT_KEYS = arrayOf(K.COMMENT_KEY)
    @JvmField
    val SCRIPTED_VARIABLE_KEYS = arrayOf(K.SCRIPTED_VARIABLE_KEY)
    @JvmField
    val PARAMETER_KEYS = arrayOf(K.PARAMETER_KEY)
    @JvmField
    val CONDITION_PARAMETER_KEYS = arrayOf(K.CONDITION_PARAMETER_KEY)
    @JvmField
    val PROPERTY_KEY_KEYS = arrayOf(K.PROPERTY_KEY_KEY)
    @JvmField
    val COLOR_KEYS = arrayOf(K.COLOR_KEY)
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
