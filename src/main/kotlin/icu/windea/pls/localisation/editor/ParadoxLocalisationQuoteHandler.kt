package icu.windea.pls.localisation.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationQuoteHandler : SimpleTokenSetQuoteHandler(RICH_TEXT, TokenType.BAD_CHARACTER)
