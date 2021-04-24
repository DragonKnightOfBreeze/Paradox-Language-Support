package com.windea.plugin.idea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.pls.script.psi.ParadoxScriptTypes.*
import com.windea.plugin.idea.pls.script.*

class ParadoxScriptParserDefinition : ParserDefinition {
	companion object {
		val WHITE_SPACES = TokenSet.create(WHITE_SPACE)
		val COMMENTS = TokenSet.create(COMMENT, END_OF_LINE_COMMENT)
		val STRINGS = TokenSet.create(STRING_TOKEN, QUOTED_STRING_TOKEN)
		val FILE = ParadoxScriptStubElementTypes.FILE
	}

	override fun getWhitespaceTokens() = WHITE_SPACES
	
	override fun getCommentTokens() = COMMENTS
	
	override fun getStringLiteralElements() = STRINGS
	
	override fun getFileNodeType() = FILE
	
	override fun createFile(viewProvider: FileViewProvider) = ParadoxScriptFile(viewProvider)
	
	override fun createElement(node: ASTNode) = Factory.createElement(node)
	
	override fun createParser(project: Project?) = ParadoxScriptParser()
	
	override fun createLexer(project: Project?) = ParadoxScriptLexerAdapter()
}
