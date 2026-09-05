package icu.windea.pls.script.lexer

import com.intellij.lexer.FlexAdapter
import icu.windea.pls.model.ParadoxGameType

class ParadoxScriptInlineMathLexer(
    val gameType: ParadoxGameType? = null
) : FlexAdapter(_ParadoxScriptInlineMathLexer(gameType))
