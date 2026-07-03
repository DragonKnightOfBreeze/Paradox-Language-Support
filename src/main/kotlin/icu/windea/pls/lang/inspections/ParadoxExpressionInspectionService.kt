package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalQuickFix
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.match.similarity.SimilarityMatchOptions
import icu.windea.pls.core.match.similarity.SimilarityMatchService
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContextBuilder
import icu.windea.pls.lang.fixes.GenerateLocalisationsFix
import icu.windea.pls.lang.fixes.GenerateLocalisationsInFileFix
import icu.windea.pls.lang.fixes.ReplaceWithSimilarExpressionFix
import icu.windea.pls.lang.fixes.ReplaceWithSimilarExpressionInListFix
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import kotlin.collections.plusAssign

object ParadoxExpressionInspectionService {
    fun getSimilarityBasedFixesForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): List<LocalQuickFix> {
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

    fun getLocalisationReferenceFixesForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): List<LocalQuickFix> {
        if (expectedConfigs.isEmpty()) return emptyList()
        if (element !is ParadoxScriptStringExpressionElement) return emptyList()
        val context = expectedConfigs.firstNotNullOfOrNull {
            ParadoxLocalisationCodeInsightContextBuilder.fromReference(element, it, fromInspection = true)
        }
        if (context == null) return emptyList()
        return listOf(
            GenerateLocalisationsFix(element, context),
            GenerateLocalisationsInFileFix(element),
        )
    }
}
