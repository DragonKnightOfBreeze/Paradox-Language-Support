package icu.windea.pls.localisation.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

@Suppress("unused")
object ParadoxLocalisationTokenSets {
    // used by the lexer
    @JvmField val MERGED_TOKENS = TokenSet.create(TokenType.WHITE_SPACE, PROPERTY_VALUE_TOKEN)
    @JvmField val MERGED_TEXT_TOKENS = TokenSet.create(TokenType.WHITE_SPACE, TEXT_TOKEN, COMMAND_TEXT_TOKEN)

    // used by the parser definition
    @JvmField val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    @JvmField val COMMENTS = TokenSet.create(COMMENT)
    @JvmField val STRING_LITERALS = TokenSet.create(TEXT_TOKEN)

    // used by the word scanner
    @JvmField val IDENTIFIER_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, PARAMETER_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN, COMMAND_TEXT_TOKEN, CONCEPT_NAME_TOKEN, ICON_TOKEN, TEXT_ICON_TOKEN, TEXT_FORMAT_TOKEN)
    @JvmField val COMMENT_TOKENS = TokenSet.create(COMMENT)
    @JvmField val LITERAL_TOKENS = TokenSet.create(TEXT_TOKEN)

    @JvmField val QUOTE_TOKENS = TokenSet.create(LEFT_QUOTE, RIGHT_QUOTE)
    @JvmField val TEXT_OR_QUOTE_TOKENS = TokenSet.create(TEXT_TOKEN, LEFT_QUOTE, RIGHT_QUOTE)
    @JvmField val EXPRESSION_TOKENS = TokenSet.create(COMMAND_TEXT_TOKEN, CONCEPT_NAME_TOKEN)
    @JvmField val INTERPOLATION_TOKENS = TokenSet.create(PARAMETER, COMMAND)
    @JvmField val RICH_TEXT_CONTEXT_TOKENS = TokenSet.create(PROPERTY_LIST, PROPERTY, PROPERTY_VALUE, PROPERTY_VALUE_TOKEN, TEXT_ROOT, COLORFUL_TEXT, PARAMETER, ICON, COMMAND, CONCEPT_COMMAND, CONCEPT_TEXT, TEXT_FORMAT, TEXT_FORMAT_TEXT)
}
