package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
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
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptExpressionElement && element !is ParadoxCsvExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type !in CwtDataTypeSets.IntField) return // for int only
        val intRange = configExpression.intRange ?: return
        val intValue = when {
            element is ParadoxScriptExpressionElement -> element.resolved()?.intValue()
            else -> element.value.toIntOrNull()
        } ?: return
        if (intValue in intRange) return
        val description = ChronicleEpBundle.message("incorrectExpression.range.desc", intRange.expression, intValue)
        holder.registerProblem(element, description)
    }
}

/**
 * 检查浮点数数值是否在指定的区间内。
 */
class ParadoxRangedFloatChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptExpressionElement && element !is ParadoxCsvExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type !in CwtDataTypeSets.FloatField) return // for int and float
        val floatRange = configExpression.floatRange ?: return
        val floatValue = when {
            element is ParadoxScriptExpressionElement -> element.resolved()?.floatValue()
            else -> element.value.toFloatOrNull()
        } ?: return
        if (floatValue in floatRange) return
        val description = ChronicleEpBundle.message("incorrectExpression.range.desc", floatRange.expression, floatValue)
        holder.registerProblem(element, description)
    }
}

/**
 * 检查颜色字段的颜色类型是否匹配。
 */
class ParadoxColorFieldChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptColor) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.ColorField) return
        val expectedColorType = configExpression.value ?: return
        val colorType = element.colorType
        if (colorType == expectedColorType) return
        val description = ChronicleEpBundle.message("incorrectExpression.colorType.desc", expectedColorType, colorType)
        holder.registerProblem(element, description)
    }
}

/**
 * 检查作用域字段表达式的作用域是否匹配指定的作用域。
 */
class ParadoxScopeBasedScopeFieldExpressionChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptStringExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.Scope) return
        val expectedScope = configExpression.value ?: return
        val value = element.value
        val configGroup = config.configGroup
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, null, configGroup) ?: return
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return
        val parentScopeContext = ParadoxScopeManager.getScopeContext(memberElement) ?: ParadoxScopeContext.resolveAny()
        val scopeContext = ParadoxScopeManager.getScopeContext(element, scopeFieldExpression, parentScopeContext)
        if (ParadoxScopeManager.matchesScope(scopeContext, expectedScope, configGroup)) return
        val description = ChronicleEpBundle.message("incorrectExpression.scope.desc", expectedScope, scopeContext.scope.id)
        holder.registerProblem(element, description)
    }
}

/**
 * 检查作用域字段表达式的作用域是否匹配指定的作用域分组。
 */
class ParadoxScopeGroupBasedScopeFieldExpressionChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptStringExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.ScopeGroup) return
        val expectedScopeGroup = configExpression.value ?: return
        val value = element.value
        val configGroup = config.configGroup
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, null, configGroup) ?: return
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return
        val parentScopeContext = ParadoxScopeManager.getScopeContext(memberElement) ?: ParadoxScopeContext.resolveAny()
        val scopeContext = ParadoxScopeManager.getScopeContext(element, scopeFieldExpression, parentScopeContext)
        if (ParadoxScopeManager.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return
        val description = ChronicleEpBundle.message("incorrectExpression.scopeGroup.desc", expectedScopeGroup, scopeContext.scope.id)
        holder.registerProblem(element, description)
    }
}

/**
 * 检查整数值字段的评估结果是否是一个整数。如果指定了区间，也检查是否在此区间内。
 */
class ParadoxIntValueFieldChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.IntValueField) return

        val evaluated = ParadoxComplexExpressionEvaluator().evaluate(element) ?: return
        val intValue = evaluated.intValue()
        val intRange = configExpression.intRange
        if (intValue == null) {
            val description = ChronicleEpBundle.message("incorrectExpression.intValueField.desc", evaluated.expression)
            holder.registerProblem(element, description)
        }
        if (intValue == null) return
        if (intRange != null && intValue !in intRange) {
            val description = ChronicleEpBundle.message("incorrectExpression.intValueFieldRange.desc", evaluated.expression, intRange.expression, intValue)
            holder.registerProblem(element, description)
        }
    }
}

