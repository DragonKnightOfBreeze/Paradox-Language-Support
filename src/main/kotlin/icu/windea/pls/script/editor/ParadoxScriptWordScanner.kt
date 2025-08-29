package icu.windea.pls.script.editor

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import icu.windea.pls.script.lexer.ParadoxScriptLexerFactory
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

class ParadoxScriptWordScanner : DefaultWordsScanner(
    ParadoxScriptLexerFactory.createLayeredLexer(),
    ParadoxScriptTokenSets.IDENTIFIER_TOKENS,
    ParadoxScriptTokenSets.COMMENT_TOKENS,
    ParadoxScriptTokenSets.LITERAL_TOKENS
)
