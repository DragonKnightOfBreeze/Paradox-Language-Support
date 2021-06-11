package icu.windea.pls.script.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.ParadoxScriptTypes.*

class ParadoxScriptQuoteHandler: SimpleTokenSetQuoteHandler(PROPERTY_KEY_ID, QUOTED_PROPERTY_KEY_ID,STRING_TOKEN, QUOTED_STRING_TOKEN, TokenType.BAD_CHARACTER)
