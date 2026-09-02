package icu.windea.pls.lang.findUsages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import icu.windea.pls.cwt.lexer.CwtLexerFactory
import icu.windea.pls.cwt.psi.CwtTokenSets

class CwtWordScanner : DefaultWordsScanner(
    CwtLexerFactory.createLexer(),
    CwtTokenSets.IDENTIFIER_TOKENS,
    CwtTokenSets.COMMENT_TOKENS,
    CwtTokenSets.LITERAL_TOKENS,
)
