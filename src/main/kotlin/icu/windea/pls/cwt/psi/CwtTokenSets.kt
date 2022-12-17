package icu.windea.pls.cwt.psi

import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

object CwtTokenSets {
	@JvmField val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
	@JvmField val COMMENTS = TokenSet.create(COMMENT, DOCUMENTATION_TOKEN)
	@JvmField val STRING_LITERALS = TokenSet.create(STRING_TOKEN)
	
	@JvmField val IDENTIFIERS = TokenSet.create(PROPERTY_KEY_TOKEN, OPTION_KEY_TOKEN)
	@JvmField val LITERALS = TokenSet.create(STRING_TOKEN)
}