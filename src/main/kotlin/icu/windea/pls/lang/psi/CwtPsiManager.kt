package icu.windea.pls.lang.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.cwt.psi.CwtDocComment

object CwtPsiManager {
    fun getOwnedDocComments(element: PsiElement): List<PsiComment> {
        return PsiService.getOwnedComments(element) { it is CwtDocComment }
    }

    @Inferred
    fun getDocCommentText(comments: List<PsiComment>): String? {
        // 如果某行注释至少存在4个前导的 `#`，则将注释文本视为 Markdown 文本
        return PsiService.getDocCommentText(comments) { it.startsWith("####") }
    }
}
