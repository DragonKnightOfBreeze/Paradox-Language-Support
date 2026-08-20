package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
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
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.settings.ChronicleInternalSettings
import icu.windea.pls.lang.tagType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.model.orSpecific
import icu.windea.pls.script.psi.ParadoxParameterAwareElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDataExpression

object ParadoxExpressionInspectionService {
    fun createContext(tool: LocalInspectionTool, holder: ProblemsHolder, ignoredByConfig: Boolean = false, showExpectInfo: Boolean = true): ParadoxExpressionInspectionContext {
        val gameType = selectGameType(holder.file)
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, gameType)
        return ParadoxExpressionInspectionContext(tool, holder, configGroup, ignoredByConfig, showExpectInfo)
    }

    // unresolvedExpression

    fun checkForUnresolvedExpression(element: ParadoxScriptExpressionElement, context: ParadoxExpressionInspectionContext) {
        if (!element.isDataExpression()) return // skip if is not a data expression

        // skip if it is parameterized
        if (element is ParadoxParameterAwareElement && element.text.isParameterized()) return

        // skip if it is a special tag (Do not consider whether matched configs exist)
        if (element is ParadoxScriptString && element.tagType != null) return

        // 如果表达式是块（`{...}`），需要递归向下检查其中的表达式（键/值）
        // 如果存在错误，则不再递归向下检查
        // 如果匹配的规则的数据类型为 `Any`，则不再递归向下检查
        // 如果因为其他原因被忽略，仍然需要在必要时递归向下检查

        // TODO 3.0.2 #386

        // skip if config context not exists
        val configContext = ParadoxConfigManager.getConfigContext(element) ?: return
        // skip if config context should be skipped (mainly by member path and member role)
        if (configContext.skipUnresolvedExpressionCheck()) return

        // skip if there are no context configs
        if (configContext.getConfigs().isEmpty()) return

        // skip if there are any matched configs
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(fallback = false))
        if (configs.isNotEmpty()) return

        val expectedConfigs = getExpectedConfigs(element, configContext)
        if (skipForUnresolvedExpression(element, expectedConfigs, context)) return

        applyUnresolvedExpressionCheckers(element, expectedConfigs, context)
    }

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

    private fun getExpectedConfigs(element: ParadoxScriptExpressionElement, configContext: CwtConfigContext): List<CwtMemberConfig<*>> {
        when (element) {
            is ParadoxScriptPropertyKey -> {
                // 这里使用合并后的子规则，即使 parentProperty 可以精确匹配
                val parentMemberElement = element.parentOfType<ParadoxScriptMember>() ?: return emptyList()
                val parentConfigContext = ParadoxConfigManager.getConfigContext(parentMemberElement) ?: return emptyList()
                return buildList {
                    val parentContextConfigs = parentConfigContext.getConfigs()
                    parentContextConfigs.forEachFast f@{ parentContextConfig ->
                        parentContextConfig.configs?.forEachFast f1@{ contextConfig ->
                            val c = contextConfig as? CwtPropertyConfig ?: return@f1
                            // 优先使用重载后的规则
                            ParadoxConfigManager.collectConfigWithOverridden(element, c, this)
                        }
                    }
                }
            }
            is ParadoxScriptValue -> {
                return buildList {
                    val contextConfigs = configContext.getConfigs()
                    contextConfigs.forEachFast f@{ contextConfig ->
                        val c = contextConfig as? CwtValueConfig ?: return@f
                        // 优先使用重载后的规则
                        ParadoxConfigManager.collectConfigWithOverridden(element, c, this)
                    }
                }
            }
            else -> return emptyList()
        }
    }

    private fun getExpectedConfigs(columnConfig: CwtPropertyConfig): List<CwtValueConfig> {
        val valueConfig = columnConfig.valueConfig ?: return emptyList()
        return listOf(valueConfig)
    }

    private fun skipForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean {
        if (expectedConfigs.isEmpty()) return false
        if (context.ignoredByConfig && ParadoxConfigManager.checkExtendedConfig(element, expectedConfigs)) return true
        return false
    }

    fun applyUnresolvedExpressionCheckers(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean {
        val gameType = context.gameType
        val checkers = ParadoxUnresolvedExpressionChecker.getAll()
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

    fun getDefaultLocationForUnresolvedExpression(element: ParadoxExpressionElement): PsiElement {
        if (element is ParadoxCsvColumn && ParadoxCsvPsiService.isEmptyColumn(element)) {
            return ParadoxCsvPsiService.getLocationForEmptyColumn(element) // in case
        }
        return element
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

    fun checkForIncorrectExpression(element: ParadoxScriptExpressionElement, context: ParadoxExpressionInspectionContext) {
        if (!element.isDataExpression()) return // skip if is not a data expression
        if (element is ParadoxScriptBlock) return // skip
        if (element is ParadoxScriptBoolean) return // skip

        // 得到完全匹配的规则
        val config = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(fallback = false)).firstOrNull() ?: return

        // 开始检查
        applyIncorrectExpressionCheckers(element, config, context)

        // TODO 1.3.26+ 应当也适用于各种复杂表达式中的数据源
    }

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
        val checkers = ParadoxIncorrectExpressionChecker.getAll()
        checkers.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.check(element, config, context)
            if (!r) return false
        }
        return true
    }
}
