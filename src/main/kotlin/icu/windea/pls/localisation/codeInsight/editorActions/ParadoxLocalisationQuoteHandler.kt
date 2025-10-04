package icu.windea.pls.localisation.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.TokenType
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes

class ParadoxLocalisationQuoteHandler : SimpleTokenSetQuoteHandler(
    ParadoxLocalisationElementTypes.LEFT_QUOTE,
    ParadoxLocalisationElementTypes.RIGHT_QUOTE,
    ParadoxLocalisationElementTypes.LEFT_SINGLE_QUOTE,
    ParadoxLocalisationElementTypes.RIGHT_SINGLE_QUOTE, TokenType.BAD_CHARACTER)
