package com.windea.plugin.idea.pls.localisation.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.localisation.psi.*

class ParadoxLocalisationQuoteHandler : SimpleTokenSetQuoteHandler(ParadoxLocalisationTypes.RICH_TEXT, TokenType.BAD_CHARACTER)
