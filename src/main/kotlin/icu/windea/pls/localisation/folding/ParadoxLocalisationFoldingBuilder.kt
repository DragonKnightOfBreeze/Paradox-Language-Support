package icu.windea.pls.localisation.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.PlsPsiManager
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.constants.*

class ParadoxLocalisationFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            COMMENT -> PlsStringConstants.commentFolder
            PARAMETER -> ""
            ICON -> ""
            COMMAND -> PlsStringConstants.commandFolder
            CONCEPT_COMMAND -> PlsStringConstants.conceptCommandFolder
            CONCEPT_TEXT -> "..."
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = PlsFacade.getSettings().folding
        return when (node.elementType) {
            COMMENT -> settings.commentByDefault
            PARAMETER -> settings.localisationReferencesFullyByDefault
            ICON -> settings.localisationIconsFullyByDefault
            COMMAND -> settings.localisationCommandsByDefault
            CONCEPT_COMMAND -> settings.localisationConceptCommandsByDefault
            CONCEPT_TEXT -> settings.localisationConceptTextsByDefault
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
            LOCALE -> return false //optimization
            PARAMETER -> {
                if (!settings.localisationReferencesFully) return true
                descriptors.add(FoldingDescriptor(node, node.textRange))
            }
            ICON -> {
                if (!settings.localisationIconsFully) return true
                descriptors.add(FoldingDescriptor(node, node.textRange))
            }
            COMMAND -> {
                if (!settings.localisationCommands) return true
                descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsStringConstants.commandFolder))
            }
            CONCEPT_COMMAND -> {
                if (!settings.localisationConceptCommands) return true
                val conceptTextNode = node.findChildByType(CONCEPT_TEXT)
                if (conceptTextNode == null) {
                    descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsStringConstants.conceptCommandFolder))
                } else {
                    descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsStringConstants.conceptCommandWithTextFolder))
                }
            }
            CONCEPT_NAME -> return false //optimization
            CONCEPT_TEXT -> {
                if (!settings.localisationConceptTexts) return true
                descriptors.add(FoldingDescriptor(node, node.textRange))
            }
        }
        return true
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == ParadoxLocalisationFile.Companion.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
