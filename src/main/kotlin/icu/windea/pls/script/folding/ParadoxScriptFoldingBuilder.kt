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
import icu.windea.pls.base.settings.ChronicleSettings
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptNormalConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptPsiService

@Optimized
class ParadoxScriptFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            COMMENT -> ChronicleStrings.commentFolder
            BLOCK -> ChronicleStrings.blockFolder
            NORMAL_CONDITIONAL_BLOCK -> {
                val psi = node.psi.castOrNull<ParadoxScriptNormalConditionalBlock>()
                val expressionText = psi?.conditionalExpression?.presentableText
                ChronicleStrings.conditionalBlockFolder(expressionText.or.unresolved())
            }
            INLINE_CONDITIONAL_BLOCK -> {
                val psi = node.psi.castOrNull<ParadoxScriptInlineConditionalBlock>()
                val expressionText = psi?.conditionalExpression?.presentableText
                ChronicleStrings.conditionalBlockFolder(expressionText.or.unresolved())
            }
            INLINE_MATH -> ChronicleStrings.inlineMathFolder
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
        collectCommentDescriptors(element, descriptors, settings)
        val r = collectOtherDescriptors(element, descriptors, settings)
        if (!r) return
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
        return ParadoxScriptPsiService.isStrictMemberContext(element)
    }

    private fun isEnabled(node: ASTNode, settings: ChronicleSettings.FoldingState): Boolean {
        return when (node.elementType) {
            // COMMENT -> settings.comments // not here
            BLOCK -> true
            NORMAL_CONDITIONAL_BLOCK -> settings.conditionalBlocks
            INLINE_CONDITIONAL_BLOCK -> settings.inlineConditionalBlocks
            INLINE_MATH -> settings.inlineMaths
            else -> false
        }
    }

    private fun isEnabledByDefault(node: ASTNode, settings: ChronicleSettings.FoldingState): Boolean {
        return when (node.elementType) {
            COMMENT -> settings.commentsByDefault
            BLOCK -> false
            NORMAL_CONDITIONAL_BLOCK -> settings.conditionalBlocksByDefault
            INLINE_CONDITIONAL_BLOCK -> settings.inlineConditionalBlocksByDefault
            INLINE_MATH -> settings.inlineMathsByDefault
            else -> false
        }
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == ParadoxScriptFile.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
