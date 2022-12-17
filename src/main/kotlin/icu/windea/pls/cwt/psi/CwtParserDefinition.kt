package icu.windea.pls.cwt.psi

import com.intellij.lang.*
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
}