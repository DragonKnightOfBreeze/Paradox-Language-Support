package icu.windea.pls.localisation.folding

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
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiService
import icu.windea.pls.model.constants.ChronicleStrings

@Optimized
class ParadoxLocalisationFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when (node.elementType) {
            COMMENT -> ChronicleStrings.commentFolder
            PROPERTY_VALUE -> ChronicleStrings.quotedFolder
            PARAMETER -> ""
            COMMAND -> ChronicleStrings.localisationCommandFolder
            CONCEPT_COMMAND -> {
                val conceptStringNode = node.findChildByType(CONCEPT_STRING)
                if (conceptStringNode == null) ChronicleStrings.localisationConceptCommandFolder
                else ChronicleStrings.localisationConceptCommandFolderWithText
            }
            CONCEPT_STRING -> "..."
            ICON -> ""
            TEXT_ICON -> ""
            TEXT_FORMAT -> ChronicleStrings.localisationTextFormatFolder
            TEXT_FORMAT_STRING -> ""
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
        return ParadoxLocalisationPsiService.isStrictRichTextContext(element)
    }

    private fun isEnabled(node: ASTNode, settings: ChronicleSettings.FoldingState): Boolean {
        return when (node.elementType) {
            // COMMENT -> settings.comments // not here
            PROPERTY_VALUE -> settings.localisationTexts
            PARAMETER -> settings.localisationParametersFully
            COMMAND -> settings.localisationCommands
            CONCEPT_COMMAND -> settings.localisationConceptCommands
            CONCEPT_STRING -> settings.localisationConceptStrings
            ICON -> settings.localisationIconsFully
            TEXT_ICON -> settings.localisationTextIconsFully
            TEXT_FORMAT -> settings.localisationTextFormats
            TEXT_FORMAT_STRING -> settings.localisationTextFormatStrings
            else -> false
        }
    }

    private fun isEnabledByDefault(node: ASTNode, settings: ChronicleSettings.FoldingState): Boolean {
        return when (node.elementType) {
            COMMENT -> settings.commentsByDefault
            PROPERTY_VALUE -> settings.localisationTextsByDefault
            PARAMETER -> settings.localisationParametersFullyByDefault
            COMMAND -> settings.localisationCommandsByDefault
            CONCEPT_COMMAND -> settings.localisationConceptCommandsByDefault
            CONCEPT_STRING -> settings.localisationConceptStringsByDefault
            ICON -> settings.localisationIconsFullyByDefault
            TEXT_ICON -> settings.localisationTextIconsFullyByDefault
            TEXT_FORMAT -> settings.localisationTextFormatsByDefault
            TEXT_FORMAT_STRING -> settings.localisationTextFormatStringsByDefault
            else -> false
        }
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == ParadoxLocalisationFile.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
