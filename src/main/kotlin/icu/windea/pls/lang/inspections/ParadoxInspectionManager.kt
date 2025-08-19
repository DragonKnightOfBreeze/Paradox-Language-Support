package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.ep.inspection.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.*
import java.util.regex.*

object ParadoxInspectionManager {
    private val SUPPRESS_IN_LINE_COMMENT_PATTERN = Pattern.compile("#" + SuppressionUtil.COMMON_SUPPRESS_REGEXP + ".*")

    fun getCommentsForSuppression(element: PsiElement): Sequence<PsiElement> {
        return if (element is PsiFile) {
            val context = element.firstChild ?: return emptySequence()
            context.siblings(forward = true, withSelf = true)
                .takeWhile { it is PsiWhiteSpace || it is PsiComment }
                .filter { it is PsiComment }
        } else {
            val context = element
            context.siblings(forward = false, withSelf = false)
                .takeWhile { it is PsiWhiteSpace || it is PsiComment }
                .filter { it is PsiComment }
        }
    }

    fun isSuppressedInComment(element: PsiElement, toolId: String): Boolean {
        val comments = getCommentsForSuppression(element)
        for (comment in comments) {
            val matcher = SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(comment.text)
            if (matcher.matches()) {
                if (SuppressionUtil.isInspectionToolIdMentioned(matcher.group(1), toolId)) {
                    return true
                }
            }
        }
        return false
    }

    fun isSuppressedForDefinition(element: PsiElement, toolId: String): Boolean {
        if (element !is ParadoxScriptDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false
        val suppressedToolIds = ParadoxDefinitionInspectionSuppressionProvider.getSuppressedToolIds(element, definitionInfo)
        return toolId in suppressedToolIds
    }
}
