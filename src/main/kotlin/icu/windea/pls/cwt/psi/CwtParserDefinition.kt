package icu.windea.pls.cwt.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtParserDefinition : ParserDefinition {
	companion object {
		val FILE = IFileElementType("CWT_FILE", CwtLanguage)
	}
	
	override fun getWhitespaceTokens() = CwtTokenSets.WHITE_SPACES
	
	override fun getCommentTokens() = CwtTokenSets.COMMENTS
	
	override fun getStringLiteralElements() = CwtTokenSets.STRING_LITERALS
	
	override fun getFileNodeType() = FILE
	
	override fun createFile(viewProvider: FileViewProvider) = CwtFile(viewProvider)
	
	override fun createElement(node: ASTNode?) = Factory.createElement(node)
	
	override fun createParser(project: Project?) = CwtParser()
	
	override fun createLexer(project: Project?) = CwtLexerAdapter()
	
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