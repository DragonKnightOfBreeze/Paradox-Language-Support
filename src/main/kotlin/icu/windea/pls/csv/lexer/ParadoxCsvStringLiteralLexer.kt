package icu.windea.pls.csv.lexer

import com.intellij.lexer.*
import com.intellij.psi.tree.*

class ParadoxCsvStringLiteralLexer(
    originalLiteralToken: IElementType
): StringLiteralLexer(NO_QUOTE_CHAR, originalLiteralToken, false, "", false, false)
