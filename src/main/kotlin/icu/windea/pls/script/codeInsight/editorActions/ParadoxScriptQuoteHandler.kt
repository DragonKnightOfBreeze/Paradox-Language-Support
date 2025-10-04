package icu.windea.pls.script.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.TokenType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes

// NOTE 1.3.0+ 对于脚本语言，因为允许多行的引号括起的字符串，即使已经注册了对应的QuoteHandler，目前IDE也不会自动插入成对的双引号

class ParadoxScriptQuoteHandler : SimpleTokenSetQuoteHandler(ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN, ParadoxScriptElementTypes.STRING_TOKEN, TokenType.BAD_CHARACTER)
