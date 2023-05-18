package icu.windea.pls.core.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import java.util.regex.*

private val SUPPRESS_IN_LINE_COMMENT_PATTERN = Pattern.compile("#" + SuppressionUtil.COMMON_SUPPRESS_REGEXP + ".*")

fun isSuppressedInComment(element: PsiElement, toolId: String): Boolean {
    val comments = getCommentsForSuppression(element)
    for(comment in comments) {
        val matcher = SUPPRESS_IN_LINE_COMMENT_PATTERN.matcher(comment.text)
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

fun isSuppressedForDefinition(element: PsiElement, toolId: String) : Boolean {
    if(element !is ParadoxScriptDefinitionElement) return false
    val definitionInfo = element.definitionInfo ?: return false
    //0.10.3 禁用继承自其他事件的事件的 ParadoxScriptMissingExpression 检查 - 这并不准确，但目前就这样处理吧
    if(definitionInfo.type == "event" && definitionInfo.subtypes.contains("inherited")) {
        if(toolId == "ParadoxScriptMissingExpression") return true
    }
    return false
}
