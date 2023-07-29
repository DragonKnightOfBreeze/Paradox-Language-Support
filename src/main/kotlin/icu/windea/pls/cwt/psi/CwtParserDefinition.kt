package icu.windea.pls.cwt.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtParserDefinition : ParserDefinition {
	override fun getWhitespaceTokens() = CwtTokenSets.WHITE_SPACES
	
	override fun getCommentTokens() = CwtTokenSets.COMMENTS
	
	override fun getStringLiteralElements() = CwtTokenSets.STRING_LITERALS
	
	override fun getFileNodeType() = CwtFile.ELEMENT_TYPE
	
	override fun createFile(viewProvider: FileViewProvider) = CwtFile(viewProvider)
	
	override fun createElement(node: ASTNode?) = Factory.createElement(node)
	
	override fun createParser(project: Project?) = CwtParser()
	
	override fun createLexer(project: Project?) = CwtLexer()
	
	override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): SpaceRequirements {
		val leftType = left?.elementType
		//val rightType = right?.elementType
		return when {
			leftType == COMMENT -> MUST_LINE_BREAK
			leftType == OPTION_COMMENT -> MUST_LINE_BREAK
			leftType == DOCUMENTATION_COMMENT -> MUST_LINE_BREAK
			else -> MAY
		}
	}
}