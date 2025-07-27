package icu.windea.pls.cwt.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.constants.*

class CwtFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            COMMENT -> PlsStringConstants.commentFolder
            BLOCK -> PlsStringConstants.blockFolder
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = PlsFacade.getSettings().folding
        return when (node.elementType) {
            COMMENT -> settings.commentByDefault
            else -> false
        }
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = PlsFacade.getSettings().folding
        collectDescriptorsRecursively(root.node, document, descriptors, settings)
    }

    private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState) {
        val r = doCollectDescriptors(node, document, descriptors, settings)
        if (!r) return
        val children = node.getChildren(null)
        children.forEach { doCollectDescriptors(it, document, descriptors, settings) }
    }

    private fun doCollectDescriptors(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState): Boolean {
        when (node.elementType) {
            COMMENT -> {
                if (!settings.comment) return true
                PlsPsiManager.addCommentFoldingDescriptor(node, document, descriptors)
            }
            OPTION_COMMENT -> return true //optimization
            DOC_COMMENT -> return true //optimization
            BLOCK -> descriptors.add(FoldingDescriptor(node, node.textRange))
            //BLOCK -> if(isSpanMultipleLines(node, document)) descriptors.add(FoldingDescriptor(node, node.textRange))
        }
        return true
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == CwtFile.Companion.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
