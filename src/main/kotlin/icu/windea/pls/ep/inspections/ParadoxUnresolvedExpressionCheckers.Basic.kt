package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.inspections.InspectionService
import icu.windea.pls.core.joinToStringFast
import icu.windea.pls.core.orNull
import icu.windea.pls.core.text.TextService
import icu.windea.pls.core.truncate
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fixes.ParadoxExpressionFixService
import icu.windea.pls.lang.fixes.ReplaceWithExpressionFix
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.match.util.ParadoxMatchFactory
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.isResolvableLiteralExpression
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * - 如果期望整数字段，但实际是一个浮点数字段，则使用更弱的高亮级别。
 * - 如果期望整数百分比字段，但实际是一个浮点数百分比字段，则使用更弱的高亮级别。
 * - 如果期望本地化引用，但实际是一个普通的属性键或字符串，则使用更弱的高亮级别。
 * - 额外提供基于相似度匹配的快速修复（如果可以从期望的规则中提取字面量）。
 * - 额外提供适用于本地化引用的快速修复（生成本地化）。
 */
class ParadoxDefaultUnresolvedExpressionChecker : ParadoxUnresolvedExpressionChecker {
    override fun check(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean {
        val location = ParadoxExpressionInspectionService.getDefaultLocationForUnresolvedExpression(element)
        val description = ParadoxExpressionInspectionService.getDefaultDescriptionForUnresolvedExpression(element, expectedConfigs, context)
        val highlightType = getHighlightType(element, expectedConfigs, context)
        val fixes = getFixes(element, expectedConfigs)
        context.holder.registerProblem(location, description, highlightType, *fixes)
        return false
    }

    private fun getHighlightType(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): ProblemHighlightType {
        // rules:
        // - int field -> actual is float (after resolution) -> use weaker highlight type
        // - int percentage field -> actual is float percentage field -> use weaker highlight type
        // - localisation reference -> expression can be a string literal instead -> use weaker highlight type

        var result = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        expectedConfigs.expandConfigExpression p@{ configExpression ->
            when (configExpression.type) {
                in CwtDataTypeSets.IntField -> {
                    if (element is ParadoxScriptFloat || element.castOrNull<ParadoxScriptedVariableReference>()?.resolved() is ParadoxScriptFloat) {
                        result = InspectionService.getWeakerHighlightType(context.tool)
                        return@p false
                    }
                }
                CwtDataTypes.IntPercentageField -> {
                    if (ParadoxMatchFactory.matchesFloatPercentageField(element.value)) {
                        result = InspectionService.getWeakerHighlightType(context.tool)
                        return@p false
                    }
                }
                in CwtDataTypeSets.LocalisationReference -> {
                    if (element is ParadoxScriptStringExpressionElement) {
                        result = InspectionService.getWeakerHighlightType(context.tool)
                        return@p false
                    }
                }
            }
            true
        }
        return result
    }

    private fun getFixes(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Array<LocalQuickFix> {
        // rules:
        // - add similarity based fixes if expect some literals
        // - add localisation reference fixes if expect some localisation references
        // - add goto config fixes if available

        val result = mutableListOf<LocalQuickFix>()
        ParadoxExpressionFixService.collectSimilarityBasedFixes(element, expectedConfigs, result)
        ParadoxExpressionFixService.collectLocalisationReferenceFixes(element, expectedConfigs, result)
        ParadoxExpressionFixService.collectGotoTargetFixes(element, expectedConfigs, result)
        ParadoxExpressionFixService.collectGotoConfigFixes(element, expectedConfigs, result)
        return result.toArray(LocalQuickFix.EMPTY_ARRAY)
    }
}

/**
 * 如果期望布尔值，但实际上是一个格式错误的标识符（如 `true` `Yes` `ON`），则使用特殊的报错描述和快速修复。
 */
class ParadoxWrongBooleanUnresolvedExpressionChecker : ParadoxUnresolvedExpressionChecker {
    override fun check(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptString && element !is ParadoxCsvColumn) return true

        // for `CwtDataTypes.Bool` only (after expansion)
        val configExpression = ProcessorScope.findFrom({ expectedConfigs.expandConfigExpression { process(it) } }) { it.type == CwtDataTypes.Bool }
        if (configExpression == null) return true

        // val text = element.text
        // if (text.isEmpty()) return true // should not be
        // if (text == ChronicleStrings.yesKeyword || text == ChronicleStrings.noKeyword) return true // should not be
        val value = element.value
        if (value.isEmpty()) return true
        val expectedBoolean = TextService.convertToBooleanLenient(value) ?: return true
        val expected = if (expectedBoolean) ChronicleStrings.yesKeyword else ChronicleStrings.noKeyword
        val description = when {
            context.showExpect -> {
                val text = element.presentableText
                ChronicleEpBundle.message("unresolvedExpression.wrongBoolean.desc.1", expected, text)
            }
            else -> ChronicleEpBundle.message("unresolvedExpression.wrongBoolean.desc.0")
        }
        val fix = ReplaceWithExpressionFix(expected)
        context.holder.registerProblem(element, description, fix)
        return false
    }
}

/**
 * 如果期望定义，但定义的子类型不匹配，则使用特殊的报错描述和快速修复。
 */
@Optimized
class ParadoxSubtypesMismatchedDefinitionUnresolvedExpressionChecker : ParadoxUnresolvedExpressionChecker {
    override fun check(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element is ParadoxScriptExpressionElement && !element.isResolvableLiteralExpression()) return true

