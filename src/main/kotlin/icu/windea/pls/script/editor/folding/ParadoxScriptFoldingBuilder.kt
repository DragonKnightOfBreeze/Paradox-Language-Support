package icu.windea.pls.script.editor.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.editor.folding.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            BLOCK -> PlsConstants.Strings.blockFolder
            PARAMETER_CONDITION -> {
                val expression = node.psi.castOrNull<ParadoxScriptParameterCondition>()?.conditionExpression
                if (expression == null) return "..."
                PlsConstants.Strings.parameterConditionFolder(expression)
            }
            INLINE_MATH -> PlsConstants.Strings.inlineMathFolder
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        return when (node.elementType) {
            BLOCK -> false
            PARAMETER_CONDITION -> ParadoxFoldingSettings.getInstance().parameterConditionBlocks
            INLINE_MATH -> ParadoxFoldingSettings.getInstance().inlineMathBlocks
            else -> false
        }
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        collectDescriptorsRecursively(root.node, document, descriptors)
    }

    private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>) {
        when (node.elementType) {
            COMMENT -> return //optimization
            SCRIPTED_VARIABLE -> return //optimization
            BLOCK -> descriptors.add(FoldingDescriptor(node, node.textRange))
            PARAMETER_CONDITION -> descriptors.add(FoldingDescriptor(node, node.textRange))
            INLINE_MATH -> descriptors.add(FoldingDescriptor(node, node.textRange))
        }
        val children = node.getChildren(null)
        for (child in children) {
            collectDescriptorsRecursively(child, document, descriptors)
        }
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == ParadoxScriptFile.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
