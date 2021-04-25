package com.windea.plugin.idea.pls.cwt.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.cwt.psi.*
import com.windea.plugin.idea.pls.cwt.psi.CwtTypes.*

class CwtQuoteHandler:SimpleTokenSetQuoteHandler(KEY_TOKEN, QUOTED_KEY_TOKEN, STRING_TOKEN, QUOTED_STRING_TOKEN, TokenType.BAD_CHARACTER)