package icu.windea.pls.cwt.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.forEachChild
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtPsiUtil
import icu.windea.pls.lang.psi.PlsPsiManager
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.constants.PlsStringConstants

class CwtFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            COMMENT -> PlsStringConstants.commentFolder
            BLOCK -> PlsStringConstants.blockFolder
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = PlsSettings.getInstance().state.folding
        return when (node.elementType) {
            COMMENT -> settings.commentsByDefault
            else -> false
        }
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = PlsSettings.getInstance().state.folding
        collectDescriptors(root, descriptors, settings)
    }

    private fun collectDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettings.FoldingState) {
        collectCommentDescriptors(element, descriptors, settings)
        val r = collectOtherDescriptors(element, descriptors)
        if (!r) return
        element.forEachChild { collectDescriptors(it, descriptors, settings) }
    }

    private fun collectCommentDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettings.FoldingState) {
        if (!settings.comments) return
        val allSiblingLineComments = PlsPsiManager.findAllSiblingCommentsIn(element) { it.elementType == COMMENT }
        if (allSiblingLineComments.isEmpty()) return
        allSiblingLineComments.forEach {
            val startOffset = it.first().startOffset
            val endOffset = it.last().endOffset
            val descriptor = FoldingDescriptor(it.first(), TextRange(startOffset, endOffset))
            descriptors.add(descriptor)
        }
    }

    private fun collectOtherDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>): Boolean {
        if (element.elementType == BLOCK) {
            descriptors.add(FoldingDescriptor(element, element.textRange))
        }
        return CwtPsiUtil.isMemberContextElement(element)
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == CwtFile.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
