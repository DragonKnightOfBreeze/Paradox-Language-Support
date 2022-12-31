package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.lang.ParserDefinition.SpaceRequirements.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationParserDefinition : ParserDefinition {
	override fun getWhitespaceTokens() = ParadoxLocalisationTokenSets. WHITE_SPACES
	
	override fun getCommentTokens() = ParadoxLocalisationTokenSets.COMMENTS
	
	override fun getStringLiteralElements() = ParadoxLocalisationTokenSets.STRING_LITERALS
	
	override fun getFileNodeType() = ParadoxLocalisationStubElementTypes.FILE
	
	override fun createFile(viewProvider: FileViewProvider): ParadoxLocalisationFile {
		return ParadoxLocalisationFile(viewProvider)
	}
	
	override fun createElement(node: ASTNode): PsiElement {
		return Factory.createElement(node)
	}
	
	override fun createParser(project: Project?): ParadoxLocalisationParser {
		return ParadoxLocalisationParser()
	}
	
	override fun createLexer(project: Project?): ParadoxLocalisationLexerAdapter {
		return ParadoxLocalisationLexerAdapter()
	}
	
	fun createLexer(virtualFile: VirtualFile, project: Project?): ParadoxLocalisationLexerAdapter {
		val fileInfo = virtualFile.fileInfo
		val context = ParadoxLocalisationParsingContext(project, fileInfo)
		return ParadoxLocalisationLexerAdapter(context)
	}
	
	override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
		val leftType = left?.elementType
		val rightType = right?.elementType
		return when {
			leftType == LOCALE_ID && rightType == COLON -> MUST_NOT
			//语言区域之前必须换行
			rightType == LOCALE_ID -> MUST_LINE_BREAK
			//属性之前必须换行
			rightType == PROPERTY_KEY_TOKEN -> MUST_LINE_BREAK
			else -> MAY
		}
	}
}


