package icu.windea.pls.cwt.editor

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.cwt.psi.*

/**
 * @see icu.windea.pls.inject.injectors.CommentByLineCommentHandlerCodeInjector
 * @see icu.windea.pls.inject.injectors.LineCommentCopyPastePreProcessorCodeInjector
 */
class CwtCommenter : CodeDocumentationAwareCommenterEx {
    companion object {
        const val LINE_COMMENT_PREFIX = "#"
        const val OPTION_COMMENT_PREFIX = "##"
        const val DOCUMENTATION_COMMENT_PREFIX = "###"
    }
    
    override fun getLineCommentPrefix() = LINE_COMMENT_PREFIX
    
    override fun getBlockCommentPrefix() = null
    
    override fun getBlockCommentSuffix() = null
    
    override fun getCommentedBlockCommentPrefix() = null
    
    override fun getCommentedBlockCommentSuffix() = null
    
    override fun getDocumentationCommentPrefix() = DOCUMENTATION_COMMENT_PREFIX
    
    override fun getDocumentationCommentLinePrefix() = DOCUMENTATION_COMMENT_PREFIX
    
    override fun getDocumentationCommentSuffix() = null
    
    override fun getLineCommentTokenType() = CwtElementTypes.COMMENT
    
    override fun getBlockCommentTokenType() = null
    
    override fun getDocumentationCommentTokenType() = CwtElementTypes.DOCUMENTATION_COMMENT
    
    override fun isDocumentationComment(element: PsiComment) = element is CwtDocumentationComment
    
    override fun isDocumentationCommentText(element: PsiElement): Boolean {
        val elementType = element.elementType
        return elementType == CwtElementTypes.DOCUMENTATION_TEXT || element == CwtElementTypes.DOCUMENTATION_TOKEN
    }
}
