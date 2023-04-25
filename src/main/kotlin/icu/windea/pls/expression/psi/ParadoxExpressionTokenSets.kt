package icu.windea.pls.expression.psi

import com.intellij.psi.*
import com.intellij.psi.tree.*

object ParadoxExpressionTokenSets {
	@JvmField val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
	@JvmField val COMMENTS = TokenSet.create()
	@JvmField val STRING_LITERALS = TokenSet.create()
}
