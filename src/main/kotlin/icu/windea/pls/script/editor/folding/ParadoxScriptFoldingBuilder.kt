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
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            BLOCK -> PlsStringConstants.blockFolder
            PARAMETER_CONDITION -> {
                val expression = node.psi.castOrNull<ParadoxScriptParameterCondition>()?.conditionExpression
                if (expression == null) return "..."
                PlsStringConstants.parameterConditionFolder(expression)
            }
            INLINE_MATH -> PlsStringConstants.inlineMathFolder
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = PlsFacade.getSettings().folding
        return when (node.elementType) {
            COMMENT -> settings.commentByDefault
            BLOCK -> false
            PARAMETER_CONDITION -> settings.parameterConditionBlocksByDefault
            INLINE_MATH -> settings.inlineMathBlocksByDefault
            else -> false
        }
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = PlsFacade.getSettings().folding
        collectDescriptorsRecursively(root.node, document, descriptors, settings)
    }

    private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState) {
        run {
            when (node.elementType) {
                COMMENT -> {
                    if (!settings.comment) return@run
                    ParadoxFoldingManager.addCommentFoldingDescriptor(node, document, descriptors)
                }
                SCRIPTED_VARIABLE -> {
                    return //optimization
                }
                BLOCK -> {
                    descriptors.add(FoldingDescriptor(node, node.textRange))
                }
                PARAMETER_CONDITION -> {
                    descriptors.add(FoldingDescriptor(node, node.textRange))
                }
                INLINE_MATH -> {
                    descriptors.add(FoldingDescriptor(node, node.textRange))
                }
            }
        }
        val children = node.getChildren(null)
        for (child in children) {
            collectDescriptorsRecursively(child, document, descriptors, settings)
        }
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType is ParadoxScriptFileStubElementType
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
