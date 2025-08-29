package icu.windea.pls.localisation.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets
import icu.windea.pls.model.ParadoxGameType

class ParadoxLocalisationTextLexer(
    gameType: ParadoxGameType? = null
): MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationTextLexer(gameType)), ParadoxLocalisationTokenSets.TEXT_TOKENS_TO_MERGE)
