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
    companion object {
        private const val TRIGGER_KEY = "trigger"
        private const val TRIGGER_SCOPE_KEY = "trigger_scope"
        private const val COMPLEX_TRIGGER_MODIFIER_NAME = "complex_trigger_modifier"
        private const val COMPLEX_TRIGGER_MODIFIER_KEY = "alias[modifier_rule:complex_trigger_modifier]"
        
        private val SKIP_MISSING_CHECK_KEYS = arrayOf("value", "count", "percentage")
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtDataConfig<*>> getOverriddenConfigs(element: ParadoxScriptMemberElement, rawConfig: T): List<T>? {
        //重载complex_trigger_modifier = {...}中属性parameters的值对应的CWT规则
        //兼容使用内联或者使用封装变量的情况
        if(element !is ParadoxScriptProperty) return null
        if(rawConfig !is CwtPropertyConfig) return null
        if(rawConfig.key != TRIGGER_KEY) return null
        val aliasConfig = rawConfig.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.config.key != COMPLEX_TRIGGER_MODIFIER_KEY) return null
        ProgressManager.checkCanceled()
        val complexTriggerModifierProperty = element.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() == COMPLEX_TRIGGER_MODIFIER_NAME }
            .find { ParadoxConfigHandler.getConfigs(it).any { c -> c.inlineableConfig == aliasConfig } }
            ?: return null
        val triggerProperty = complexTriggerModifierProperty.findProperty(TRIGGER_KEY, inline = true) ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        val configGroup = rawConfig.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return null
        val resultConfigs = SmartList<CwtPropertyConfig>()
        for(resultTriggerConfig in resultTriggerConfigs) {
            if(!resultTriggerConfig.config.isBlock) continue //not complex trigger, skip
            val inlined = ParadoxConfigInlineHandler.inlineWithConfig(rawConfig, resultTriggerConfig.config, ParadoxConfigInlineHandler.Mode.VALUE_TO_VALUE) ?: continue
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }
    
    override fun skipMissingExpressionCheck(configs: List<CwtDataConfig<*>>, configExpression: CwtDataExpression): Boolean {
        val isDirectChild = configs.any { it.castOrNull<CwtValueConfig>()?.propertyConfig?.overriddenProvider != null }
        if(isDirectChild) {
            if(configExpression.expressionString in SKIP_MISSING_CHECK_KEYS) return true
        }
        return false
    }
}
