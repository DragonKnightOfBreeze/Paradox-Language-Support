package icu.windea.pls.script.folding

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
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptPsiService

class ParadoxScriptFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            COMMENT -> ChronicleStrings.commentFolder
            BLOCK -> ChronicleStrings.blockFolder
            CONDITIONAL_BLOCK -> {
                val expression = node.psi.castOrNull<ParadoxScriptConditionalBlock>()?.conditionExpression
                if (expression == null) return "..."
                ChronicleStrings.conditionalBlockFolder(expression)
            }
            INLINE_MATH -> ChronicleStrings.inlineMathFolder
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = ChronicleSettings.getInstance().state.folding
        return when (node.elementType) {
            COMMENT -> settings.commentsByDefault
            BLOCK -> false
            CONDITIONAL_BLOCK -> settings.conditionalBlocksByDefault
            INLINE_MATH -> settings.inlineMathsByDefault
            else -> false
        }
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = ChronicleSettings.getInstance().state.folding
        collectDescriptors(root, descriptors, settings)
    }

    private fun collectDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: ChronicleSettings.FoldingState) {
        collectCommentDescriptors(element, descriptors, settings)
        val r = collectOtherDescriptors(element, descriptors, settings)
        if (!r) return
        element.forEachChild { collectDescriptors(it, descriptors, settings) }
    }

    private fun collectCommentDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: ChronicleSettings.FoldingState) {
        if (!settings.comments) return
        val allSiblingLineComments = PsiService.findAllSiblingCommentsIn(element) { it.elementType == COMMENT }
        if (allSiblingLineComments.isEmpty()) return
        allSiblingLineComments.forEach {
            val startOffset = it.first().startOffset
            val endOffset = it.last().endOffset
            val descriptor = FoldingDescriptor(it.first(), TextRange(startOffset, endOffset))
            descriptors.add(descriptor)
        }
    }

    private fun collectOtherDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: ChronicleSettings.FoldingState): Boolean {
        when (element.elementType) {
            BLOCK -> {
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
            CONDITIONAL_BLOCK -> run r@{
                if (!settings.conditionalBlocks) return@r
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
            INLINE_MATH -> run r@{
                if (!settings.inlineMaths) return@r
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
        }
        return ParadoxScriptPsiService.isStrictMemberContext(element)
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == ParadoxScriptFile.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
