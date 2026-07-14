package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.match.similarity.SimilarityMatchOptions
import icu.windea.pls.core.match.similarity.SimilarityMatchService
import icu.windea.pls.core.truncate
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContextService
import icu.windea.pls.lang.fixes.GenerateLocalisationsFix
import icu.windea.pls.lang.fixes.GenerateLocalisationsInFileFix
import icu.windea.pls.lang.fixes.ReplaceWithSimilarExpressionFix
import icu.windea.pls.lang.fixes.ReplaceWithSimilarExpressionInListFix
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.ChronicleInternalSettings
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxExpressionInspectionService {
    fun createContext(tool: LocalInspectionTool, holder: ProblemsHolder): ParadoxExpressionInspectionContext {
        val gameType = selectGameType(holder.file)
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, gameType)
        return ParadoxExpressionInspectionContext(tool, holder, configGroup)
    }

    fun getDefaultDescriptionForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): String {
        val expression = element.expression
        val expect = when {
            expectedConfigs.isEmpty() -> ""
            showExpectInfo(context) -> expectedConfigs.mapTo(mutableSetOf()) { it.configExpression.expressionString }
                .truncate(ChronicleInternalSettings.getInstance().itemLimit).joinToString()
            else -> null
        }
        val message = when {
            expect == null -> ChronicleBundle.message("unresolvedExpression.desc.withoutExpect", expression)
            expect.isNotEmpty() -> ChronicleBundle.message("unresolvedExpression.desc.withExpect", expression, expect)
            else -> ChronicleBundle.message("unresolvedExpression.desc.noExpect", expression)
        }
        return message
    }

    private fun showExpectInfo(context: ParadoxExpressionInspectionContext): Boolean {
        val tool = context.tool
        return when (tool) {
            is icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection -> tool.showExpectInfo
            is icu.windea.pls.lang.inspections.csv.expression.UnresolvedExpressionInspection -> tool.showExpectInfo
            else -> true
        }
    }

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
            ParadoxLocalisationCodeInsightContextService.fromReference(element, it, fromInspection = true)
        }
        if (context == null) return emptyList()
        return listOf(
            GenerateLocalisationsFix(element, context),
            GenerateLocalisationsInFileFix(element),
        )
    }
}
