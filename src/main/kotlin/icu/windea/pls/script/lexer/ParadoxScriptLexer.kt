package icu.windea.pls.script.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

class ParadoxScriptLexer: MergingLexerAdapter(FlexAdapter(_ParadoxScriptLexer()), ParadoxScriptTokenSets.TOKENS_TO_MERGE)
