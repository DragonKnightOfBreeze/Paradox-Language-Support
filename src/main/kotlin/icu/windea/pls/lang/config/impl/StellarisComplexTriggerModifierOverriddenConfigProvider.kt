package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisComplexTriggerModifierOverriddenConfigProvider : ParadoxOverriddenConfigProvider {
    companion object {
        private const val TRIGGER_KEY = "trigger"
        private const val PARAMETERS_KEY = "parameters"
        private const val COMPLEX_TRIGGER_MODIFIER_NAME = "complex_trigger_modifier"
        private val COMPLEX_TRIGGER_MODIFIER_KEYS = arrayOf("alias[modifier_rule:complex_trigger_modifier]", "alias[modifier_rule_with_loc:complex_trigger_modifier]")
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T>? {
        //重载complex_trigger_modifier = {...}中属性parameters的值对应的CWT规则
        //兼容使用内联或者使用封装变量的情况
        if(config !is CwtPropertyConfig) return null
        if(config.key != PARAMETERS_KEY) return null
        val aliasConfig = config.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.config.key !in COMPLEX_TRIGGER_MODIFIER_KEYS) return null
        ProgressManager.checkCanceled()
        val complexTriggerModifierProperty = contextElement.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() == COMPLEX_TRIGGER_MODIFIER_NAME }
            .find { ParadoxConfigHandler.getConfigs(it).any { c -> c.inlineableConfig == aliasConfig } }
            ?: return null
        val triggerProperty = complexTriggerModifierProperty.findProperty(TRIGGER_KEY, inline = true) ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        if(CwtValueExpression.resolve(triggerName).type != CwtDataType.Constant) return null //must be predefined trigger
        val configGroup = config.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return null
        val resultConfigs = mutableListOf<CwtPropertyConfig>()
        for(resultTriggerConfig in resultTriggerConfigs) {
            if(!resultTriggerConfig.config.isBlock) continue //not complex trigger, skip
            val inlined = ParadoxConfigInlineHandler.inlineWithConfig(config, resultTriggerConfig.config, ParadoxConfigInlineHandler.Mode.VALUE_TO_VALUE) ?: continue
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }
    
    override fun skipMissingExpressionCheck(configs: List<CwtMemberConfig<*>>, configExpression: CwtDataExpression): Boolean {
        // skip properties whose value config type is one of the following types:
        // int / float / value_field / int_value_field / variable_field / int_variable_field
        if(configExpression !is CwtKeyExpression) return false
        configs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if(c2 is CwtPropertyConfig && c2.expression == configExpression) {
                    val valueExpression = c2.valueExpression
                    when {
                        valueExpression.type == CwtDataType.Int -> return true
                        valueExpression.type == CwtDataType.Float -> return true
                        valueExpression.type.isValueFieldType() -> return true
                        valueExpression.type.isVariableFieldType() -> return true
                    }
                }
            }
        }
        return false
    }
}
