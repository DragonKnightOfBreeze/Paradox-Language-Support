package icu.windea.pls.core.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
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
    //1.1.2 TODO 考虑提取成扩展点
    //1.1.2 禁用继承自其他事件的事件的某些检查
    if(definitionInfo.type == "event" && definitionInfo.subtypes.contains("inherited")) {
        if(toolId == "ParadoxScriptMissingExpression") return true
        if(toolId == "ParadoxScriptMissingLocalisation") return true
        if(toolId == "ParadoxScriptMissingImage") return true
    }
    if(definitionInfo.gameType == ParadoxGameType.Stellaris) {
        //1.1.2 禁用名字以数字结尾的领袖特质的某些检查
        if(definitionInfo.type == "trait" && definitionInfo.subtypes.contains("leader_trait") && definitionInfo.name.substringAfterLast('_', "").toIntOrNull() != null) {
            if(toolId == "ParadoxScriptMissingLocalisation") return true
            if(toolId == "ParadoxScriptMissingImage") return true
        }
    }
    return false
}
