package icu.windea.pls.localisation.editor.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.editor.folding.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            PROPERTY_REFERENCE -> ""
            ICON -> ""
            COMMAND -> PlsConstants.Strings.commandFolder
            CONCEPT_TEXT -> "..."
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        return when (node.elementType) {
            COMMENT -> ParadoxFoldingSettings.getInstance().comment
            PROPERTY_REFERENCE -> ParadoxFoldingSettings.getInstance().localisationReferencesFully
            ICON -> ParadoxFoldingSettings.getInstance().localisationIconsFully
            COMMAND -> {
                val conceptNode = node.findChildByType(CONCEPT)
                if (conceptNode == null) {
                    ParadoxFoldingSettings.getInstance().localisationCommands
                } else {
                    ParadoxFoldingSettings.getInstance().localisationConcepts
                }
            }
            CONCEPT_TEXT -> ParadoxFoldingSettings.getInstance().localisationConceptTexts
            else -> false
        }
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = ParadoxFoldingSettings.getInstance()
        collectDescriptorsRecursively(root.node, document, descriptors, settings)
    }

    private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>, settings: ParadoxFoldingSettings) {
        when (node.elementType) {
            COMMENT -> {
                if (settings.commentEnabled) {
                    ParadoxFoldingManager.addCommentFoldingDescriptor(node, document, descriptors)
                }
            }
            LOCALE -> return //optimization
            PROPERTY_REFERENCE -> {
                if (settings.localisationReferencesFullyEnabled) {
                    descriptors.add(FoldingDescriptor(node, node.textRange))
                }
            }
            ICON -> {
                if (settings.localisationIconsFullyEnabled) {
                    descriptors.add(FoldingDescriptor(node, node.textRange))
                }
            }
            COMMAND -> {
                val conceptNode = node.findChildByType(CONCEPT)
                if (conceptNode == null) {
                    if (settings.localisationCommandsEnabled) {
                        descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsConstants.Strings.commandFolder))
                    }
                } else {
                    if (settings.localisationConceptsEnabled) {
                        val conceptTextNode = conceptNode.findChildByType(CONCEPT_TEXT)
                        if (conceptTextNode == null) {
                            descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsConstants.Strings.conceptFolder))
                        } else {
                            descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsConstants.Strings.conceptWithTextFolder))
                        }
                    }
                }
            }
            CONCEPT_NAME -> return //optimization
            CONCEPT_TEXT -> {
                if (settings.localisationConceptTextsEnabled) {
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
        return node.elementType == ParadoxLocalisationFile.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
