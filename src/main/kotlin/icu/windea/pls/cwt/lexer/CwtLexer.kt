package icu.windea.pls.cwt.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.cwt.psi.CwtTokenSets

class CwtLexer : MergingLexerAdapter(FlexAdapter(_CwtLexer(null)), CwtTokenSets.TOKENS_TO_MERGE)
