package icu.windea.pls.cwt.lexer

import com.intellij.lexer.*
import com.intellij.psi.tree.*

class CwtStringLiteralLexer(
    originalLiteralToken: IElementType
): StringLiteralLexer(NO_QUOTE_CHAR, originalLiteralToken, false, "$", false, false)
