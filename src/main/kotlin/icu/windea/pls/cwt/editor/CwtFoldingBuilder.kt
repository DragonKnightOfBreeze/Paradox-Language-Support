package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtFoldingBuilder : CustomFoldingBuilder(), DumbAware {
	override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
		return when(node.elementType) {
			BLOCK -> PlsConstants.blockFolder
			else -> null
		}
	}
	
	override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
		return false
	}
	
	override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
		collectDescriptorsRecursively(root.node, document, descriptors)
	}
	
	private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>) {
		when(node.elementType) {
			COMMENT -> return //optimization
			OPTION_COMMENT -> return //optimization
			DOCUMENTATION_COMMENT -> return //optimization
			BLOCK -> descriptors.add(FoldingDescriptor(node, node.textRange))
			//BLOCK -> if(isSpanMultipleLines(node, document)) descriptors.add(FoldingDescriptor(node, node.textRange))
		}
		val children = node.getChildren(null)
		for(child in children) {
			collectDescriptorsRecursively(child, document, descriptors)
		}
	}
	
	override fun isCustomFoldingRoot(node: ASTNode): Boolean {
		return node.elementType == CwtFile.ELEMENT_TYPE
	}
	
	override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
		return node.elementType == COMMENT
	}
}
