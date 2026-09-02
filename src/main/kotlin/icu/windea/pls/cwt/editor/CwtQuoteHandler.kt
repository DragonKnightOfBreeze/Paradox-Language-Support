package icu.windea.pls.cwt.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.TokenType
import icu.windea.pls.cwt.psi.CwtElementTypes

class CwtQuoteHandler : SimpleTokenSetQuoteHandler(
    CwtElementTypes.OPTION_KEY_TOKEN,
    CwtElementTypes.PROPERTY_KEY_TOKEN,
    CwtElementTypes.STRING_TOKEN,
    TokenType.BAD_CHARACTER,
)
