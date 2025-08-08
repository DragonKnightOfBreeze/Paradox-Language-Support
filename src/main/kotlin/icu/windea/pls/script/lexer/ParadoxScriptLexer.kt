package icu.windea.pls.script.lexer

import com.intellij.lexer.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.*

class ParadoxScriptLexer: MergingLexerAdapter(FlexAdapter(_ParadoxScriptLexer()), ParadoxScriptTokenSets.TOKENS_TO_MERGE)
