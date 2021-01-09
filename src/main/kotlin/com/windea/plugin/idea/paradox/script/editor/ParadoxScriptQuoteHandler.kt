package com.windea.plugin.idea.paradox.script.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptQuoteHandler: SimpleTokenSetQuoteHandler(ParadoxScriptTypes.STRING_TOKEN, ParadoxScriptTypes.QUOTED_STRING_TOKEN, TokenType.BAD_CHARACTER)
