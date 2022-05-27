package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.util.SmartList
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*

class CwtFoldingBuilder:FoldingBuilder,DumbAware {
	override fun getPlaceholderText(node: ASTNode): String {
		return when(node.elementType) {
			BLOCK -> PlsFolders.blockFolder
			else -> throw InternalError()
		}
	}

	override fun isCollapsedByDefault(node: ASTNode): Boolean {
		return false
	}

	override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
		val descriptors: MutableList<FoldingDescriptor> = SmartList()
		collectDescriptorsRecursively(node,document,descriptors)
		return descriptors.toTypedArray()
	}

	private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>) {
		when(node.elementType) {
			BLOCK -> {
				descriptors.add(FoldingDescriptor(node, node.textRange))
				//if(isSpanMultipleLines(node, document)) descriptors.add(FoldingDescriptor(node, node.textRange))
			}
		}
		val children = node.getChildren(null)
		for(child in children) {
			collectDescriptorsRecursively(child, document, descriptors)
		}
	}
}