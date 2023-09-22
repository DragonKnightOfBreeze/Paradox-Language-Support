package icu.windea.pls.script.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptQuoteHandler : SimpleTokenSetQuoteHandler(PROPERTY_KEY_TOKEN, STRING_TOKEN, TokenType.BAD_CHARACTER)
