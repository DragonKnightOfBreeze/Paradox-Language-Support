package icu.windea.pls.localisation.editor

import com.intellij.lang.cacheBuilder.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationWordScanner : DefaultWordsScanner(
    ParadoxLocalisationLexer(),
    ParadoxLocalisationTokenSets.IDENTIFIER_TOKENS,
    ParadoxLocalisationTokenSets.COMMENT_TOKENS,
    ParadoxLocalisationTokenSets.LITERAL_TOKENS
)
