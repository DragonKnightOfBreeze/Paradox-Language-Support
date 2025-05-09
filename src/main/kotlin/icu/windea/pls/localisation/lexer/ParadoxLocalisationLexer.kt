package icu.windea.pls.localisation.lexer

import com.intellij.lexer.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationLexer: MergingLexerAdapter(FlexAdapter(_ParadoxLocalisationLexer()), ParadoxLocalisationTokenSets.TOKENS_TO_MERGE)
