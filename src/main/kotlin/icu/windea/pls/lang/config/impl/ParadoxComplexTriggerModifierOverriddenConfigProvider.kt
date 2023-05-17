package icu.windea.pls.lang.config.impl

import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.script.psi.*

class ParadoxComplexTriggerModifierOverriddenConfigProvider : ParadoxOverriddenConfigProvider {
    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtDataConfig<*>> getOverriddenConfigs(element: ParadoxScriptMemberElement, rawConfig: T): List<T>? {
        //兼容使用内联或者使用封装变量的情况
        if(element !is ParadoxScriptProperty) return null
        if(rawConfig !is CwtPropertyConfig) return null
        if(rawConfig.key != "parameters") return null
        val aliasConfig = rawConfig.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.config.key != "alias[modifier_rule:complex_trigger_modifier]") return null
        val triggerProperty = element.parent?.castOrNull<ParadoxScriptBlock>()?.findProperty("trigger", inline = true) ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        val configGroup = rawConfig.info.configGroup
        val triggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return null
        val resultConfigs = SmartList<CwtPropertyConfig>()
        for(triggerConfig in triggerConfigs) {
            val inlined = rawConfig.inlineFromAliasConfig(triggerConfig, key = rawConfig.key)
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }
}
