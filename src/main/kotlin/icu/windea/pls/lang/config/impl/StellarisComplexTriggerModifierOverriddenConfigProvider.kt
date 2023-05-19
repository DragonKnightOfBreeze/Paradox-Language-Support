package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisComplexTriggerModifierOverriddenConfigProvider : ParadoxOverriddenConfigProvider {
    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtDataConfig<*>> getOverriddenConfigs(element: ParadoxScriptMemberElement, rawConfig: T): List<T>? {
        //重载complex_trigger_modifier = {...}中属性parameters的值对应的CWT规则
        //兼容使用内联或者使用封装变量的情况
        if(element !is ParadoxScriptProperty) return null
        if(rawConfig !is CwtPropertyConfig) return null
        if(rawConfig.key != "parameters") return null
        val complexTriggerModifierConfig = rawConfig.parent?.castOrNull<CwtPropertyConfig>()
        val aliasConfig = complexTriggerModifierConfig?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.config.key != "alias[modifier_rule:complex_trigger_modifier]") return null
        ProgressManager.checkCanceled()
        val complexTriggerModifierProperty = element.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() == "complex_trigger_modifier" }
            .find { ParadoxConfigHandler.getConfigs(it).any { c -> c.inlineableConfig == aliasConfig } }
            ?: return null
        val triggerProperty = complexTriggerModifierProperty.findProperty("trigger", inline = true) ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        val configGroup = rawConfig.info.configGroup
        val triggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return null
        val resultConfigs = SmartList<CwtPropertyConfig>()
        for(triggerConfig in triggerConfigs) {
            ProgressManager.checkCanceled()
            val inlined = rawConfig.inlineFromAliasConfig(triggerConfig, valueOnly = true)
            inlined.inlineableConfig = null
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }
    
    override fun skipMissingExpressionCheck(configs: List<CwtDataConfig<*>>, configExpression: CwtDataExpression): Boolean {
        val isDirectChild = configs.any { it.castOrNull<CwtValueConfig>()?.propertyConfig?.overriddenProvider != null }
        if(isDirectChild) {
            val s = configExpression.expressionString
            return s == "value" || s == "count" || s == "percentage"
        }
        return false
    }
}
