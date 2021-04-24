package com.windea.plugin.idea.pls.script.psi

import com.intellij.lexer.*

class ParadoxScriptLexerAdapter : FlexAdapter(ParadoxScriptLexer(null))
