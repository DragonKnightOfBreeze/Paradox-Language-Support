package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*

class ParadoxLocalisationParserDefinition : ParserDefinition {
	companion object {
		val WHITE_SPACES = TokenSet.create(WHITE_SPACE)
		val COMMENTS = TokenSet.create(COMMENT, ROOT_COMMENT,END_OF_LINE_COMMENT)
		val STRINGS = TokenSet.create(STRING_TOKEN)
		val FILE = ParadoxLocalisationStubElementTypes.FILE
	}
	
	override fun getWhitespaceTokens() = WHITE_SPACES

	override fun getCommentTokens() = COMMENTS

	override fun getStringLiteralElements() = STRINGS
	
	override fun getFileNodeType() = FILE
	
	override fun createFile(viewProvider: FileViewProvider) = ParadoxLocalisationFile(viewProvider)

	override fun createElement(node: ASTNode) = Factory.createElement(node)
	
	override fun createParser(project: Project?) = ParadoxLocalisationParser()
	
	override fun createLexer(project: Project?) = ParadoxLocalisationLexerAdapter()
	
	override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
		return when {
			//语言区域之前必须换行
			right?.elementType == LOCALE -> MUST_LINE_BREAK
			//属性之前必须换行
			right?.elementType == PROPERTY -> MUST_LINE_BREAK
			else -> MAY
		}
	}
}


