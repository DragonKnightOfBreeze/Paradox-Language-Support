package icu.windea.pls.script.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.AT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.BLOCK
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.BOOLEAN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.BOOLEAN_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.COLOR
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.COMMENT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.CONDITION_PARAMETER_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.EQUAL_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.FLOAT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.FLOAT_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.GE_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.GT_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INLINE_MATH
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INT
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.INT_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LE_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.LT_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.NOT_EQUAL_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PARAMETER_CONDITION
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PARAMETER_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.ROOT_BLOCK
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SAFE_EQUAL_SIGN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE_NAME_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE_REFERENCE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.STRING
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.STRING_TOKEN

object ParadoxScriptTokenSets {
    @JvmField
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    @JvmField
    val COMMENTS = TokenSet.create(COMMENT)
    @JvmField
    val STRING_LITERALS = TokenSet.create(STRING_TOKEN)

    @JvmField
    val IDENTIFIER_TOKENS = TokenSet.create(SCRIPTED_VARIABLE_NAME_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN, PROPERTY_KEY_TOKEN, STRING_TOKEN, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN, CONDITION_PARAMETER_TOKEN, PARAMETER_TOKEN)
    @JvmField
    val COMMENT_TOKENS = TokenSet.create(COMMENT)
    @JvmField
    val LITERAL_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN)

    @JvmField
    val COMPARISON_TOKENS = TokenSet.create(LT_SIGN, GT_SIGN, LE_SIGN, GE_SIGN, NOT_EQUAL_SIGN)
    @JvmField
    val SCRIPTED_VARIABLE_VALUE_TOKENS = TokenSet.create(BOOLEAN_TOKEN, INT_TOKEN, FLOAT_TOKEN, STRING_TOKEN)
    @JvmField
    val VARIABLE_VALUE_TOKENS = TokenSet.create(INT_TOKEN, FLOAT_TOKEN, STRING_TOKEN)

    @JvmField
    val SCRIPTED_VARIABLE_NAME_TOKENS = TokenSet.create(SCRIPTED_VARIABLE_NAME_TOKEN)
    @JvmField
    val SCRIPTED_VARIABLE_REFERENCE_TOKENS = TokenSet.create(SCRIPTED_VARIABLE_REFERENCE_TOKEN, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    @JvmField
    val STRING_TOKENS = TokenSet.create(STRING_TOKEN)
    @JvmField
    val KEY_OR_STRING_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN)
    @JvmField
    val PARAMETER_TOKENS = TokenSet.create(PARAMETER_TOKEN, CONDITION_PARAMETER_TOKEN)

    @JvmField
    val VALUES = TokenSet.create(BOOLEAN, INT, FLOAT, STRING, COLOR, BLOCK, INLINE_MATH, SCRIPTED_VARIABLE_REFERENCE)

    @JvmField
    val SNIPPET_TYPES = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN, SCRIPTED_VARIABLE_NAME_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    @JvmField
    val LEFT_SNIPPET_TYPES = TokenSet.create(AT, SCRIPTED_VARIABLE_NAME_TOKEN, PROPERTY_KEY_TOKEN)
    @JvmField
    val RIGHT_SNIPPET_TYPES = TokenSet.create(STRING_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN)
    @JvmField
    val BREAK_SNIPPET_TYPES = TokenSet.create(TokenType.WHITE_SPACE, COMMENT)

    @JvmField
    val PROPERTY_SEPARATOR_TOKENS = TokenSet.create(EQUAL_SIGN, LT_SIGN, GT_SIGN, LE_SIGN, GE_SIGN, NOT_EQUAL_SIGN, SAFE_EQUAL_SIGN)

    @JvmField
    val BLOCK_OR_ROOT_BLOCK = TokenSet.create(BLOCK, ROOT_BLOCK)

    @JvmField
    val TOKENS_TO_MERGE = TokenSet.create(TokenType.WHITE_SPACE, PROPERTY_KEY_TOKEN, STRING_TOKEN, SCRIPTED_VARIABLE_NAME_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN)

    @JvmField
    val MEMBER_CONTEXT = TokenSet.create(PROPERTY, ROOT_BLOCK, BLOCK, PARAMETER_CONDITION)
}
