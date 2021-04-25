package com.windea.plugin.idea.pls.cwt.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.cwt.psi.*
import com.windea.plugin.idea.pls.cwt.psi.CwtTypes.*

class CwtQuoteHandler:SimpleTokenSetQuoteHandler(KEY_TOKEN, STRING_TOKEN, TokenType.BAD_CHARACTER)