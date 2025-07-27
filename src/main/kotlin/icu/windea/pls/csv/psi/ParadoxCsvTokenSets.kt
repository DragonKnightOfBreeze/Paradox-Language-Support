package icu.windea.pls.csv.psi

import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

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
}
