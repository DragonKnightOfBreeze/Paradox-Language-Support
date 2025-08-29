package icu.windea.pls.cwt.editor

import com.intellij.lang.CodeDocumentationAwareCommenterEx
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.cwt.psi.CwtDocComment
import icu.windea.pls.cwt.psi.CwtElementTypes

/**
 * @see icu.windea.pls.inject.injectors.CommentByLineCommentHandlerCodeInjector
 * @see icu.windea.pls.inject.injectors.LineCommentCopyPastePreProcessorCodeInjector
 */
class CwtCommenter : CodeDocumentationAwareCommenterEx {
    companion object {
        const val DOC_COMMENT_PREFIX = "###"
        const val OPTION_COMMENT_PREFIX = "##"
        const val LINE_COMMENT_PREFIX = "#"
    }

    override fun getLineCommentPrefix() = LINE_COMMENT_PREFIX

    override fun getBlockCommentPrefix() = null

    override fun getBlockCommentSuffix() = null

    override fun getCommentedBlockCommentPrefix() = null

    override fun getCommentedBlockCommentSuffix() = null

    override fun getDocumentationCommentPrefix() = DOC_COMMENT_PREFIX

    override fun getDocumentationCommentLinePrefix() = DOC_COMMENT_PREFIX

    override fun getDocumentationCommentSuffix() = null

    override fun getLineCommentTokenType() = CwtElementTypes.COMMENT

    override fun getBlockCommentTokenType() = null

    override fun getDocumentationCommentTokenType() = CwtElementTypes.DOC_COMMENT

    override fun isDocumentationComment(element: PsiComment) = element is CwtDocComment

    override fun isDocumentationCommentText(element: PsiElement) = element.elementType == CwtElementTypes.DOC_COMMENT_TOKEN
}
