package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.CwtTypes.*

class CwtFoldingBuilder:FoldingBuilder,DumbAware {
	override fun getPlaceholderText(node: ASTNode): String {
		return when(node.elementType){
			BLOCK -> blockFolder
			else -> throw InternalError()
		}
	}

	override fun isCollapsedByDefault(node: ASTNode): Boolean {
		return false
	}

	override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
		val descriptors = mutableListOf<FoldingDescriptor>()
		collectDescriptorsRecursively(node,document,descriptors)
		return descriptors.toTypedArray()
	}

	private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>) {
		when(node.elementType) {
			BLOCK -> {
				if(isSpanMultipleLines(node, document)) descriptors.add(FoldingDescriptor(node, node.textRange))
			}
		}
		val children = node.getChildren(null)
		for(child in children) {
			collectDescriptorsRecursively(child, document, descriptors)
		}
	}
}