package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.match.similarity.SimilarityMatchOptions
import icu.windea.pls.core.match.similarity.SimilarityMatchService
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.quickfix.GenerateLocalisationsFix
import icu.windea.pls.lang.quickfix.GenerateLocalisationsInFileFix
import icu.windea.pls.lang.quickfix.ReplaceWithSimilarExpressionFix
import icu.windea.pls.lang.quickfix.ReplaceWithSimilarExpressionInListFix
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContextBuilder
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object PlsInspectionUtil {
    fun isConfigGroupInitialized(file: PsiFile): Boolean {
        val project = file.project
        return PlsFacade.getConfigGroup(project).initialized
            && PlsFacade.getConfigGroup(project, selectGameType(file)).initialized
    }

    context(tool: LocalInspectionTool)
    fun getWeakerHighlightType(condition: Boolean = true): ProblemHighlightType {
        if (!condition) return ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        val current = tool.defaultLevel.severity
        return when {
            current > HighlightSeverity.WARNING -> ProblemHighlightType.WARNING
            current > HighlightSeverity.WEAK_WARNING -> ProblemHighlightType.WEAK_WARNING
            else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        }
    }

    fun getSimilarityBasedFixesForUnresolvedExpression(
        element: ParadoxExpressionElement,
        expectedConfigs: List<CwtMemberConfig<*>>
    ): List<LocalQuickFix> {
        val literals = CwtConfigManager.findLiterals(expectedConfigs)
        if (literals.isEmpty()) return emptyList()

        val input = element.value
        if (input.isEmpty()) return emptyList()
        val ignoreCase = when (element) {
            is ParadoxScriptStringExpressionElement -> true
            is ParadoxCsvColumn -> true
            else -> false
        }
        val options = if (ignoreCase) SimilarityMatchOptions.IGNORE_CASE else SimilarityMatchOptions.DEFAULT

        // 查询输入项的最佳匹配，但排除完全匹配的相似项
        val matches = SimilarityMatchService.findBestMatches(input, literals, options).filter { it.score < 1.0 }
        if (matches.isEmpty()) return emptyList()

        // 为最匹配的项提供单独的快速修复（直接替换）
        // 如果匹配项不唯一，再为所有匹配项提供一个快速修复（弹出列表） - 如果分别提供快速修复，这些快速修复最终会按名字正序排序（这不符合预期）
        val fixes = mutableListOf<LocalQuickFix>()
        val first = matches.first()
        fixes += ReplaceWithSimilarExpressionFix(element, first)
        val remain = matches.drop(1)
        if (remain.isNotEmpty()) {
            fixes += ReplaceWithSimilarExpressionInListFix(element, matches)
        }

        return fixes
    }

    fun getLocalisationReferenceFixesForUnresolvedExpression(
        element: ParadoxExpressionElement,
        expectedConfigs: List<CwtMemberConfig<*>>
    ): List<LocalQuickFix> {
        if (element !is ParadoxScriptStringExpressionElement) return emptyList()
        val locales = ParadoxLocaleManager.getLocaleConfigs()
        val context = expectedConfigs.firstNotNullOfOrNull {
            ParadoxLocalisationCodeInsightContextBuilder.fromReference(element, it, locales, fromInspection = true)
        }
        if (context == null) return emptyList()
        return listOf(
            GenerateLocalisationsFix(element, context),
            GenerateLocalisationsInFileFix(element),
        )
    }
}

