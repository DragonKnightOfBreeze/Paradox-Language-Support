package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.parentOfType
import icu.windea.pls.base.annotations.WithGameType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.configExpression.floatRange
import icu.windea.pls.config.configExpression.intRange
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.floatValue
import icu.windea.pls.lang.psi.intValue
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.evaluators.ParadoxComplexExpressionEvaluator
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 检查整数数值是否在指定的区间内。
 */
class ParadoxRangedIntChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptExpressionElement && element !is ParadoxCsvExpressionElement) return true

        val configExpression = config.configExpression
        if (configExpression.type !in CwtDataTypeSets.IntField) return true // for int only
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
class ParadoxRangedFloatChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptExpressionElement && element !is ParadoxCsvExpressionElement) return true

        val configExpression = config.configExpression
        if (configExpression.type !in CwtDataTypeSets.FloatField) return true // for int and float
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

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.ColorField) return true
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

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.Scope) return true
        val expectedScope = configExpression.value ?: return true
        val value = element.value
        val configGroup = config.configGroup
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, null, configGroup) ?: return true
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return true
        val parentScopeContext = ParadoxScopeManager.getScopeContext(memberElement) ?: ParadoxScopeContext.resolveAny()
        val scopeContext = ParadoxScopeManager.getScopeContext(element, scopeFieldExpression, parentScopeContext)
        if (ParadoxScopeManager.matchesScope(scopeContext, expectedScope, configGroup)) return true
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

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.ScopeGroup) return true
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

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.IntValueField) return true

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

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.ValueField) return true

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

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.TechnologyWithLevel) return true
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

/**
 * 检查特定语句中指定的触发器（trigger）是否是简单触发器（simple trigger）。
 *
 * 适用于：
 * - `switch = {...}`
 * - `inverted_switch = {...}`
 */
class ParadoxTriggerInSwitchStatementsChecker : ParadoxIncorrectExpressionChecker {
    object Constants {
        val triggerKeys = setOf("trigger", "on_trigger")
        val contextNames = setOf("switch", "inverted_switch")
    }

    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (element !is ParadoxScriptExpressionElement) return true

        if (element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return true
        if (config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return true

        val propertyConfig = config.propertyConfig ?: return true
        if (propertyConfig.key !in Constants.triggerKeys) return true
        val aliasConfig = propertyConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return true
        if (aliasConfig.subName !in Constants.contextNames) return true

        val triggerName = element.stringValue() ?: return true
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return true

        if (resultTriggerConfigs.none { it.config.valueType != CwtExpressionType.Block }) {
            context.holder.registerProblem(element, ChronicleEpBundle.message("incorrectExpression.simpleTrigger.desc", element.expression))
        }
        return true
    }
}

/**
 * 检查特定语句中指定的触发器（trigger）是否是简单触发器（simple trigger）（如果不带参数），或者复杂触发器（complex trigger）（如果带参数）。
 *
 * 适用于：
 * - `complex_trigger_modifier = {...}`
 * - `export_trigger_value_to_variable = {...}`
 */
class ParadoxTriggerInWithParametersStatementsChecker : ParadoxIncorrectExpressionChecker {
    object Constants {
        const val triggerKey = "trigger"
        val contextNames = setOf("complex_trigger_modifier", "export_trigger_value_to_variable")
    }

    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        // `__` - caret position
        // `<container> = { <trigger_field> = __<trigger> parameters = { ... } }`
        // -> `<container> = { <trigger_field> = <trigger> __parameters = { ... } }`

        if (element !is ParadoxScriptExpressionElement) return true

        if (element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return true
        if (config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return true

        val propertyConfig = config.propertyConfig ?: return true
        if (propertyConfig.key != Constants.triggerKey) return true
        val aliasConfig = propertyConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return true
        if (aliasConfig.subName !in Constants.contextNames) return true

        val triggerName = element.stringValue() ?: return true
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return true

        val hasParameters = selectScope { element.queryParentBy("*/*").asProperty().queryBy("parameters").asProperty().any() }
        if (hasParameters) {
            if (resultTriggerConfigs.none { it.config.valueType == CwtExpressionType.Block }) {
                context.holder.registerProblem(element, ChronicleEpBundle.message("incorrectExpression.complexTrigger.desc", element.expression))
            }
        } else {
            // can also be complex trigger here, for some parameters can be ignored (like `count = xxx`)
            // if (resultTriggerConfigs.none { !it.config.isBlock }) {
            //    context.holder.registerProblem(element, ChronicleEpBundle.message("incorrectExpression.checker.expect.simpleTrigger", element.expression.orEmpty()))
            // }
        }
        return true
    }
}
