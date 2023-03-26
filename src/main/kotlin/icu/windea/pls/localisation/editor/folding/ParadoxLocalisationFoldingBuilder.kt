package icu.windea.pls.localisation.editor.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationFoldingBuilder : CustomFoldingBuilder(), DumbAware {
    override fun getLanguagePlaceholderText(node: ASTNode, range: TextRange): String {
        return ""
    }
    
    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
    
    override fun buildLanguageFoldRegions(descriptors: MutableList<FoldingDescriptor>, root: PsiElement, document: Document, quick: Boolean) {
        
    }
    
    override fun isCustomFoldingRoot(node: ASTNode): Boolean {
        return node.elementType == ParadoxLocalisationParserDefinition.FILE
    }
    
    override fun isCustomFoldingCandidate(node: ASTNode): Boolean {
        return node.elementType == COMMENT
    }
}