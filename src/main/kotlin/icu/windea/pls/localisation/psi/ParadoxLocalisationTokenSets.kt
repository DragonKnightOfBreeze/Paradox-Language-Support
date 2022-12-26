package icu.windea.pls.localisation.psi

import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

object ParadoxLocalisationTokenSets {
	@JvmField val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
	@JvmField val COMMENTS = TokenSet.create(COMMENT)
	@JvmField val STRING_LITERALS = TokenSet.create(STRING_TOKEN)
	
	@JvmField val IDENTIFIER_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, PROPERTY_REFERENCE_ID, SCRIPTED_VARIABLE_REFERENCE_ID, ICON_ID, COMMAND_SCOPE_ID, COMMAND_FIELD_ID, STELLARIS_NAME_FORMAT_ID)
	@JvmField val COMMENT_TOKENS = TokenSet.create(COMMENT)
	@JvmField val LITERAL_TOKENS = TokenSet.create(STRING_TOKEN)
}
