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
import icu.windea.pls.base.settings.ChronicleSettings
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtPsiService
import icu.windea.pls.model.constants.ChronicleStrings

@Optimized
class CwtFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            COMMENT -> ChronicleStrings.commentFolder
            BLOCK -> ChronicleStrings.blockFolder
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = ChronicleSettings.getInstance().state.folding
        return isEnabledByDefault(node, settings)
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = ChronicleSettings.getInstance().state.folding
        collectDescriptors(root, descriptors, settings)
    }

    private fun collectDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: ChronicleSettings.FoldingState) {
        if (!collectCommentDescriptors(element, descriptors, settings)) return
        if (!collectOtherDescriptors(element, descriptors, settings)) return
        element.forEachChild { collectDescriptors(it, descriptors, settings) }
    }

    private fun collectCommentDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: ChronicleSettings.FoldingState): Boolean {
        if (!settings.comments) return true
        val allSiblingLineComments = PsiService.findAllSiblingCommentsIn(element) { it.elementType == COMMENT }
        if (allSiblingLineComments.isEmpty()) return true
        allSiblingLineComments.forEachFast {
            val first = it.first()
            val last = it.last()
            val descriptor = FoldingDescriptor(first, TextRange(first.startOffset, last.endOffset))
            descriptors.add(descriptor)
        }
        return true
    }

    private fun collectOtherDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: ChronicleSettings.FoldingState): Boolean {
        if (isEnabled(element.node, settings)) {
            descriptors.add(FoldingDescriptor(element, element.textRange))
        }
        return CwtPsiService.isStrictMemberContext(element)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun isEnabled(node: ASTNode, settings: ChronicleSettings.FoldingState): Boolean {
        return when (node.elementType) {
            // COMMENT -> settings.comments // not here
            BLOCK -> true
            else -> false
        }
    }

    private fun isEnabledByDefault(node: ASTNode, settings: ChronicleSettings.FoldingState): Boolean {
        return when (node.elementType) {
            COMMENT -> settings.commentsByDefault
            BLOCK -> false
            else -> false
        }
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == CwtFile.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
