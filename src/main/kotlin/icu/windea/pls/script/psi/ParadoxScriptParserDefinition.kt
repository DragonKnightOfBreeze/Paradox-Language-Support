package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.TokenType.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

class ParadoxScriptParserDefinition : ParserDefinition {
	companion object {
		val WHITE_SPACES = TokenSet.create(WHITE_SPACE)
		val COMMENTS = TokenSet.create(COMMENT, END_OF_LINE_COMMENT)
		val STRINGS = TokenSet.create(STRING_TOKEN, QUOTED_STRING_TOKEN)
		val FILE = ParadoxScriptStubElementTypes.FILE
	}
	
	override fun getWhitespaceTokens() = WHITE_SPACES
	
	override fun getCommentTokens() = COMMENTS
	
	override fun getStringLiteralElements() = STRINGS
	
	override fun getFileNodeType() = FILE
	
	override fun createFile(viewProvider: FileViewProvider) = ParadoxScriptFile(viewProvider)
	
	override fun createElement(node: ASTNode): PsiElement {
		//if(node.elementType == STRING && ) {
		//	val nodeText = node.text
		//	if(nodeText == "optimize_memory") {
		//		val finalNode = ASTFactory.composite(TAG)
		//		finalNode.rawAddChildrenWithoutNotifications(ASTFactory.leaf(TAG_TOKEN, nodeText))
		//		val parent = node.treeParent as CompositeElement
		//		parent.replaceChildInternal()
		//		//parent.addInternal(finalNode, finalNode, node, true)
		//		//parent.deleteChildInternal(node)
		//		return ParadoxScriptTagImpl(finalNode)
		//	}
		//}
		//if(node.elementType == ParadoxScriptStringLikeTokenType){
		//	node as CompositeElement
		//	val childNode = node.firstChildNode as TreeElement
		//	val childType = childNode.elementType
		//	val errorNode = childNode.treeParent as TreeElement
		//	errorNode.rawReplaceWithList(childNode)
		//	//val parentNode = node.treeParent as CompositeElement
		//	////node.rawRemoveAllChildren()
		//	//parentNode.replaceChild(node, childNode)
		//	////parentNode.replaceChildInternal(node, childNode)
		//	////parentNode.firstChildNode = childNode
		//	//parentNode.rawRemoveAllChildren()
		//	//val blockNode = parentNode.treeParent
		//	//blockNode.replaceChild(parentNode, childNode)
		//	//blockNode.addInternal(parentNode, parentNode, childNode, true)
		//	return when(childType){
		//		STRING -> ParadoxScriptStringImpl(childNode)
		//		TAG -> ParadoxScriptTagImpl(childNode)
		//		else -> throw InternalError()
		//	}
		//}
		//return when(node.elementType) {
		//	STRING -> {
		//		////TODO
		//		//val parentNode = node.treeParent as LazyParseableElement
		//		//val childNode = node.firstChildNode
		//		//parentNode.rawRemoveAllChildren()
		//		//parentNode.rawAddChildrenWithoutNotifications(childNode as TreeElement)
		//		//val childType = childNode.elementType
		//		//return when(childType){
		//		//	STRING -> ParadoxScriptStringImpl(childNode)
		//		//	TAG -> ParadoxScriptTagImpl(childNode)
		//		//	else -> throw InternalError()
		//		//}
		//	}
		//	else -> Factory.createElement(node)
		//}
		if(node.elementType == ParadoxScriptStringLikeTokenType){
			val childNode = node.firstChildNode
			val childType = childNode.elementType
			return when(childType){
				STRING_TOKEN -> ParadoxScriptStringImpl(node)
				TAG_TOKEN -> ParadoxScriptTagImpl(node)
				else -> throw InternalError()
			}
		}
		return Factory.createElement(node)
	}
	
	override fun createParser(project: Project?) = ParadoxScriptParser()
	
	override fun createLexer(project: Project?) = ParadoxScriptLexerAdapter(project)
	
	fun createLexer(project: Project?, definitionInfo: ParadoxDefinitionInfo?) = ParadoxScriptLexerAdapter(project)
}
