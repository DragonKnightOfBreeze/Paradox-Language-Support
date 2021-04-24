package com.windea.plugin.idea.pls.cwt.psi

import com.intellij.lang.*
import com.intellij.lexer.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.pls.cwt.CwtLanguage
import com.windea.plugin.idea.pls.cwt.psi.CwtTypes.*

class CwtParseDefinition:ParserDefinition {
	companion object{
		val WHITE_SPACES=TokenSet.create(WHITE_SPACE)
		val COMMENTS = TokenSet.create(COMMENT, OPTION_COMMENT, DOCUMENTATION_COMMENT)
		val STRINGS = TokenSet.create(STRING_TOKEN, QUOTED_STRING_TOKEN)
		val FILE = CwtFile()
	}
	
	override fun createLexer(project: Project?) = CwtLexerAdapter()
	
	override fun getWhitespaceTokens() = WHITE_SPACES
	
	override fun getCommentTokens() = COMMENTS
	
	override fun getStringLiteralElements() = STRINGS
	
	override fun getFileNodeType(): IFileElementType = FILE
	
	override fun createParser(project: Project?) = CwtParser()
	
	override fun createElement(node: ASTNode?) = Factory.createElement()
}