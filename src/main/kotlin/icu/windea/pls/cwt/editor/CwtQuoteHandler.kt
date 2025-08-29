package icu.windea.pls.cwt.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.TokenType
import icu.windea.pls.cwt.psi.CwtElementTypes.OPTION_KEY_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.PROPERTY_KEY_TOKEN
import icu.windea.pls.cwt.psi.CwtElementTypes.STRING_TOKEN

class CwtQuoteHandler : SimpleTokenSetQuoteHandler(PROPERTY_KEY_TOKEN, OPTION_KEY_TOKEN, STRING_TOKEN, TokenType.BAD_CHARACTER)
