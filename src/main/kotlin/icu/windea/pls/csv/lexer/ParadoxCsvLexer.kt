package icu.windea.pls.csv.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.csv.psi.ParadoxCsvTokenSets

class ParadoxCsvLexer : MergingLexerAdapter(FlexAdapter(_ParadoxCsvLexer()), ParadoxCsvTokenSets.TOKENS_TO_MERGE)
