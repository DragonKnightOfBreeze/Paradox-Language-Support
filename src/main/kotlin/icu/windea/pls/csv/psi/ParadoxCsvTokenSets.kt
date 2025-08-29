package icu.windea.pls.csv.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.COLUMN_TOKEN
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.COMMENT
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.EOL

object ParadoxCsvTokenSets {
    @JvmField
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE, EOL)
    @JvmField
    val COMMENTS = TokenSet.create(COMMENT)
    @JvmField
    val STRING_LITERALS = TokenSet.create(COLUMN_TOKEN)

    @JvmField
    val IDENTIFIER_TOKENS = TokenSet.create(COLUMN_TOKEN)
    @JvmField
    val COMMENT_TOKENS = TokenSet.create(COMMENT)
    @JvmField
    val LITERAL_TOKENS = TokenSet.create(COLUMN_TOKEN)

    @JvmField
    val EXPRESSION_TOKENS = TokenSet.create(COLUMN_TOKEN)

    @JvmField
    val TOKENS_TO_MERGE = TokenSet.create(TokenType.WHITE_SPACE, COLUMN_TOKEN)
}
