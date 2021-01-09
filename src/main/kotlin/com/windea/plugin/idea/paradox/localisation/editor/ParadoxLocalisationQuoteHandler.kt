package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationQuoteHandler : SimpleTokenSetQuoteHandler(ParadoxLocalisationTypes.RICH_TEXT, TokenType.BAD_CHARACTER)
