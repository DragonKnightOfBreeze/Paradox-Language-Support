package icu.windea.pls.lang.fixes

import com.intellij.codeInspection.LocalQuickFix
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.match.similarity.SimilarityMatchOptions
import icu.windea.pls.core.match.similarity.SimilarityMatchService
import icu.windea.pls.core.orNull
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContextFactory
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

@Optimized
object ParadoxExpressionFixService {
    fun collectSimilarityBasedFixes(element: ParadoxExpressionElement, configs: List<CwtMemberConfig<*>>, result: MutableList<LocalQuickFix>) {
        val literals = CwtConfigManager.findLiterals(configs)
        if (literals.isEmpty()) return

        val input = element.value
        if (input.isEmpty()) return
        val ignoreCase = when (element) {
            is ParadoxScriptStringExpressionElement -> true
            is ParadoxCsvColumn -> true
            else -> false
        }
        val options = if (ignoreCase) SimilarityMatchOptions.IGNORE_CASE else SimilarityMatchOptions.DEFAULT

        // 查询输入项的最佳匹配，但排除完全匹配的相似项
        val matches = SimilarityMatchService.findBestMatches(input, literals, options).filter { it.score < 1.0 }
        if (matches.isEmpty()) return

        // 为最匹配的项提供单独的快速修复（直接替换）
        // 如果匹配项不唯一，再为所有匹配项提供一个快速修复（弹出列表） - 如果分别提供快速修复，这些快速修复最终会按名字正序排序（这不符合预期）
        val first = matches.first()
        result += ReplaceWithSimilarExpressionFix(element, first)
        val remain = matches.drop(1)
        if (remain.isNotEmpty()) {
            result += ReplaceWithSimilarExpressionInListFix(element, matches)
        }
    }

    fun collectLocalisationReferenceFixes(element: ParadoxExpressionElement, configs: List<CwtMemberConfig<*>>, result: MutableList<LocalQuickFix>) {
        if (configs.isEmpty()) return
        if (element !is ParadoxScriptStringExpressionElement) return
        configs.forEachFast f@{ config ->
            val context = ParadoxLocalisationCodeInsightContextFactory.fromReference(element, config, fromInspection = true) ?: return@f
            result += GenerateLocalisationsFix(element, context)
            result += GenerateLocalisationsInFileFix(element)
            return
        }
    }

    fun collectGotoTargetFixes(element: ParadoxExpressionElement, configs: List<CwtMemberConfig<*>>, result: MutableList<LocalQuickFix>) {
        configs.forEachFast { config ->
            collectGotoTargetFixes(element, config.configExpression, config.configGroup, result)
        }
    }

    fun collectGotoTargetFixes(element: ParadoxExpressionElement, configExpressions: List<CwtDataExpression>, configGroup: CwtConfigGroup, result: MutableList<LocalQuickFix>) {
        configExpressions.forEachFast { configExpression ->
            collectGotoTargetFixes(element, configExpression, configGroup, result)
        }
    }

    private fun collectGotoTargetFixes(element: ParadoxExpressionElement, configExpression: CwtDataExpression, configGroup: CwtConfigGroup, result: MutableList<LocalQuickFix>) {
        when (configExpression.type) {
            CwtDataTypes.Definition -> {
                val typeExpression = configExpression.metadata.value?.orNull() ?: return
                val type = typeExpression.substringBefore('.')
                collectGotoTargetFixesFromType(element, type, configGroup, result)
            }
            CwtDataTypes.TechnologyWithLevel -> {
                val type = ParadoxDefinitionTypes.technology
                collectGotoTargetFixesFromType(element, type, configGroup, result)
            }
        }
    }

    private fun collectGotoTargetFixesFromType(element: ParadoxExpressionElement, type: String, configGroup: CwtConfigGroup, result: MutableList<LocalQuickFix>) {
        val value = element.value
        if (value.isEmpty()) return
        val selector = ParadoxDefinitionSearch.selector(configGroup.project, element)
        val definitionLenientMatched = ParadoxDefinitionSearch.searchElement(value, type, selector).findFirst()
        if (definitionLenientMatched == null) return
        val target = definitionLenientMatched
        val message = ChronicleInspectionBundle.message("fix.gotoTarget.fix.definition", value)
        result += GotoTargetFix(element, message, target.createPointer())
    }

    fun collectGotoConfigFixes(element: ParadoxExpressionElement, configs: List<CwtMemberConfig<*>>, result: MutableList<LocalQuickFix>) {
        configs.forEachFast { config ->
            collectGotoConfigFixes(element, config.configExpression, config.configGroup, result)
        }
    }

    fun collectGotoConfigFixes(element: ParadoxExpressionElement, configExpressions: List<CwtDataExpression>, configGroup: CwtConfigGroup, result: MutableList<LocalQuickFix>) {
        configExpressions.forEachFast { configExpression ->
            collectGotoConfigFixes(element, configExpression, configGroup, result)
        }
    }

    private fun collectGotoConfigFixes(element: ParadoxExpressionElement, configExpression: CwtDataExpression, configGroup: CwtConfigGroup, result: MutableList<LocalQuickFix>) {
        when (configExpression.type) {
            CwtDataTypes.Definition -> {
                val typeExpression = configExpression.metadata.value?.orNull() ?: return
                val type = typeExpression.substringBefore('.')
                collectGotoConfigFixesFromType(element, type, configGroup, result)
            }
            CwtDataTypes.EnumValue -> {
                val enumName = configExpression.metadata.value?.orNull() ?: return
                collectGotoConfigFixesFromEnumName(element, enumName, configGroup, result)
            }
            CwtDataTypes.TechnologyWithLevel -> {
                val type = ParadoxDefinitionTypes.technology
                collectGotoConfigFixesFromType(element, type, configGroup, result)
            }
        }
    }

    private fun collectGotoConfigFixesFromType(element: ParadoxExpressionElement, type: String, configGroup: CwtConfigGroup, result: MutableList<LocalQuickFix>) {
        val config = configGroup.types[type] ?: return
        val message = ChronicleInspectionBundle.message("fix.gotoConfig.fix.type", config.name)
        result += GotoConfigFix(element, message, config)
    }

    private fun collectGotoConfigFixesFromEnumName(element: ParadoxExpressionElement, enumName: String, configGroup: CwtConfigGroup, result: MutableList<LocalQuickFix>) {
        run {
            val config = configGroup.complexEnums[enumName] ?: return@run
            val message = ChronicleInspectionBundle.message("fix.gotoConfig.fix.complexEnum", config.name)
            result += GotoConfigFix(element, message, config)
        }
        run {
            val config = configGroup.enums[enumName] ?: return@run
            val message = ChronicleInspectionBundle.message("fix.gotoConfig.fix.enum", config.name)
            result += GotoConfigFix(element, message, config)
        }
    }
}
