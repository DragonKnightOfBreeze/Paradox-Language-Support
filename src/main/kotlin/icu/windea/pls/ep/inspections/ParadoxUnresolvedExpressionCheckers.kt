package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.inspections.InspectionService
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.match.ParadoxMatchProvider
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.script.psi.ParadoxScriptFloat
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
        val location = ParadoxExpressionInspectionService.getLocation(element)
        val description = ParadoxExpressionInspectionService.getDefaultDescriptionForUnresolvedExpression(element, expectedConfigs, context)
        val highlightType = getHighlightType(element, expectedConfigs, context)
        val fixes = getFixes(element, expectedConfigs)
        context.holder.registerProblem(location, description, highlightType, *fixes)
        return false
    }

    private fun getHighlightType(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): ProblemHighlightType {
        // int, int value field, int variable field -> actual is float (after resolution) -> use weaker highlight type
        if (expectedConfigs.any { it.configExpression.type in CwtDataTypeSets.IntField }) {
            if (element is ParadoxScriptFloat || element.castOrNull<ParadoxScriptedVariableReference>()?.resolved() is ParadoxScriptFloat) {
                return getWeakerHighlightType(context)
            }
        }
        // int percentage field -> actual is float percentage field -> use weaker highlight type
        if (expectedConfigs.any { it.configExpression.type == CwtDataTypes.IntPercentageField }) {
            if (ParadoxMatchProvider.matchesFloatPercentageField(element.value)) {
                return getWeakerHighlightType(context)
            }
        }
        // localisation reference -> expression can be a string literal instead -> use weaker highlight type
        if (expectedConfigs.any { it.configExpression.type in CwtDataTypeSets.LocalisationReference }) {
            if (element is ParadoxScriptStringExpressionElement) {
                return getWeakerHighlightType(context)
            }
        }
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING
    }

    private fun getWeakerHighlightType(context: ParadoxExpressionInspectionContext): ProblemHighlightType {
        return with(context.tool) { InspectionService.getWeakerHighlightType() }
    }

    private fun getFixes(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Array<LocalQuickFix> {
        val result = mutableListOf<LocalQuickFix>()
        result += ParadoxExpressionInspectionService.getSimilarityBasedFixesForUnresolvedExpression(element, expectedConfigs)
        result += ParadoxExpressionInspectionService.getLocalisationReferenceFixesForUnresolvedExpression(element, expectedConfigs)
        return result.toArray(LocalQuickFix.EMPTY_ARRAY)
    }
}