        // for `CwtDataTypes.Definition` only (after expansion)
        val configExpressions = ProcessorScope.collectFrom({ expectedConfigs.expandConfigExpression { process(it) } }) { it.type == CwtDataTypes.Definition }
        if (configExpressions.isEmpty()) return true

        val value = element.value
        if (value.isEmpty()) return true
        configExpressions.forEachFast f@{ configExpression ->
            val typeExpression = configExpression.metadata.value?.orNull() ?: return@f
            val type = typeExpression.substringBefore('.', "").orNull() ?: return@f // skip if subtype information exists
            val selector = ParadoxDefinitionSearch.selector(context.project, context.holder.file) // `context.holder.file` is ok here
            val definitionLenientMatched = ParadoxDefinitionSearch.searchElement(value, type, selector).findFirst()
            if (definitionLenientMatched == null) return@f
            val definitionInfoLenientMatched = definitionLenientMatched.definitionInfo
            if (definitionInfoLenientMatched == null) return@f
            val description = when {
                context.showExpect -> {
                    val expectedConfigExpressions = configExpressions.mapFast { it.expressionString }.toSet()
                    val expected = expectedConfigExpressions.truncate(context.truncateExpect).joinToStringFast()
                    val actualTypes = definitionInfoLenientMatched.subtypes.joinToStringFast()
                    ChronicleEpBundle.message("unresolvedExpression.subtypesMismatchedDefinition.desc.1", expected, actualTypes)
                }
                else -> ChronicleEpBundle.message("unresolvedExpression.subtypesMismatchedDefinition.desc.0")
            }
            val fixes = getFixes(element, configExpressions, context.configGroup)
            context.holder.registerProblem(element, description, *fixes)
            return false
        }
        return true
    }

    private fun getFixes(element: ParadoxExpressionElement, expectedConfigExpressions: List<CwtDataExpression>, configGroup: CwtConfigGroup): Array<LocalQuickFix> {
        val result = mutableListOf<LocalQuickFix>()
        ParadoxExpressionFixService.collectGotoTargetFixes(element, expectedConfigExpressions, configGroup, result)
        ParadoxExpressionFixService.collectGotoConfigFixes(element, expectedConfigExpressions, configGroup, result)
        return result.toArray(LocalQuickFix.EMPTY_ARRAY)
    }
}
