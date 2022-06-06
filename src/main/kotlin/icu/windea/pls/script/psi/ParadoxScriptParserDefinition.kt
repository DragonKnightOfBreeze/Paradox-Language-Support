package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptParserDefinition : ParserDefinition {
	companion object {
		val WHITE_SPACES = TokenSet.create(WHITE_SPACE)
		val COMMENTS = TokenSet.create(COMMENT)
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
	
	override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements {
		val leftType = left?.elementType
		val rightType = right?.elementType
		return when {
			leftType == AT && (rightType == VARIABLE_NAME_ID || rightType == VARIABLE_REFERENCE_ID) -> MUST_NOT
			leftType == PARAMETER_START || rightType == PARAMETER_END -> MUST_NOT
			leftType == PIPE || rightType == PIPE -> MUST_NOT
			else -> MAY
		}
	}
}
