package icu.windea.pls.cwt.psi

import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

object CwtTokenSets {
    @JvmField
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    @JvmField
    val COMMENTS = TokenSet.create(COMMENT) //DO NOT add DOC_COMMENT here
    @JvmField
    val STRING_LITERALS = TokenSet.create(STRING_TOKEN)

    @JvmField
    val IDENTIFIER_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN)
    @JvmField
    val COMMENT_TOKENS = TokenSet.create(COMMENT, OPTION_COMMENT_TOKEN, DOC_COMMENT_TOKEN)
    @JvmField
    val LITERAL_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN)

    @JvmField
    val STRING_TOKENS = TokenSet.create(STRING_TOKEN)
    @JvmField
    val KEY_OR_STRING_TOKENS = TokenSet.create(OPTION_KEY_TOKEN, PROPERTY_KEY_TOKEN, STRING_TOKEN)
}
