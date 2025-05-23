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
            PARAMETER -> ""
            ICON -> ""
            COMMAND -> PlsConstants.Strings.commandFolder
            CONCEPT_COMMAND -> PlsConstants.Strings.conceptCommandFolder
            CONCEPT_TEXT -> "..."
            else -> null
        }
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        val settings = getSettings().folding
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
        val settings = getSettings().folding
        collectDescriptorsRecursively(root.node, document, descriptors, settings)
    }

    private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState) {
        run {
            when (node.elementType) {
                COMMENT -> {
                    if (!settings.comment) return@run
                    ParadoxFoldingManager.addCommentFoldingDescriptor(node, document, descriptors)
                }
                LOCALE -> {
                    return //optimization
                }
                PARAMETER -> {
                    if (!settings.localisationReferencesFully) return@run
                    descriptors.add(FoldingDescriptor(node, node.textRange))
                }
                ICON -> {
                    if (!settings.localisationIconsFully) return@run
                    descriptors.add(FoldingDescriptor(node, node.textRange))
                }
                COMMAND -> {
                    if (!settings.localisationCommands) return@run
                    descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsConstants.Strings.commandFolder))
                }
                CONCEPT_COMMAND -> {
                    if (!settings.localisationConceptCommands) return@run
                    val conceptTextNode = node.findChildByType(CONCEPT_TEXT)
                    if (conceptTextNode == null) {
                        descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsConstants.Strings.conceptCommandFolder))
                    } else {
                        descriptors.add(FoldingDescriptor(node, node.textRange, null, PlsConstants.Strings.conceptCommandWithTextFolder))
                    }
                }
                CONCEPT_NAME -> {
                    return //optimization
                }
                CONCEPT_TEXT -> {
                    if (!settings.localisationConceptTexts) return@run
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
