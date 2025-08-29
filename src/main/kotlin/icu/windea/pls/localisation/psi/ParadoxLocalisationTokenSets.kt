package icu.windea.pls.localisation.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COLORFUL_TEXT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMAND
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMAND_TEXT_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMENT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.CONCEPT_COMMAND
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.CONCEPT_NAME_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.CONCEPT_TEXT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_LIST
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_VALUE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY_VALUE_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.RIGHT_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.SCRIPTED_VARIABLE_REFERENCE_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.STRING_TOKEN
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_FORMAT_TEXT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.TEXT_ROOT

object ParadoxLocalisationTokenSets {
    @JvmField
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    @JvmField
    val COMMENTS = TokenSet.create(COMMENT)
    @JvmField
    val STRING_LITERALS = TokenSet.create(STRING_TOKEN)

    @JvmField
    val IDENTIFIER_TOKENS = TokenSet.create(PROPERTY_KEY_TOKEN, PARAMETER_TOKEN, SCRIPTED_VARIABLE_REFERENCE_TOKEN, ICON_TOKEN, COMMAND_TEXT_TOKEN, CONCEPT_NAME_TOKEN)
    @JvmField
    val COMMENT_TOKENS = TokenSet.create(COMMENT)
    @JvmField
    val LITERAL_TOKENS = TokenSet.create(STRING_TOKEN)

    @JvmField
    val EXPRESSION_TOKENS = TokenSet.create(COMMAND_TEXT_TOKEN, CONCEPT_NAME_TOKEN)
    @JvmField
    val STRING_TOKEN_OR_QUOTE = TokenSet.create(STRING_TOKEN, LEFT_QUOTE, RIGHT_QUOTE)

    @JvmField
    val TOKENS_TO_MERGE = TokenSet.create(TokenType.WHITE_SPACE, PROPERTY_VALUE_TOKEN)
    @JvmField
    val TEXT_TOKENS_TO_MERGE = TokenSet.create(TokenType.WHITE_SPACE, STRING_TOKEN, COMMAND_TEXT_TOKEN)

    @JvmField
    val EXTRA_TEMPLATE_TYPES = TokenSet.create(PARAMETER, COMMAND)
    @JvmField
    val PROPERTY_CONTEXT = TokenSet.create(PROPERTY_LIST)
    @JvmField
    val RICH_TEXT_CONTEXT = TokenSet.create(PROPERTY_LIST, PROPERTY, PROPERTY_VALUE, PROPERTY_VALUE_TOKEN, TEXT_ROOT, COLORFUL_TEXT, PARAMETER, CONCEPT_COMMAND, CONCEPT_TEXT, TEXT_FORMAT, TEXT_FORMAT_TEXT)
}
