package icu.windea.pls.ep.config

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class CwtSwitchOverriddenConfigProvider : CwtOverriddenConfigProvider {
    object Constants {
        const val CASE_KEY = "scalar"
        val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
        val CONTEXT_NAMES = arrayOf("switch", "inverted_switch")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T>? {
        //重载switch = {...}中对应的CWT规则为scalar的属性的键对应的CWT规则
        //重载inverted_switch = {...}中对应的CWT规则为scalar的属性的键对应的CWT规则
        //兼容使用内联或者使用封装变量的情况

        if (config !is CwtPropertyConfig) return null
        if (config.key != Constants.CASE_KEY) return null
        val aliasConfig = config.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return null
        if (aliasConfig.subName !in Constants.CONTEXT_NAMES) return null
        ProgressManager.checkCanceled()
        val triggerConfig = aliasConfig.config.configs?.find { it is CwtPropertyConfig && it.key in Constants.TRIGGER_KEYS && it.value == "alias_keys_field[trigger]" } ?: return null
        val triggerConfigKey = triggerConfig.castOrNull<CwtPropertyConfig>()?.key ?: return null
        val triggerProperty = contextElement.parentOfType<ParadoxScriptBlock>(withSelf = false)
            ?.findProperty(triggerConfigKey, inline = true)
            ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        if (CwtDataExpression.resolve(triggerName, false).type != CwtDataTypes.Constant) return null //must be a predefined trigger
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return null
        val resultConfigs = mutableListOf<CwtPropertyConfig>()
        for (resultTriggerConfig in resultTriggerConfigs) {
            if (resultTriggerConfig.config.isBlock) continue //not simple trigger, skip
            val inlined = CwtConfigManipulator.inlineWithConfig(config, resultTriggerConfig.config, CwtConfigManipulator.InlineMode.VALUE_TO_KEY) ?: continue
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }
}

class CwtTriggerWithParametersAwareOverriddenConfigProvider : CwtOverriddenConfigProvider {
    object Constants {
        const val TRIGGER_KEY = "trigger"
        const val PARAMETERS_KEY = "parameters"
        val CONTEXT_NAMES = arrayOf("complex_trigger_modifier", "export_trigger_value_to_variable")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T>? {
        //重载complex_trigger_modifier = {...}中属性parameters的值对应的CWT规则
        //重载export_trigger_value_to_variable = {...}中属性parameters的值对应的CWT规则
        //兼容使用内联或者使用封装变量的情况

        if (config !is CwtPropertyConfig) return null
        if (config.key != Constants.PARAMETERS_KEY) return null
        val aliasConfig = config.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return null
        if (aliasConfig.subName !in Constants.CONTEXT_NAMES) return null
        ProgressManager.checkCanceled()
        val contextProperty = contextElement.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() in Constants.CONTEXT_NAMES }
            .find { ParadoxExpressionManager.getConfigs(it).any { c -> c is CwtPropertyConfig && c.aliasConfig == aliasConfig } }
            ?: return null
        val triggerProperty = contextProperty.findProperty(Constants.TRIGGER_KEY, inline = true) ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        if (CwtDataExpression.resolve(triggerName, false).type != CwtDataTypes.Constant) return null //must be a predefined trigger
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return null
        val resultConfigs = mutableListOf<CwtPropertyConfig>()
        for (resultTriggerConfig in resultTriggerConfigs) {
            if (!resultTriggerConfig.config.isBlock) continue //not complex trigger, skip
            val inlined = CwtConfigManipulator.inlineWithConfig(config, resultTriggerConfig.config, CwtConfigManipulator.InlineMode.VALUE_TO_VALUE) ?: continue
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }

    override fun skipMissingExpressionCheck(configs: List<CwtMemberConfig<*>>, configExpression: CwtDataExpression): Boolean {
        //for export_trigger_value_to_variable, skip all properties

        //for complex_trigger_modifier, skip properties whose value config type is one of the following types:
        //int / float / value_field / int_value_field / variable_field / int_variable_field

        if (!configExpression.isKey) return false
        configs.forEach { c1 ->
            val pc = c1.memberConfig.parentConfig?.memberConfig?.castOrNull<CwtPropertyConfig>()
            if (pc?.key != "complex_trigger_modifier") return true
            c1.configs?.forEach { c2 ->
                if (c2 is CwtPropertyConfig && c2.configExpression == configExpression) {
                    val valueExpression = c2.valueExpression
                    when {
                        valueExpression.type == CwtDataTypes.Int -> return true
                        valueExpression.type == CwtDataTypes.Float -> return true
                        valueExpression.type in CwtDataTypeGroups.ValueField -> return true
                        valueExpression.type in CwtDataTypeGroups.VariableField -> return true
                    }
                }
            }
        }
        return false
    }
}
