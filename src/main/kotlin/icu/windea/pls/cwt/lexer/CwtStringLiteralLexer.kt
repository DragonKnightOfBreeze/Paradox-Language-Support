package icu.windea.pls.cwt.lexer

import com.intellij.lexer.StringLiteralLexer
import com.intellij.psi.tree.IElementType

class CwtStringLiteralLexer(
    originalLiteralToken: IElementType
): StringLiteralLexer(NO_QUOTE_CHAR, originalLiteralToken, false, "$", false, false)
