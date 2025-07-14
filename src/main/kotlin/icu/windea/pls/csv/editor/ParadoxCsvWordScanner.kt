package icu.windea.pls.csv.editor

import com.intellij.lang.cacheBuilder.*
import icu.windea.pls.csv.lexer.*
import icu.windea.pls.csv.psi.*

class ParadoxCsvWordScanner : DefaultWordsScanner(
    ParadoxCsvLexerFactory.createLexer(),
    ParadoxCsvTokenSets.IDENTIFIER_TOKENS,
    ParadoxCsvTokenSets.COMMENT_TOKENS,
    ParadoxCsvTokenSets.LITERAL_TOKENS
)
