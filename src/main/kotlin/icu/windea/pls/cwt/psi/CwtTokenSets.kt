package icu.windea.pls.cwt.psi

import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

object CwtTokenSets {
	@JvmField val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
	//DO NOT add DOCUMENTATION_TOKEN here, or documentation comment text will be resolved to PsiComment
	@JvmField val COMMENTS = TokenSet.create(COMMENT)
	@JvmField val STRING_LITERALS = TokenSet.create(STRING_TOKEN)
	
	@JvmField val IDENTIFIER_TOKENS = TokenSet.create(OPTION_KEY_TOKEN, PROPERTY_KEY_TOKEN, STRING_TOKEN)
	@JvmField val COMMENT_TOKENS = TokenSet.create(COMMENT, DOCUMENTATION_TOKEN)
	@JvmField val LITERAL_TOKENS = TokenSet.EMPTY
    
    @JvmField val STRING_TOKENS = TokenSet.create(STRING_TOKEN)
    @JvmField val KEY_OR_STRING_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, STRING_TOKEN)
}
