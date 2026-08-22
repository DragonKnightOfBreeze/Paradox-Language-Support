package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.inspections.InspectionService
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.fixes.ReplaceWithExpressionFix
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.match.util.ParadoxMatchProvider
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.model.constants.ChronicleStrings
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
                    if (ParadoxMatchProvider.matchesFloatPercentageField(element.value)) {
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

        if (element is ParadoxCsvColumn && ParadoxCsvPsiService.isEmptyColumn(element)) {
            return LocalQuickFix.EMPTY_ARRAY // in case
        }
        val result = mutableListOf<LocalQuickFix>()
        result += ParadoxExpressionInspectionService.getSimilarityBasedFixesForUnresolvedExpression(element, expectedConfigs)
        result += ParadoxExpressionInspectionService.getLocalisationReferenceFixesForUnresolvedExpression(element, expectedConfigs)
        return result.toArray(LocalQuickFix.EMPTY_ARRAY)
    }
}

/**
 * 如果期望布尔值，但实际上是一个格式错误的标识符（如 `true` `Yes` `ON`），则提供额外的快速修复。
 */
class ParadoxWrongBooleanUnresolvedExpressionChecker : ParadoxUnresolvedExpressionChecker {
    override fun check(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean {
        // for boolean only (after expansion)
        val configExpression = ProcessorScope.findFrom({ expectedConfigs.expandConfigExpression { process(it) } }) { it.type == CwtDataTypes.Bool }
        if (configExpression == null) return true

        if (element !is ParadoxScriptString && element !is ParadoxCsvColumn) return true
        val text = element.text
        if (text.isEmpty()) return true
        // if (text == ChronicleStrings.yesKeyword || text == ChronicleStrings.noKeyword) return true // should not be
        val value = element.value
        if (value.isEmpty()) return true
        val expected = when {
            value.equals("yes", true) || value.equals("true", true) || value.equals("on", true) -> ChronicleStrings.yesKeyword
            value.equals("no", true) || value.equals("false", true) || value.equals("off", true) -> ChronicleStrings.noKeyword
            else -> null
        } ?: return true
        val description = when {
            context.showExpect -> ChronicleEpBundle.message("unresolvedExpression.wrongBoolean.desc.1", expected, text)
            else -> ChronicleEpBundle.message("unresolvedExpression.wrongBoolean.desc.0")
        }
        val fix = ReplaceWithExpressionFix(expected)
        context.holder.registerProblem(element, description, fix)
        return false
    }
}
