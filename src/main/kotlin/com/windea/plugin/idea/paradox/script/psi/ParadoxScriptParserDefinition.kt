package com.windea.plugin.idea.paradox.script.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*
import com.windea.plugin.idea.paradox.script.*

class ParadoxScriptParserDefinition : ParserDefinition {
	companion object {
		val WHITE_SPACES = TokenSet.create(WHITE_SPACE)
		val COMMENTS = TokenSet.create(COMMENT, END_OF_LINE_COMMENT)
		val STRINGS = TokenSet.create(QUOTED_STRING_TOKEN, STRING_TOKEN)
		val FILE = IFileElementType(ParadoxScriptLanguage)
	}

	override fun createLexer(project: Project?) = ParadoxScriptLexerAdapter()

	override fun getWhitespaceTokens() = WHITE_SPACES

	override fun getCommentTokens() = COMMENTS

	override fun getStringLiteralElements() = STRINGS

	override fun createParser(project: Project?) = ParadoxScriptParser()

	override fun getFileNodeType() = ParadoxScriptStubElementTypes.FILE

	override fun createFile(viewProvider: FileViewProvider) = ParadoxScriptFile(viewProvider)

	override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?) = when {
		//文本之间必须有空白
		left?.elementType == STRING && right?.elementType == STRING -> MUST
		//变量之前必须换行
		right?.elementType == VARIABLE_NAME -> MUST_LINE_BREAK
		//属性之前必须换行，除非是"{"
		right?.elementType == PROPERTY_KEY && left?.elementType != LEFT_BRACE -> MUST_LINE_BREAK
		else -> MAY
	}

	override fun createElement(node: ASTNode) = ParadoxScriptTypes.Factory.createElement(node)
}
