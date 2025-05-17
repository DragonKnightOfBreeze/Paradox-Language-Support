package icu.windea.pls.script.lexer

import com.intellij.lexer.*
import com.intellij.psi.tree.*

class ParadoxScriptStringLiteralLexer(
    originalLiteralToken: IElementType
): StringLiteralLexer(NO_QUOTE_CHAR, originalLiteralToken, false, "$", false, false)
