package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.parentOfType
import icu.windea.pls.base.annotations.WithGameType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.intRange
import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.floatValue
import icu.windea.pls.lang.psi.intValue
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.evaluators.ParadoxComplexExpressionEvaluator
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 检查整数数值是否在指定的区间内。
 */
class ParadoxRangedIntFieldChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptExpressionElement && element !is ParadoxCsvExpressionElement) return true

        // for int field only (after expansion)
        val configExpression = config.expandConfigExpression().find { it.type in CwtDataTypeSets.IntField }
        if (configExpression == null) return true

        val intRange = configExpression.intRange ?: return true
        val intValue = when {
            element is ParadoxScriptExpressionElement -> element.resolved()?.intValue()
            else -> element.value.toIntOrNull()
        } ?: return true
        if (intValue in intRange) return true
        val description = ChronicleEpBundle.message("incorrectExpression.range.desc", intRange.expression, intValue)
        context.holder.registerProblem(element, description)
        return false
    }
}

/**
 * 检查浮点数数值是否在指定的区间内。
 */
class ParadoxRangedFloatFieldChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptExpressionElement && element !is ParadoxCsvExpressionElement) return true

        // for float field only (after expansion)
        val configExpression = config.expandConfigExpression().find { it.type in CwtDataTypeSets.FloatField }
        if (configExpression == null) return true

        val floatRange = configExpression.floatRange ?: return true
        val floatValue = when {
            element is ParadoxScriptExpressionElement -> element.resolved()?.floatValue()
            else -> element.value.toFloatOrNull()
        } ?: return true
        if (floatValue in floatRange) return true
        val description = ChronicleEpBundle.message("incorrectExpression.range.desc", floatRange.expression, floatValue)
        context.holder.registerProblem(element, description)
        return false
    }
}

/**
 * 检查颜色字段的颜色类型是否匹配。
 */
class ParadoxColorFieldChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptColor) return true

        // for color field only (after expansion)
        val configExpression = config.expandConfigExpression().find { it.type == CwtDataTypes.ColorField }
        if (configExpression == null) return true

        val expectedColorType = configExpression.value ?: return true
        val colorType = element.colorType
        if (colorType == expectedColorType) return true
        val description = ChronicleEpBundle.message("incorrectExpression.colorType.desc", expectedColorType, colorType)
        context.holder.registerProblem(element, description)
        return false
    }
}

/**
 * 检查作用域字段表达式的作用域是否匹配指定的作用域。
 */
class ParadoxScopeBasedScopeFieldExpressionChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptStringExpressionElement) return true

        val configExpression = config.expandConfigExpression().find { it.type == CwtDataTypes.Scope }
        if (configExpression == null) return true

        val expectedScope = configExpression.value ?: return true
        val value = element.value
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, null, context.configGroup) ?: return true
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return true
        val parentScopeContext = ParadoxScopeManager.getScopeContext(memberElement) ?: ParadoxScopeContext.resolveAny()
        val scopeContext = ParadoxScopeManager.getScopeContext(element, scopeFieldExpression, parentScopeContext)
        if (ParadoxScopeManager.matchesScope(scopeContext, expectedScope, context.configGroup)) return true
        val description = ChronicleEpBundle.message("incorrectExpression.scope.desc", expectedScope, scopeContext.scope.id)
        context.holder.registerProblem(element, description)
        return false
    }
}

/**
 * 检查作用域字段表达式的作用域是否匹配指定的作用域分组。
 */
class ParadoxScopeGroupBasedScopeFieldExpressionChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptStringExpressionElement) return true

        val configExpression = config.expandConfigExpression().find { it.type == CwtDataTypes.ScopeGroup }
        if (configExpression == null) return true

        val expectedScopeGroup = configExpression.value ?: return true
        val value = element.value
        val configGroup = config.configGroup
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, null, configGroup) ?: return true
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return true
        val parentScopeContext = ParadoxScopeManager.getScopeContext(memberElement) ?: ParadoxScopeContext.resolveAny()
        val scopeContext = ParadoxScopeManager.getScopeContext(element, scopeFieldExpression, parentScopeContext)
        if (ParadoxScopeManager.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return true
        val description = ChronicleEpBundle.message("incorrectExpression.scopeGroup.desc", expectedScopeGroup, scopeContext.scope.id)
        context.holder.registerProblem(element, description)
        return false
    }
}

