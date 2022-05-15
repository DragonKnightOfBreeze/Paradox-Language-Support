package icu.windea.pls.cwt.psi

import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtParseDefinition:ParserDefinition {
	companion object{
		val WHITE_SPACES=TokenSet.create(WHITE_SPACE)
		val COMMENTS = TokenSet.create(COMMENT) //不包含DOCUMENTATION_TOKEN
		val STRINGS = TokenSet.create(STRING_TOKEN)
		val FILE = IFileElementType("CWT_FILE", CwtLanguage)
	}
	
	override fun getWhitespaceTokens() = WHITE_SPACES
	
	override fun getCommentTokens() = COMMENTS
	
	override fun getStringLiteralElements() = STRINGS
	
	override fun getFileNodeType() = FILE
	
	override fun createFile(viewProvider: FileViewProvider) =  CwtFile(viewProvider)
	
	override fun createElement(node: ASTNode?) = Factory.createElement(node)
	
	override fun createParser(project: Project?) = CwtParser()
	
	override fun createLexer(project: Project?) = CwtLexerAdapter()
}