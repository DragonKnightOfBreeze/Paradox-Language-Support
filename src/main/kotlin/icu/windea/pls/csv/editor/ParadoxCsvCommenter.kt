package icu.windea.pls.csv.editor

import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.psi.PsiComment
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes

class ParadoxCsvCommenter : CodeDocumentationAwareCommenter {
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
