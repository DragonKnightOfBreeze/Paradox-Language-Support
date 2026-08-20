package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.match.similarity.SimilarityMatchOptions
import icu.windea.pls.core.match.similarity.SimilarityMatchService
import icu.windea.pls.core.truncate
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.ep.inspections.ParadoxIncorrectExpressionChecker
import icu.windea.pls.ep.inspections.ParadoxUnresolvedExpressionChecker
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContextService
import icu.windea.pls.lang.fixes.GenerateLocalisationsFix
import icu.windea.pls.lang.fixes.GenerateLocalisationsInFileFix
import icu.windea.pls.lang.fixes.ReplaceWithSimilarExpressionFix
import icu.windea.pls.lang.fixes.ReplaceWithSimilarExpressionInListFix
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.ChronicleInternalSettings
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.model.orSpecific
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxExpressionInspectionService {
    fun createContext(tool: LocalInspectionTool, holder: ProblemsHolder, ignoredByConfig: Boolean = false, showExpectInfo: Boolean = true): ParadoxExpressionInspectionContext {
        val gameType = selectGameType(holder.file)
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, gameType)
        return ParadoxExpressionInspectionContext(tool, holder, configGroup, ignoredByConfig, showExpectInfo)
    }

    // unresolvedExpression

    fun checkForUnresolvedExpression(element: ParadoxCsvExpressionElement, rowConfig: CwtRowConfig, context: ParadoxExpressionInspectionContext) {
        if (element !is ParadoxCsvColumn) return

        if (ParadoxCsvPsiService.isHeaderColumn(element)) return // skip header columns

        // - 如果不存在对应的列规则，则直接跳过
        // - 如果存在对应的列规则且匹配，则直接跳过
        // - 按需忽略最后一行

        val columnConfig = ParadoxCsvManager.getColumnConfig(element, rowConfig) ?: return // skip (checked by `IncorrectColumnSizeInspection`)
        if (ParadoxCsvManager.isMatchedColumnConfig(element, columnConfig)) return

        val expectedConfigs = getExpectedConfigs(columnConfig)
        if (skipForUnresolvedExpression(element, expectedConfigs, context)) return

        applyUnresolvedExpressionCheckers(element, expectedConfigs, context)
    }

    private fun getExpectedConfigs(columnConfig: CwtPropertyConfig): List<CwtValueConfig> {
        val valueConfig = columnConfig.valueConfig ?: return emptyList()
        return listOf(valueConfig)
    }

    private fun skipForUnresolvedExpression(element: ParadoxCsvExpressionElement, expectedConfigs: List<CwtValueConfig>, context: ParadoxExpressionInspectionContext): Boolean {
        if (expectedConfigs.isEmpty()) return false
        if (context.ignoredByConfig && ParadoxConfigManager.checkExtendedConfig(element, expectedConfigs)) return true
        return false
    }

    fun applyUnresolvedExpressionCheckers(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean {
        val gameType = context.gameType
        val checkers = ParadoxUnresolvedExpressionChecker.EP_NAME.extensionList
        checkers.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.check(element, expectedConfigs, context)
            if (!r) return false
        }
        return true
    }

    fun getDefaultDescriptionForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): String {
        val expression = element.expression
        val expect = when {
            expectedConfigs.isEmpty() -> ""
            context.showExpectInfo -> expectedConfigs.mapFast { it.configExpression.expressionString }.toSet()
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

    // incorrectExpression

    fun checkForIncorrectExpression(element: ParadoxCsvExpressionElement, rowConfig: CwtRowConfig, context: ParadoxExpressionInspectionContext) {
        if (element !is ParadoxCsvColumn) return

        if (ParadoxCsvPsiService.isHeaderColumn(element)) return // skip header columns
        if (ParadoxCsvPsiService.isEmptyColumn(element)) return // skip empty columns

        // 得到完全匹配的规则
        val columnConfig = ParadoxCsvManager.getColumnConfig(element, rowConfig) ?: return // skip (checked by `IncorrectColumnSizeInspection`)
        if (!ParadoxCsvManager.isMatchedColumnConfig(element, columnConfig)) return // skip (checked by `UnresolvedExpressionInspection`)
        val config = columnConfig.valueConfig ?: return

        // 开始检查
        applyIncorrectExpressionCheckers(element, config, context)
    }

    private fun applyIncorrectExpressionCheckers(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        val gameType = context.gameType
        val checkers = ParadoxIncorrectExpressionChecker.EP_NAME.extensionList
        checkers.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.check(element, config, context)
            if (!r) return false
        }
        return true
    }
}
