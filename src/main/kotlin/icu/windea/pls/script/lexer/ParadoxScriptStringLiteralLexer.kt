package icu.windea.pls.script.lexer

import com.intellij.lexer.StringLiteralLexer
import com.intellij.psi.tree.IElementType

class ParadoxScriptStringLiteralLexer(
    originalLiteralToken: IElementType
): StringLiteralLexer(NO_QUOTE_CHAR, originalLiteralToken, false, "$", false, false)
