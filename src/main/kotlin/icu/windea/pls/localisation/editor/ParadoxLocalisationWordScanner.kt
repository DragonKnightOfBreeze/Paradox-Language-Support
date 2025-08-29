package icu.windea.pls.localisation.editor

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import icu.windea.pls.localisation.lexer.ParadoxLocalisationLexerFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets

class ParadoxLocalisationWordScanner : DefaultWordsScanner(
    ParadoxLocalisationLexerFactory.createLayeredLexer(),
    ParadoxLocalisationTokenSets.IDENTIFIER_TOKENS,
    ParadoxLocalisationTokenSets.COMMENT_TOKENS,
    ParadoxLocalisationTokenSets.LITERAL_TOKENS
)
