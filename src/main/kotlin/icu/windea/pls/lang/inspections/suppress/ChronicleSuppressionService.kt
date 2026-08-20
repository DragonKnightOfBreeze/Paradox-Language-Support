package icu.windea.pls.lang.inspections.suppress

import com.intellij.codeInspection.SuppressionUtil
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimizedIfEmpty
import icu.windea.pls.ep.inspections.ParadoxDefinitionInspectionSuppressionProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.orSpecific
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import java.util.regex.Pattern

object ChronicleSuppressionService {
    // com.intellij.codeInspection.SuppressionUtil
    // com.intellij.lang.properties.codeInspection.PropertiesInspectionSuppressor
    // org.intellij.grammar.inspection.BnfInspectionSuppressor

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
        if (element !is ParadoxDefinitionElement) return false
        val definitionInfo = element.definitionInfo ?: return false
        val suppressedToolIds = getSuppressedToolIds(element, definitionInfo)
        return toolId in suppressedToolIds
    }

    fun getSuppressedToolIds(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        val gameType = definitionInfo.gameType
        val result = mutableSetOf<String>()
        val eps = ParadoxDefinitionInspectionSuppressionProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            result += ep.getSuppressedToolIds(definition, definitionInfo)
        }
        return result.optimizedIfEmpty()
    }
}
