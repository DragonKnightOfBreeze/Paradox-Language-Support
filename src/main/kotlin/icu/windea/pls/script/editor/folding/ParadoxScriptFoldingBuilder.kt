package icu.windea.pls.script.editor.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String {
        return when(node.elementType) {
            BLOCK -> PlsConstants.blockFolder
            PARAMETER_CONDITION -> {
                val expression = node.psi.castOrNull<ParadoxScriptParameterCondition>()?.conditionExpression ?: PlsConstants.ellipsis
                PlsConstants.parameterConditionFolder(expression)
            }
            INLINE_MATH -> PlsConstants.inlineMathFolder
            else -> throw InternalError()
        }
    }
    
    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        return when(node.elementType) {
            BLOCK -> false
            PARAMETER_CONDITION -> service<ParadoxFoldingSettings>().collapseParameterConditions
            INLINE_MATH -> service<ParadoxFoldingSettings>().collapseInlineMathBlocks
            else -> false
        }
    }
    
    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        collectDescriptorsRecursively(root.node, document, descriptors)
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
    
    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
