package icu.windea.pls.csv.lexer

import com.intellij.lexer.StringLiteralLexer
import com.intellij.psi.tree.IElementType

class ParadoxCsvStringLiteralLexer(
    originalLiteralToken: IElementType
): StringLiteralLexer(NO_QUOTE_CHAR, originalLiteralToken, false, "", false, false)
