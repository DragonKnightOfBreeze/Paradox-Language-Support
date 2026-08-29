package icu.windea.pls.cwt.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.TokenType
import icu.windea.pls.cwt.psi.CwtElementTypes

class CwtQuoteHandler : SimpleTokenSetQuoteHandler(
    CwtElementTypes.LEFT_QUOTE,
    CwtElementTypes.RIGHT_QUOTE,
    TokenType.BAD_CHARACTER,
)