/**
 * 检查整数值字段的评估结果是否是一个整数。如果指定了区间，也检查是否在此区间内。
 */
class ParadoxIntValueFieldChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptExpressionElement) return true

        // for int value field only (after expansion)
        val configExpression = config.expandConfigExpression().find { it.type == CwtDataTypes.IntValueField }
        if (configExpression == null) return true

        val evaluated = ParadoxComplexExpressionEvaluator().evaluate(element) ?: return true
        val intValue = evaluated.intValue()
        val intRange = configExpression.intRange
        if (intValue == null) {
            val description = ChronicleEpBundle.message("incorrectExpression.intValueField.desc", evaluated.expression)
            context.holder.registerProblem(element, description)
            return false
        }
        if (intRange != null && intValue !in intRange) {
            val description = ChronicleEpBundle.message("incorrectExpression.intValueFieldRange.desc", evaluated.expression, intRange.expression, intValue)
            context.holder.registerProblem(element, description)
            return false
        }
        return true
    }
}

/**
 * 检查浮点数值字段的评估结果是否是一个浮点数。如果指定了区间，也检查是否在此区间内。
 */
class ParadoxFloatValueFieldChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptExpressionElement) return true

        // for value field only (after expansion)
        val configExpression = config.expandConfigExpression().find { it.type == CwtDataTypes.ValueField }
        if (configExpression == null) return true

        val evaluated = ParadoxComplexExpressionEvaluator().evaluate(element) ?: return true
        val floatValue = evaluated.floatValue()
        val floatRange = configExpression.floatRange
        if (floatValue == null && floatRange != null) { // NOTE 2.2.0 may not be a number after evaluation, if range is not specified
            val description = ChronicleEpBundle.message("incorrectExpression.floatValueField.desc", evaluated.expression)
            context.holder.registerProblem(element, description)
            return false
        }
        if (floatValue == null) return true
        if (floatRange != null && floatValue !in floatRange) {
            val description = ChronicleEpBundle.message("incorrectExpression.floatValueFieldRange.desc", evaluated.expression, floatRange.expression, floatValue)
            context.holder.registerProblem(element, description)
            return false
        }
        return true
    }
}

/**
 * 检查 Stellaris 中携带了循环等级的科技引用是否正确。
 */
@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyWithLevelChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptStringExpressionElement) return true

        val configExpression = config.expandConfigExpression().find { it.type == CwtDataTypes.TechnologyWithLevel }
        if (configExpression == null) return true

        val (technologyName, technologyLevel) = element.value.split('@', limit = 2).takeIf { it.size == 2 } ?: return true
        val project = config.configGroup.project
        val text = element.text
        val separatorIndex = text.indexOf('@')
        if (technologyName.isEmpty() || ParadoxDefinitionSearch.search(technologyName, "technology.repeatable", ParadoxDefinitionSearch.selector(project, element)).findFirst() == null) {
            val range = TextRange.create(0, text.length).unquote(text).let { TextRange.create(it.startOffset, separatorIndex) }
            val description = ChronicleEpBundle.message("incorrectExpression.repeatableTechnologyName.desc", range.substring(text))
            context.holder.registerProblem(element, description, ProblemHighlightType.ERROR, range)
        }
        if (technologyLevel.isEmpty() || !technologyLevel.all { c -> c.isExactDigit() } || technologyLevel.toInt() !in -1..100) {
            val range = TextRange.create(0, text.length).unquote(text).let { TextRange.create(separatorIndex + 1, it.endOffset) }
            val description = ChronicleEpBundle.message("incorrectExpression.repeatableTechnologyLevel.desc", range.substring(text))
            context.holder.registerProblem(element, description, ProblemHighlightType.GENERIC_ERROR, range)
        }
        return false
    }
}

