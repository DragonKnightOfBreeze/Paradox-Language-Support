package icu.windea.pls.ep.config

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.memberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.manipulators.CwtConfigInlineMode
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.ep.config.CwtTriggerWithParametersAwareOverriddenConfigProvider.Constants.CONTEXT_NAME_1
import icu.windea.pls.lang.psi.select.propertyOld
import icu.windea.pls.lang.psi.select.select
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.CwtType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.stringValue

class CwtSwitchOverriddenConfigProvider : CwtOverriddenConfigProvider {
    // 重载 `switch = {...}` 中匹配 `scalar` 的属性的键对应的规则
    // 重载 `inverted_switch = {...}` 中匹配 `scalar` 的属性的键对应的规则
    // 兼容使用内联或者封装变量的情况

    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T> {
        if (config !is CwtPropertyConfig) return emptyList()
        if (config.key != Constants.CASE_KEY) return emptyList()
        val aliasConfig = config.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return emptyList()
        if (aliasConfig.subName !in Constants.CONTEXT_NAMES) return emptyList()
        ProgressManager.checkCanceled()
        val triggerConfig = aliasConfig.config.configs?.find { it is CwtPropertyConfig && it.key in Constants.TRIGGER_KEYS && it.value == Constants.TRIGGER_VALUE } ?: return emptyList()
        val triggerConfigKey = triggerConfig.castOrNull<CwtPropertyConfig>()?.key ?: return emptyList()
        val triggerProperty = contextElement.parentOfType<ParadoxScriptBlock>(withSelf = false)
            ?.select { propertyOld(triggerConfigKey, inline = true) }
            ?: return emptyList()
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return emptyList()
        if (CwtDataExpression.resolve(triggerName, false).type != CwtDataTypes.Constant) return emptyList() // must be a predefined trigger
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return emptyList()
        val resultConfigs = mutableListOf<CwtPropertyConfig>()
        for (resultTriggerConfig in resultTriggerConfigs) {
            if (resultTriggerConfig.config.valueType == CwtType.Block) continue // not simple trigger, skip
            val inlined = CwtConfigManipulator.inlineWithConfig(config, resultTriggerConfig.config, CwtConfigInlineMode.VALUE_TO_KEY) ?: continue
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }

    object Constants {
        const val CASE_KEY = "scalar"
        val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
        const val TRIGGER_VALUE = "alias_keys_field[trigger]"
        val CONTEXT_NAMES = arrayOf("switch", "inverted_switch")
    }
}

class CwtTriggerWithParametersAwareOverriddenConfigProvider : CwtOverriddenConfigProvider {
    // 重载 `complex_trigger_modifier = {...}` 中名为 `parameters` 的属性的值对应的规则
    // 重载 `export_trigger_value_to_variable = {...}` 中名为 `parameters` 的属性的值对应的规则
    // 兼容使用内联或者封装变量的情况

    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T> {
        if (config !is CwtPropertyConfig) return emptyList()
        if (config.key != Constants.PARAMETERS_KEY) return emptyList()
        val aliasConfig = config.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return emptyList()
        if (aliasConfig.subName !in Constants.CONTEXT_NAMES) return emptyList()
        ProgressManager.checkCanceled()
        val contextProperty = contextElement.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() in Constants.CONTEXT_NAMES }
            .find { ParadoxExpressionManager.getConfigs(it).any { c -> c is CwtPropertyConfig && c.aliasConfig == aliasConfig } }
            ?: return emptyList()
        val triggerProperty = contextProperty.select { propertyOld(Constants.TRIGGER_KEY, inline = true) } ?: return emptyList()
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return emptyList()
        if (CwtDataExpression.resolve(triggerName, false).type != CwtDataTypes.Constant) return emptyList() // must be a predefined trigger
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return emptyList()
        val resultConfigs = mutableListOf<CwtPropertyConfig>()
        for (resultTriggerConfig in resultTriggerConfigs) {
            if (resultTriggerConfig.config.valueType != CwtType.Block) continue // not complex trigger, skip
            val inlined = CwtConfigManipulator.inlineWithConfig(config, resultTriggerConfig.config, CwtConfigInlineMode.VALUE_TO_VALUE) ?: continue
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }

    override fun skipMissingExpressionCheck(configs: List<CwtMemberConfig<*>>, configExpression: CwtDataExpression): Boolean {
        // for `export_trigger_value_to_variable`, skip all properties
        // for `complex_trigger_modifier`, skip properties whose value config type is int, float, value_field or variable_field

        if (!configExpression.isKey) return false
        configs.forEach { c1 ->
            val pc = c1.memberConfig.parentConfig?.memberConfig?.castOrNull<CwtPropertyConfig>()
            if (pc?.key != CONTEXT_NAME_1) return true
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

    object Constants {
        const val TRIGGER_KEY = "trigger"
        const val PARAMETERS_KEY = "parameters"
        const val CONTEXT_NAME_1 = "complex_trigger_modifier"
        const val CONTEXT_NAME_2 = "export_trigger_value_to_variable"
        val CONTEXT_NAMES = arrayOf(CONTEXT_NAME_1, CONTEXT_NAME_2)
    }
}
