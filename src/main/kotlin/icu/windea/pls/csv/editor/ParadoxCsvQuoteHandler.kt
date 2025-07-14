package icu.windea.pls.csv.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*

class ParadoxCsvQuoteHandler : SimpleTokenSetQuoteHandler(COLUMN_TOKEN, TokenType.BAD_CHARACTER)
