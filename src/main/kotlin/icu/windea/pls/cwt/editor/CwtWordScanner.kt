package icu.windea.pls.cwt.editor

import com.intellij.lang.cacheBuilder.*
import icu.windea.pls.cwt.lexer.*
import icu.windea.pls.cwt.psi.*

class CwtWordScanner : DefaultWordsScanner(
    CwtLexerFactory.createLexer(), //it's unnecessary to use CwtLexerFactory.createLayeredLexer() here
    CwtTokenSets.IDENTIFIER_TOKENS,
    CwtTokenSets.COMMENT_TOKENS,
    CwtTokenSets.LITERAL_TOKENS
)
