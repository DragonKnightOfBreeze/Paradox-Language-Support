package icu.windea.pls.script.editor

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition

class ParadoxScriptFoldingBuilder : FoldingBuilder, DumbAware {
	override fun getPlaceholderText(node: ASTNode): String {
		return when(node.elementType) {
			BLOCK -> PlsFolders.blockFolder
			PARAMETER_CONDITION -> {
				val expression = node.psi.castOrNull<ParadoxScriptParameterCondition>()?.expression ?: PlsFolders.ellipsis
				PlsFolders.parameterConditionFolder(expression)
			}
			INLINE_MATH -> PlsFolders.inlineMathFolder
			else -> throw InternalError()
		}
	}
	
	override fun isCollapsedByDefault(node: ASTNode): Boolean {
		return false
	}
	
	override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
		val descriptors: MutableList<FoldingDescriptor> = SmartList()
		collectDescriptorsRecursively(node, document, descriptors)
		return descriptors.toTypedArray()
	}
	
	private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>) {
		when(node.elementType) {
			BLOCK -> descriptors.add(FoldingDescriptor(node, node.textRange))
			//BLOCK -> if(isSpanMultipleLines(node, document)) descriptors.add(FoldingDescriptor(node, node.textRange))
			PARAMETER_CONDITION -> descriptors.add(FoldingDescriptor(node, node.textRange)) 
			INLINE_MATH -> descriptors.add(FoldingDescriptor(node, node.textRange))
		}
		val children = node.getChildren(null)
		for(child in children) {
			collectDescriptorsRecursively(child, document, descriptors)
		}
	}
}
