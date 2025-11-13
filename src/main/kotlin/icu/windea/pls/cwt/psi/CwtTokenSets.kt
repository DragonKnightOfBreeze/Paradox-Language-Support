package icu.windea.pls.cwt.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.cwt.psi.CwtElementTypes.*

object CwtTokenSets {
    @JvmField
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE, EOL)
    @JvmField
    val COMMENTS = TokenSet.create(COMMENT) // DO NOT add DOC_COMMENT here
    @JvmField
    val STRING_LITERALS = TokenSet.create(STRING_TOKEN)

    @JvmField
    val IDENTIFIER_TOKENS = TokenSet.create(OPTION_KEY_TOKEN, PROPERTY_KEY_TOKEN, STRING_TOKEN)
    @JvmField
    val COMMENT_TOKENS = TokenSet.create(COMMENT, DOC_COMMENT_TOKEN)
    @JvmField
    val LITERAL_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN)

    @JvmField
    val STRING_TOKENS = TokenSet.create(STRING_TOKEN)
    @JvmField
    val KEY_OR_STRING_TOKENS = TokenSet.create(OPTION_KEY_TOKEN, PROPERTY_KEY_TOKEN, STRING_TOKEN)

    @JvmField
    val MEMBER_CONTEXT = TokenSet.create(PROPERTY, ROOT_BLOCK, BLOCK)

    @JvmField
    val TOKENS_TO_MERGE = TokenSet.create(TokenType.WHITE_SPACE)
}
