package icu.windea.pls.localisation.editor

import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes
import icu.windea.pls.script.psi.*

class ParadoxLocalisationCommenter : CodeDocumentationAwareCommenter {
    override fun getLineCommentPrefix() = "#"
    
    override fun getBlockCommentPrefix() = null
    
    override fun getBlockCommentSuffix() = null
    
    override fun getCommentedBlockCommentPrefix() = null
    
    override fun getCommentedBlockCommentSuffix() = null
    
    override fun getDocumentationCommentPrefix() = null
    
    override fun getDocumentationCommentLinePrefix() = null
    
    override fun getDocumentationCommentSuffix() = null
    
    override fun getLineCommentTokenType() = ParadoxLocalisationElementTypes.COMMENT
    
    override fun getBlockCommentTokenType() = null
    
    override fun getDocumentationCommentTokenType() = null
    
    override fun isDocumentationComment(element: PsiComment?) = false
}
