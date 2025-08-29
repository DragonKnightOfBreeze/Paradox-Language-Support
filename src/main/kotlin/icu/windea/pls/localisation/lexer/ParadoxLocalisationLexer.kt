package icu.windea.pls.localisation.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets

class ParadoxLocalisationLexer: MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationLexer()), ParadoxLocalisationTokenSets.TOKENS_TO_MERGE)
