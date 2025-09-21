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
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.findChild
import icu.windea.pls.core.forEachChild
import icu.windea.pls.lang.settings.PlsSettingsState
import icu.windea.pls.lang.util.psi.PlsPsiManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMAND
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.COMMENT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.CONCEPT_COMMAND
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.CONCEPT_TEXT
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.ICON
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PARAMETER
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiUtil
import icu.windea.pls.model.constants.PlsStringConstants

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
            PARAMETER -> settings.localisationParametersFullyByDefault
            ICON -> settings.localisationIconsFullyByDefault
            COMMAND -> settings.localisationCommandsByDefault
            CONCEPT_COMMAND -> settings.localisationConceptCommandsByDefault
            CONCEPT_TEXT -> settings.localisationConceptTextsByDefault
            else -> false
        }
    }

    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = PlsFacade.getSettings().folding
        collectDescriptors(root, descriptors, settings)
    }

    private fun collectDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState) {
        collectCommentDescriptors(element, descriptors, settings)
        val r = collectOtherDescriptors(element, descriptors, settings)
        if (!r) return
        element.forEachChild { collectDescriptors(it, descriptors, settings) }
    }

    private fun collectCommentDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState) {
        if (!settings.comment) return
        val allSiblingLineComments = PlsPsiManager.findAllSiblingLineCommentsIn(element) { it.elementType == COMMENT }
        if (allSiblingLineComments.isEmpty()) return
        allSiblingLineComments.forEach {
            val startOffset = it.first().startOffset
            val endOffset = it.last().endOffset
            val descriptor = FoldingDescriptor(it.first(), TextRange(startOffset, endOffset))
            descriptors.add(descriptor)
        }
    }

    private fun collectOtherDescriptors(element: PsiElement, descriptors: MutableList<FoldingDescriptor>, settings: PlsSettingsState.FoldingState): Boolean {
        when (element.elementType) {
            PARAMETER -> run {
                if (!settings.localisationParametersFully) return@run
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
            ICON -> run {
                if (!settings.localisationIconsFully) return@run
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
            COMMAND -> run {
                if (!settings.localisationCommands) return@run
                descriptors.add(FoldingDescriptor(element.node, element.textRange, null, PlsStringConstants.commandFolder))
            }
            CONCEPT_COMMAND -> run {
                if (!settings.localisationConceptCommands) return@run
                val conceptTextNode = element.findChild { it.elementType == CONCEPT_TEXT }
                val placeholder = if (conceptTextNode == null) PlsStringConstants.conceptCommandFolder else PlsStringConstants.conceptCommandWithTextFolder
                descriptors.add(FoldingDescriptor(element.node, element.textRange, null, placeholder))
            }
            CONCEPT_TEXT -> run {
                if (!settings.localisationConceptTexts) return@run
                descriptors.add(FoldingDescriptor(element.node, element.textRange))
            }
        }
        return ParadoxLocalisationPsiUtil.isRichTextContextElement(element)
    }

    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == ParadoxLocalisationFile.ELEMENT_TYPE
    }

    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}
