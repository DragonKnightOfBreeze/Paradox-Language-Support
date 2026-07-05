package icu.windea.pls.lang.inspections.suppress

import com.intellij.codeInsight.daemon.impl.actions.SuppressByCommentFix
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.inspections.ChronicleSuppressionUtil

open class ParadoxSuppressByCommentFix(
    toolId: String,
    suppressionHolderClass: Class<out PsiElement>
) : SuppressByCommentFix(toolId, suppressionHolderClass) {
    override fun getCommentsFor(container: PsiElement): List<PsiElement> {
        return ChronicleSuppressionUtil.getCommentsForSuppression(container).toList()
    }
}
