package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.inspections.InspectionService
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.match.ParadoxMatchProvider
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 额外提供基于相似度匹配的快速修复。
 */
class ParadoxSimilarityBasedUnresolvedExpressionDecorator : ParadoxUnresolvedExpressionDecorator {
    context(tool: LocalInspectionTool)
    override fun collectFixes(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, result: MutableList<LocalQuickFix>) {
        result += ParadoxExpressionInspectionService.getSimilarityBasedFixesForUnresolvedExpression(element, expectedConfigs)
    }
}

/**
 * 如果期望整数字段，但实际是一个浮点数字段，则降低高亮级别。
 */
class ParadoxIntFieldUnresolvedExpressionDecorator : ParadoxUnresolvedExpressionDecorator {
    context(tool: LocalInspectionTool)
    override fun getHighlightType(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): ProblemHighlightType? {
        // int, int value field, int variable field -> actual is float (after resolution) -> use weaker highlight type
        if (expectedConfigs.any { it.configExpression.type in CwtDataTypeSets.Int }) {
            if (element is ParadoxScriptFloat || element.castOrNull<ParadoxScriptedVariableReference>()?.resolved() is ParadoxScriptFloat) {
                return InspectionService.getWeakerHighlightType()
            }
        }
        // int percentage field -> actual is float percentage field -> use weaker highlight type
        if (expectedConfigs.any { it.configExpression.type == CwtDataTypes.IntPercentageField }) {
            if (ParadoxMatchProvider.matchesFloatPercentageField(element.value)) {
                return InspectionService.getWeakerHighlightType()
            }
        }
        return null
    }
}

/**
 * 如果期望本地化引用，但实际是一个普通的属性键或字符串，则降低高亮级别，并额外提供相关的快速修复（生成本地化）。
 */
class ParadoxLocalisationReferenceUnresolvedExpressionDecorator : ParadoxUnresolvedExpressionDecorator {
    context(tool: LocalInspectionTool)
    override fun getHighlightType(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): ProblemHighlightType? {
        // localisation reference -> expression can be a string literal instead -> use weaker highlight type
        if (expectedConfigs.any { it.configExpression.type in CwtDataTypeSets.LocalisationReference }) {
            if (element is ParadoxScriptStringExpressionElement) {
                return InspectionService.getWeakerHighlightType()
            }
        }
        return null
    }

    context(tool: LocalInspectionTool)
    override fun collectFixes(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, result: MutableList<LocalQuickFix>) {
        result += ParadoxExpressionInspectionService.getLocalisationReferenceFixesForUnresolvedExpression(element, expectedConfigs)
    }
}
