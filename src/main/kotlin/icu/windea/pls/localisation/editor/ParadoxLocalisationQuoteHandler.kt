package icu.windea.pls.localisation.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.TokenType
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.LEFT_SINGLE_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.RIGHT_QUOTE
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.RIGHT_SINGLE_QUOTE

class ParadoxLocalisationQuoteHandler : SimpleTokenSetQuoteHandler(LEFT_QUOTE, RIGHT_QUOTE, LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE, TokenType.BAD_CHARACTER)
