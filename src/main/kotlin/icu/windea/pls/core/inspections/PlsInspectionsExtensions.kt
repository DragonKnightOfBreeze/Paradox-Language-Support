package icu.windea.pls.core.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.script.psi.*

fun isSuppressedInComment(element: PsiElement, toolId: String): Boolean {
    val comments = getCommentsForSuppression(element)
    for(comment in comments) {
        val matcher = SuppressionUtil.SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(comment.text)
        if(matcher.matches()) {
            if(SuppressionUtil.isInspectionToolIdMentioned(matcher.group(1), toolId)) {
                return true
            }
        }
    }
    return false
}

fun getCommentsForSuppression(element: PsiElement): Sequence<PsiElement> {
    return if(element is PsiFile) {
        element.firstChild.siblings(forward = true, withSelf = false)
            .takeWhile { it is PsiWhiteSpace || it is PsiComment }
            .filter { it is PsiComment }
    } else {
        element.siblings(forward = false, withSelf = false)
            .takeWhile { it is PsiWhiteSpace || it is PsiComment }
            .filter { it is PsiComment }
    }
}

