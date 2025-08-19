package icu.windea.pls.cwt.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
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
        collectDescriptors(root, descriptors, settings)
    }

    private fun collectDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState) {
        collectCommentDescriptors(element, descriptors, settings)
        val r = collectOtherDescriptors(element, descriptors)
        if (!r) return
        element.forEachChild { collectDescriptors(it, descriptors, settings) }
    }

    private fun collectCommentDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState) {
        if (!settings.comment) return
        val allSiblingLineComments = PlsPsiManager.findAllSiblingLineCommentsIn(element) { it.elementType == COMMENT }
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
