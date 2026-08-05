package icu.windea.pls.ep.resolve.config

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.containingDirectConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.manipulation.CwtConfigInlineMode
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.findIsInstanceFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.equalsAnyFast
import icu.windea.pls.core.equalsFast
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty

@Optimized
class CwtSwitchOverriddenConfigProvider : CwtOverriddenConfigProvider {
    // 重载 `switch = {...}` 中匹配 `scalar` 的属性的键对应的规则
    // 重载 `inverted_switch = {...}` 中匹配 `scalar` 的属性的键对应的规则
    // 兼容使用内联或者封装变量的情况

    object Constants {
        const val caseKey = "scalar"
        val triggerKeys = arrayOf("trigger", "on_trigger")
        const val triggerValue = "alias_keys_field[trigger]"
        val contextNames = arrayOf("switch", "inverted_switch")
    }

    override fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T> {
        if (config !is CwtPropertyConfig) return emptyList()
        if (Constants.caseKey.equalsFast(config.key)) return emptyList() // 3.0.1 radical optimization

        val aliasConfig = config.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return emptyList()
        if (!Constants.contextNames.equalsAnyFast(aliasConfig.subName)) return emptyList() // 3.0.1 radical optimization
        ProgressManager.checkCanceled()
        val triggerConfig = aliasConfig.config.configs?.findIsInstanceFast<CwtPropertyConfig> { it.key in Constants.triggerKeys && it.value == Constants.triggerValue } ?: return emptyList()
        val triggerConfigKey = triggerConfig.key
        val contextBlock = contextElement.parentOfType<ParadoxScriptBlock>(withSelf = false) ?: return emptyList()
        val triggerProperty = selectScope { contextBlock.properties(inline = true).ofKey(triggerConfigKey).one() } ?: return emptyList()
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return emptyList()
        if (CwtDataExpression.resolve(triggerName, CwtDataExpressionRole.Value).type != CwtDataTypes.Constant) return emptyList() // must be a predefined trigger
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return emptyList()
        val resultConfigs = mutableListOf<CwtPropertyConfig>()
        resultTriggerConfigs.forEachFast f@{ resultTriggerConfig ->
            if (resultTriggerConfig.config.valueType == CwtExpressionType.Block) return@f // not simple trigger, skip
            val inlined = CwtConfigManipulationService.inlineWithConfig(config, resultTriggerConfig.config, CwtConfigInlineMode.VALUE_TO_KEY) ?: return@f
            resultConfigs.add(inlined)
        }
        return resultConfigs.cast<List<T>>()
    }
}

@Optimized
class CwtTriggerWithParametersAwareOverriddenConfigProvider : CwtOverriddenConfigProvider {
    // 重载 `complex_trigger_modifier = {...}` 中名为 `parameters` 的属性的值对应的规则
    // 重载 `export_trigger_value_to_variable = {...}` 中名为 `parameters` 的属性的值对应的规则
    // 兼容使用内联或者封装变量的情况

    object Constants {
        const val triggerKey = "trigger"
        const val parametersKey = "parameters"
        const val contextName1 = "complex_trigger_modifier"
        const val contextName2 = "export_trigger_value_to_variable"
        val contextNames = arrayOf(contextName1, contextName2)
    }

    override fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T> {
        if (config !is CwtPropertyConfig) return emptyList()
        if (Constants.parametersKey.equalsFast(config.key)) return emptyList() // 3.0.1 radical optimization

        val aliasConfig = config.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return emptyList()
        if (!Constants.contextNames.equalsAnyFast(aliasConfig.subName)) return emptyList() // 3.0.1 radical optimization
        ProgressManager.checkCanceled()
        val contextProperty = contextElement.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() in Constants.contextNames }
            .find { ParadoxConfigManager.getConfigs(it).anyFast { c -> c is CwtPropertyConfig && c.aliasConfig == aliasConfig } }
            ?: return emptyList()
        val triggerProperty = selectScope { contextProperty.properties(inline = true).ofKey(Constants.triggerKey).one() } ?: return emptyList()
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return emptyList()
        if (CwtDataExpression.resolve(triggerName, CwtDataExpressionRole.Value).type != CwtDataTypes.Constant) return emptyList() // must be a predefined trigger
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return emptyList()
        val resultConfigs = mutableListOf<CwtPropertyConfig>()
        resultTriggerConfigs.forEachFast f@{ resultTriggerConfig ->
            if (resultTriggerConfig.config.valueType != CwtExpressionType.Block) return@f // not complex trigger, skip
            val inlined = CwtConfigManipulationService.inlineWithConfig(config, resultTriggerConfig.config, CwtConfigInlineMode.VALUE_TO_VALUE) ?: return@f
            resultConfigs.add(inlined)
        }
        return resultConfigs.cast<List<T>>()
    }

    override fun skipMissingExpressionCheck(configs: List<CwtMemberConfig<*>>, configExpression: CwtDataExpression): Boolean {
        // for `export_trigger_value_to_variable`, skip all properties
        // for `complex_trigger_modifier`, skip properties whose value config type is `int`, `float`, `value_field` or `variable_field`

        if (!configExpression.role.isKey()) return false
        configs.forEachFast { c1 ->
            val pc = c1.containingDirectConfig.parentConfig?.containingDirectConfig?.castOrNull<CwtPropertyConfig>()
            if (pc?.key != Constants.contextName1) return true
            c1.configs?.forEachFast { c2 ->
                if (c2 is CwtPropertyConfig && c2.configExpression == configExpression) {
                    val valueExpression = c2.valueExpression
                    when {
                        valueExpression.type == CwtDataTypes.Int -> return true
                        valueExpression.type == CwtDataTypes.Float -> return true
                        valueExpression.type in CwtDataTypeSets.ValueField -> return true
                        valueExpression.type in CwtDataTypeSets.VariableField -> return true
                    }
                }
            }
        }
        return false
    }
}
