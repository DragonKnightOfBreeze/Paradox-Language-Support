package icu.windea.pls.lang.findUsages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import icu.windea.pls.script.lexer.ParadoxScriptLexerFactory
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

class ParadoxScriptWordScanner : DefaultWordsScanner(
    ParadoxScriptLexerFactory.createLayeredLexer(), // 3.0.2 layered lexer should be used here
    ParadoxScriptTokenSets.IDENTIFIER_TOKENS,
    ParadoxScriptTokenSets.COMMENT_TOKENS,
    ParadoxScriptTokenSets.LITERAL_TOKENS,
)
