package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtCommenter : CodeDocumentationAwareCommenter {
    override fun getLineCommentPrefix() = "#"
    
    override fun getBlockCommentPrefix() = null
    
    override fun getBlockCommentSuffix() = null
    
    override fun getCommentedBlockCommentPrefix() = null
    
    override fun getCommentedBlockCommentSuffix() = null
    
    override fun getDocumentationCommentPrefix() = "###"
    
    override fun getDocumentationCommentLinePrefix() = "###"
    
    override fun getDocumentationCommentSuffix() = null
    
    override fun getLineCommentTokenType() = CwtElementTypes.COMMENT
    
    override fun getBlockCommentTokenType() = null
    
    override fun getDocumentationCommentTokenType() = CwtElementTypes.DOCUMENTATION_COMMENT
    
    override fun isDocumentationComment(element: PsiComment) = element is CwtDocumentationComment
}
