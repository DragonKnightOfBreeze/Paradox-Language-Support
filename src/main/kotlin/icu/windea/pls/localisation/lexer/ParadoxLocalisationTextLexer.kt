package icu.windea.pls.localisation.lexer

import com.intellij.lexer.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

class ParadoxLocalisationTextLexer(
    gameType: ParadoxGameType? = null
): MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationTextLexer(gameType)), ParadoxLocalisationTokenSets.TEXT_TOKENS_TO_MERGE)
