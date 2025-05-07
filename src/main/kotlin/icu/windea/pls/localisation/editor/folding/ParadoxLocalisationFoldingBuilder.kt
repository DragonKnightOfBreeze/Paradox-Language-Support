package icu.windea.pls.localisation.editor.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.editor.folding.*
import icu.windea.pls.lang.settings.*
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
        val settings = getSettings().folding
        return when (node.elementType) {
            COMMENT -> settings.commentByDefault
            PROPERTY_REFERENCE -> settings.localisationReferencesFullyByDefault
            ICON -> settings.localisationIconsFullyByDefault
            COMMAND -> {
                val conceptNode = node.findChildByType(CONCEPT)
                if (conceptNode == null) {
                    settings.localisationCommandsByDefault
                } else {
                    settings.localisationConceptsByDefault
                }
            }
            CONCEPT_TEXT -> settings.localisationConceptTextsByDefault
            else -> false
        }
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = getSettings().folding
        collectDescriptorsRecursively(root.node, document, descriptors, settings)
    }

    private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState) {
        when (node.elementType) {
            COMMENT -> {
                if (settings.comment) {
                    ParadoxFoldingManager.addCommentFoldingDescriptor(node, document, descriptors)
                }
            }
            LOCALE -> return //optimization
            PROPERTY_REFERENCE -> {
                if (settings.localisationReferencesFully) {
                    descriptors.add(FoldingDescriptor(node, node.textRange))
                }
            }
            ICON -> {
                if (settings.localisationIconsFully) {
                    descriptors.add(FoldingDescriptor(node, node.textRange))
                }
            }
            COMMAND -> {
                val conceptNode = node.findChildByType(CONCEPT)
                if (conceptNode == null) {
                    if (settings.localisationCommands) {
                        descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsConstants.Strings.commandFolder))
                    }
                } else {
                    if (settings.localisationConcepts) {
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
                if (settings.localisationConceptTexts) {
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
        return node.elementType is ParadoxLocalisationFileStubElementType
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
