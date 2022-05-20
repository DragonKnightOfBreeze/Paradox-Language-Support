package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.script.*

class ParadoxScriptLazyParseableElementType(
	debugName: String
):ILazyParseableElementType(debugName, ParadoxScriptLanguage){
	override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
		val project = psi.project
		val languageForParser = ParadoxScriptLanguage
		val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser) as ParadoxScriptParserDefinition
		val definitionInfo = psi.findParentDefinition()?.definitionInfo
		val lexer = parserDefinition.createLexer(project, definitionInfo)
		val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, chameleon.chars)
		val parser = parserDefinition.createParser(project)
		val node = parser.parse(this, builder)
		return node.firstChildNode
	}
}