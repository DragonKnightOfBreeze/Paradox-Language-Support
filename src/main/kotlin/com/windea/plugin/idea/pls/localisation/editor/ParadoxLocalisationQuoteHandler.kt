package com.windea.plugin.idea.pls.localisation.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.localisation.psi.*
import com.windea.plugin.idea.pls.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationQuoteHandler : SimpleTokenSetQuoteHandler(RICH_TEXT, TokenType.BAD_CHARACTER)
