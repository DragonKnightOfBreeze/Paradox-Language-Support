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
import icu.windea.pls.lang.psi.PlsPsiManager
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil

class ParadoxScriptFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            COMMENT -> PlsStrings.commentFolder
            BLOCK -> PlsStrings.blockFolder
            PARAMETER_CONDITION -> {
                val expression = node.psi.castOrNull<ParadoxScriptParameterCondition>()?.conditionExpression
                if (expression == null) return "..."
                PlsStrings.parameterConditionFolder(expression)
            }
            INLINE_MATH -> PlsStrings.inlineMathFolder
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = PlsSettings.getInstance().state.folding
        return when (node.elementType) {
            COMMENT -> settings.commentsByDefault
            BLOCK -> false
            PARAMETER_CONDITION -> settings.parameterConditionBlocksByDefault
            INLINE_MATH -> settings.inlineMathBlocksByDefault
            else -> false
        }
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = PlsSettings.getInstance().state.folding
        collectDescriptors(root, descriptors, settings)
    }

    private fun collectDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettings.FoldingState) {
        collectCommentDescriptors(element, descriptors, settings)
        val r = collectOtherDescriptors(element, descriptors, settings)
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

    private fun collectOtherDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettings.FoldingState): Boolean {
        when (element.elementType) {
            BLOCK -> {
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
            PARAMETER_CONDITION -> run r@{
                if (!settings.parameterConditionBlocks) return@r
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
            INLINE_MATH -> run r@{
                if (!settings.inlineMathBlocks) return@r
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
        }
        return ParadoxScriptPsiUtil.isMemberContextElement(element)
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == ParadoxScriptFile.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
