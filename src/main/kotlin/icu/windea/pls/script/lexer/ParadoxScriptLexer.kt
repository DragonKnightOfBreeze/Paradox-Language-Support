package icu.windea.pls.script.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.MergingLexerAdapter
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptTokenSets

class ParadoxScriptLexer(
    val gameType: ParadoxGameType? = null // NOTE 3.0.2 unused (so not passed) atm
) : MergingLexerAdapter(FlexAdapter(_ParadoxScriptLexer(gameType)), ParadoxScriptTokenSets.MERGED_TOKENS)
