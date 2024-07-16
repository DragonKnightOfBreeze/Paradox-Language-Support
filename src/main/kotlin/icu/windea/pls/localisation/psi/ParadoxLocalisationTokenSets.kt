package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

object ParadoxLocalisationTokenSets {
	@JvmField val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
	@JvmField val COMMENTS = TokenSet.create(COMMENT)
	@JvmField val STRING_LITERALS = TokenSet.create(STRING_TOKEN)
	
	@JvmField val IDENTIFIER_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, PROPERTY_REFERENCE_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN, ICON_TOKEN, COMMAND_SCOPE_TOKEN, COMMAND_FIELD_TOKEN, CONCEPT_NAME_TOKEN)
	@JvmField val COMMENT_TOKENS = TokenSet.create(COMMENT)
	@JvmField val LITERAL_TOKENS = TokenSet.create(STRING_TOKEN)
    
    @JvmField val EXPRESSION_TOKENS = TokenSet.create(CONCEPT_NAME_TOKEN)
}
