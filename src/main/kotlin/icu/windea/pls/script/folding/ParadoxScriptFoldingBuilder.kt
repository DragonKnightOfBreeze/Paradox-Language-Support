package icu.windea.pls.script.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.settings.PlsSettingsState
import icu.windea.pls.lang.util.PlsPsiManager
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition

class ParadoxScriptFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            ParadoxScriptElementTypes.COMMENT -> PlsStringConstants.commentFolder
            ParadoxScriptElementTypes.BLOCK -> PlsStringConstants.blockFolder
            ParadoxScriptElementTypes.PARAMETER_CONDITION -> {
                val expression = node.psi.castOrNull<ParadoxScriptParameterCondition>()?.conditionExpression
                if (expression == null) return "..."
                PlsStringConstants.parameterConditionFolder(expression)
            }
            ParadoxScriptElementTypes.INLINE_MATH -> PlsStringConstants.inlineMathFolder
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = PlsFacade.getSettings().folding
        return when (node.elementType) {
            ParadoxScriptElementTypes.COMMENT -> settings.commentByDefault
            ParadoxScriptElementTypes.BLOCK -> false
            ParadoxScriptElementTypes.PARAMETER_CONDITION -> settings.parameterConditionBlocksByDefault
            ParadoxScriptElementTypes.INLINE_MATH -> settings.inlineMathBlocksByDefault
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
        children.forEach { collectDescriptorsRecursively(it, document, descriptors, settings) }
    }

    private fun doCollectDescriptors(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState): Boolean {
        when (node.elementType) {
            ParadoxScriptElementTypes.COMMENT -> {
                if (!settings.comment) return true
                PlsPsiManager.addCommentFoldingDescriptor(node, document, descriptors)
            }
            ParadoxScriptElementTypes.SCRIPTED_VARIABLE -> return false //optimization
            ParadoxScriptElementTypes.BLOCK -> {
                descriptors.add(FoldingDescriptor(node, node.textRange))
            }
            ParadoxScriptElementTypes.PARAMETER_CONDITION -> {
                descriptors.add(FoldingDescriptor(node, node.textRange))
            }
            ParadoxScriptElementTypes.INLINE_MATH -> {
                descriptors.add(FoldingDescriptor(node, node.textRange))
            }
        }
        return true
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == ParadoxScriptFile.Companion.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == ParadoxScriptElementTypes.COMMENT
    }
}
