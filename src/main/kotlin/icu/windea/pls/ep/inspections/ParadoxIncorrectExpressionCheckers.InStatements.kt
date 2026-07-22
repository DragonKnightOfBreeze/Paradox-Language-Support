package icu.windea.pls.ep.inspections

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString

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
