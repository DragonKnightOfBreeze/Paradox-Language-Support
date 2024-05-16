package icu.windea.pls.localisation.editor.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.lang.editor.folding.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String? {
        return when(node.elementType) {
            PROPERTY_REFERENCE -> ""
            ICON -> ""
            else -> null
        }
    }
    
    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        return when(node.elementType) {
            PROPERTY_REFERENCE -> ParadoxFoldingSettings.getInstance().localisationReferencesFully
            ICON -> ParadoxFoldingSettings.getInstance().localisationIconsFully
            else -> false
        }
    }
    
    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        val settings = ParadoxFoldingSettings.getInstance()
        collectDescriptorsRecursively(root.node, document, descriptors, settings)
    }
    
    private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>, settings: ParadoxFoldingSettings) {
        when(node.elementType) {
            COMMENT -> return //optimization
            LOCALE -> return //optimization
            PROPERTY_REFERENCE -> {
                if(settings.localisationReferencesFully) descriptors.add(FoldingDescriptor(node, node.textRange))
            }
            ICON -> {
                if(settings.localisationIconsFully) descriptors.add(FoldingDescriptor(node, node.textRange))
            }
        }
        val children = node.getChildren(null)
        for(child in children) {
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