package icu.windea.pls.cwt.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes

@Suppress("unused")
object CwtTokenSets {
    @JvmField val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE, EOL)
    @JvmField val COMMENTS = TokenSet.create(COMMENT) // DO NOT add DOC_COMMENT here
    @JvmField val STRING_LITERALS = TokenSet.create(STRING_TOKEN)

    @JvmField val IDENTIFIER_TOKENS = TokenSet.create(OPTION_KEY_TOKEN, PROPERTY_KEY_TOKEN, STRING_TOKEN)
    @JvmField val COMMENT_TOKENS = TokenSet.create(COMMENT, DOC_COMMENT_TOKEN)
    @JvmField val LITERAL_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN)

    @JvmField val MERGED_TOKENS = TokenSet.create(TokenType.WHITE_SPACE/*not possible:*//*, OPTION_KEY_TOKEN, PROPERTY_KEY_TOKEN*/, STRING_TOKEN)
    @JvmField val QUOTE_TOKENS = TokenSet.create(LEFT_QUOTE, RIGHT_QUOTE)

    @JvmField val STRING_TOKENS = TokenSet.create(STRING_TOKEN)
    @JvmField val KEY_OR_STRING_TOKENS = TokenSet.create(OPTION_KEY_TOKEN, PROPERTY_KEY_TOKEN, STRING_TOKEN)
    @JvmField val STRING_EXPRESSION_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN)
    @JvmField val MEMBER_CONTEXT_TOKENS = TokenSet.create(PROPERTY, ROOT_BLOCK, BLOCK)

    @JvmField val PROPERTY_SEPARATOR_TOKENS = TokenSet.create(EQUAL_SIGN, NOT_EQUAL_SIGN, DOUBLE_EQUAL_SIGN)
}
