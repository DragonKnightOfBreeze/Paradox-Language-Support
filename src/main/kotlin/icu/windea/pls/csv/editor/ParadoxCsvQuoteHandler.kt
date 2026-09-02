package icu.windea.pls.csv.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.TokenType
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes

class ParadoxCsvQuoteHandler : SimpleTokenSetQuoteHandler(
    ParadoxCsvElementTypes.COLUMN_TOKEN,
    TokenType.BAD_CHARACTER,
)
