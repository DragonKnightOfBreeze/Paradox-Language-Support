package icu.windea.pls.cwt.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.lang.settings.PlsSettingsState
import icu.windea.pls.lang.util.PlsPsiManager
import icu.windea.pls.model.constants.PlsStringConstants

class CwtFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            CwtElementTypes.COMMENT -> PlsStringConstants.commentFolder
            CwtElementTypes.BLOCK -> PlsStringConstants.blockFolder
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = PlsFacade.getSettings().folding
        return when (node.elementType) {
            CwtElementTypes.COMMENT -> settings.commentByDefault
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
            CwtElementTypes.COMMENT -> {
                if (!settings.comment) return true
                PlsPsiManager.addCommentFoldingDescriptor(node, document, descriptors)
            }
            CwtElementTypes.OPTION_COMMENT -> return true //optimization
            CwtElementTypes.DOC_COMMENT -> return true //optimization
            CwtElementTypes.BLOCK -> descriptors.add(FoldingDescriptor(node, node.textRange))
            //BLOCK -> if(isSpanMultipleLines(node, document)) descriptors.add(FoldingDescriptor(node, node.textRange))
        }
        return true
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == CwtFile.Companion.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == CwtElementTypes.COMMENT
    }
}
