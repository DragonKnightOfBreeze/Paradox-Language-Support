package icu.windea.pls.csv.lexer

import com.intellij.lexer.*
import icu.windea.pls.csv.psi.*

class ParadoxCsvLexer : MergingLexerAdapter(FlexAdapter(_ParadoxCsvLexer()), ParadoxCsvTokenSets.TOKENS_TO_MERGE)
