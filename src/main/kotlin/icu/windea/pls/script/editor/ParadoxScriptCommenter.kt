package icu.windea.pls.script.editor

import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.psi.PsiComment
import icu.windea.pls.script.psi.ParadoxScriptElementTypes

class ParadoxScriptCommenter : CodeDocumentationAwareCommenter {
    override fun getLineCommentPrefix() = "#"

    override fun getBlockCommentPrefix() = null

    override fun getBlockCommentSuffix() = null

    override fun getCommentedBlockCommentPrefix() = null

    override fun getCommentedBlockCommentSuffix() = null

    override fun getDocumentationCommentPrefix() = null

    override fun getDocumentationCommentLinePrefix() = null

    override fun getDocumentationCommentSuffix() = null

    override fun getLineCommentTokenType() = ParadoxScriptElementTypes.COMMENT

    override fun getBlockCommentTokenType() = null

    override fun getDocumentationCommentTokenType() = null

    override fun isDocumentationComment(element: PsiComment?) = false
}
