package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.script.psi.*

class ParadoxSwitchCaseOverriddenConfigProvider: ParadoxOverriddenConfigProvider {
    companion object {
        private const val CASE_KEY = "scalar"
        private val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
        private val SWITCH_KEYS = arrayOf("alias[effect:switch]", "alias[trigger:switch]", "alias[effect:inverted_switch]", "alias[effect:inverted_switch]")
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtDataConfig<*>> getOverriddenConfigs(element: ParadoxScriptMemberElement, rawConfig: T): List<T>? {
        //重置switch = {...}和inverted_switch = {...}中对应的CWT规则为scalar的属性的键对应的CWT规则
        //兼容使用内联或者使用封装变量的情况
        if(element !is ParadoxScriptProperty) return null
        if(rawConfig !is CwtPropertyConfig) return null
        if(rawConfig.key != CASE_KEY) return null
        val aliasConfig = rawConfig.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.config.key !in SWITCH_KEYS) return null
        ProgressManager.checkCanceled()
        val triggerConfig = aliasConfig.config.configs?.find { it is CwtPropertyConfig && it.key in TRIGGER_KEYS && it.value == "alias_keys_field[trigger]" } ?: return null
        val triggerConfigKey = triggerConfig.castOrNull<CwtPropertyConfig>()?.key ?: return null
        val triggerProperty = element.parent?.castOrNull<ParadoxScriptBlock>()?.findProperty(triggerConfigKey, inline = true) ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        val configGroup = rawConfig.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return null
        val resultConfigs = SmartList<CwtPropertyConfig>()
        for(resultTriggerConfig in resultTriggerConfigs) {
            if(resultTriggerConfig.config.isBlock) continue //not simple trigger, skip
            val inlined = ParadoxConfigInlineHandler.inlineWithConfig(rawConfig, resultTriggerConfig.config, ParadoxConfigInlineHandler.Mode.VALUE_TO_KEY) ?: continue
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }
}