/**
 * 检查浮点数值字段的评估结果是否是一个浮点数。如果指定了区间，也检查是否在此区间内。
 */
class ParadoxFloatValueFieldChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.ValueField) return

        val evaluated = ParadoxComplexExpressionEvaluator().evaluate(element) ?: return
        val floatValue = evaluated.floatValue()
        val floatRange = configExpression.floatRange
        if (floatValue == null && floatRange != null) { // NOTE 2.2.0 may not be a number after evaluation, if range is not specified
            val description = ChronicleEpBundle.message("incorrectExpression.floatValueField.desc", evaluated.expression)
            holder.registerProblem(element, description)
            return
        }
        if (floatValue == null) return
        if (floatRange != null && floatValue !in floatRange) {
            val description = ChronicleEpBundle.message("incorrectExpression.floatValueFieldRange.desc", evaluated.expression, floatRange.expression, floatValue)
            holder.registerProblem(element, description)
        }
    }
}

/**
 * 检查 Stellaris 中携带了循环等级的科技引用是否正确。
 */
@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyWithLevelChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptStringExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.TechnologyWithLevel) return
        val (technologyName, technologyLevel) = element.value.split('@', limit = 2).takeIf { it.size == 2 } ?: return
        val project = config.configGroup.project
        val text = element.text
        val separatorIndex = text.indexOf('@')
        if (technologyName.isEmpty() || ParadoxDefinitionSearch.search(technologyName, "technology.repeatable", ParadoxDefinitionSearch.selector(project, element)).findFirst() == null) {
            val range = TextRange.create(0, text.length).unquote(text).let { TextRange.create(it.startOffset, separatorIndex) }
            val description = ChronicleEpBundle.message("incorrectExpression.repeatableTechnologyName.desc", range.substring(text))
            holder.registerProblem(element, description, ProblemHighlightType.ERROR, range)
        }
        if (technologyLevel.isEmpty() || !technologyLevel.all { c -> c.isExactDigit() } || technologyLevel.toInt() !in -1..100) {
            val range = TextRange.create(0, text.length).unquote(text).let { TextRange.create(separatorIndex + 1, it.endOffset) }
            val description = ChronicleEpBundle.message("incorrectExpression.repeatableTechnologyLevel.desc", range.substring(text))
            holder.registerProblem(element, description, ProblemHighlightType.GENERIC_ERROR, range)
        }
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

    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptExpressionElement) return

        if (element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if (config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return

        val propertyConfig = config.propertyConfig ?: return
        if (propertyConfig.key !in Constants.triggerKeys) return
        val aliasConfig = propertyConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return
        if (aliasConfig.subName !in Constants.contextNames) return

        val triggerName = element.stringValue() ?: return
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return

        if (resultTriggerConfigs.none { it.config.valueType != CwtExpressionType.Block }) {
            holder.registerProblem(element, ChronicleEpBundle.message("incorrectExpression.simpleTrigger.desc", element.expression))
        }
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

    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        // `__` - caret position
        // `<container> = { <trigger_field> = __<trigger> parameters = { ... } }`
        // -> `<container> = { <trigger_field> = <trigger> __parameters = { ... } }`

        if (element !is ParadoxScriptExpressionElement) return

        if (element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if (config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return

        val propertyConfig = config.propertyConfig ?: return
        if (propertyConfig.key != Constants.triggerKey) return
        val aliasConfig = propertyConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return
        if (aliasConfig.subName !in Constants.contextNames) return

        val triggerName = element.stringValue() ?: return
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return

        val hasParameters = selectScope { element.queryParentBy("*/*").asProperty().queryBy("parameters").asProperty().any() }
        if (hasParameters) {
            if (resultTriggerConfigs.none { it.config.valueType == CwtExpressionType.Block }) {
                holder.registerProblem(element, ChronicleEpBundle.message("incorrectExpression.complexTrigger.desc", element.expression))
            }
        } else {
            // can also be complex trigger here, for some parameters can be ignored (like `count = xxx`)
            // if (resultTriggerConfigs.none { !it.config.isBlock }) {
            //    holder.registerProblem(element, ChronicleEpBundle.message("incorrectExpression.checker.expect.simpleTrigger", element.expression.orEmpty()))
            // }
        }
    }
}
