package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.gameTypes.stellaris.*
import icu.windea.pls.gameTypes.stellaris.psi.*
import icu.windea.pls.localisation.*

//org.intellij.plugins.markdown.lang.MarkdownLazyElementType

class ParadoxLocalisationStringTokenType(
	debugName: String
) : ILazyParseableElementType(debugName, ParadoxLocalisationLanguage) {
	override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
		return doParseContentsForStellaris(chameleon, psi)
		//val gameType = ParadoxSelectorUtils.selectGameType(psi)
		//return when(gameType) {
		//	ParadoxGameType.Stellaris -> doParseContentsForStellaris(chameleon, psi)
		//	else -> null
		//} ?: doParseStringTokenContents(chameleon)
	}
	
	private fun doParseContentsForStellaris(chameleon: ASTNode, psi: PsiElement): ASTNode {
		val property = psi.parentOfType<ParadoxLocalisationProperty>()!!
		val propertyName = property.name
		//这里要求访问索引（Gist）
		val project = property.project
		val lexer = StellarisFormatStringLexerAdapter()
		val chars = chameleon.chars
		val parser = StellarisFormatStringParser()
		val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, ParadoxLocalisationLanguage, chars)
		val node = parser.parse(chameleon.elementType, builder)
		return node.firstChildNode
	}
	
	//private fun doParseStringTokenContents(chameleon: ASTNode): LeafElement {
	//	val stringTokenNode = ASTFactory.leaf(ParadoxLocalisationElementTypes.STRING_TOKEN, chameleon.text)
	//	val stringNode = ASTFactory.composite(ParadoxLocalisationElementTypes.STRING)
	//	stringNode.rawAddChildrenWithoutNotifications(stringTokenNode)
	//	return stringTokenNode
	//}
}