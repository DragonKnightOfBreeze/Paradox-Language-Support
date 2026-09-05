package icu.windea.pls.localisation.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets
import icu.windea.pls.model.ParadoxGameType

class ParadoxLocalisationLexer(
    val gameType: ParadoxGameType? = null // NOTE 3.0.2 unused (so the argument is not passed) atm
) : MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationLexer(gameType)), ParadoxLocalisationTokenSets.MERGED_TOKENS)
