package icu.windea.pls.cwt.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.cwt.psi.CwtElementTypes.BLOCK
import icu.windea.pls.cwt.psi.CwtElementTypes.COMMENT
import icu.windea.pls.cwt.psi.CwtElementTypes.DOC_COMMENT_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.OPTION_COMMENT_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.OPTION_KEY_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.PROPERTY
import icu.windea.pls.cwt.psi.CwtElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.ROOT_BLOCK
import icu.windea.pls.cwt.psi.CwtElementTypes.STRING_TOKEN

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

    @JvmField
    val MEMBER_CONTEXT = TokenSet.create(PROPERTY, ROOT_BLOCK, BLOCK)
}
