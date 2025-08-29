package icu.windea.pls.csv.editor

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import icu.windea.pls.csv.lexer.ParadoxCsvLexerFactory
import icu.windea.pls.csv.psi.ParadoxCsvTokenSets

class ParadoxCsvWordScanner : DefaultWordsScanner(
    ParadoxCsvLexerFactory.createLexer(),
    ParadoxCsvTokenSets.IDENTIFIER_TOKENS,
    ParadoxCsvTokenSets.COMMENT_TOKENS,
    ParadoxCsvTokenSets.LITERAL_TOKENS
)
