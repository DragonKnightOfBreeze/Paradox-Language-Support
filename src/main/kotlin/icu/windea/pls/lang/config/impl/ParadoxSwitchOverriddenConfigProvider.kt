package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.script.psi.*

//icu.windea.pls.lang.config.impl.ParadoxSwitchOverriddenConfigProvider

private const val CASE_KEY = "scalar"
private val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
private val CONTEXT_KEYS = arrayOf("switch", "inverted_switch")

class ParadoxSwitchOverriddenConfigProvider : ParadoxOverriddenConfigProvider {
    @Suppress("UNCHECKED_CAST")
    override fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T>? {
        //重载switch = {...}和inverted_switch = {...}中对应的CWT规则为scalar的属性的键对应的CWT规则
        //兼容使用内联或者使用封装变量的情况
        if(config !is CwtPropertyConfig) return null
        if(config.key != CASE_KEY) return null
        val aliasConfig = config.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.subName !in CONTEXT_KEYS) return null
        ProgressManager.checkCanceled()
        val triggerConfig = aliasConfig.config.configs?.find { it is CwtPropertyConfig && it.key in TRIGGER_KEYS && it.value == "alias_keys_field[trigger]" } ?: return null
        val triggerConfigKey = triggerConfig.castOrNull<CwtPropertyConfig>()?.key ?: return null
        val triggerProperty = contextElement.parentOfType<ParadoxScriptBlock>(withSelf = false)
            ?.findProperty(triggerConfigKey, inline = true)
            ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        if(CwtValueExpression.resolve(triggerName).type != CwtDataType.Constant) return null //must be predefined trigger
        val configGroup = config.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return null
        val resultConfigs = mutableListOf<CwtPropertyConfig>()
        for(resultTriggerConfig in resultTriggerConfigs) {
            if(resultTriggerConfig.config.isBlock) continue //not simple trigger, skip
            val inlined = ParadoxConfigGenerator.inlineWithConfig(config, resultTriggerConfig.config, ParadoxConfigGenerator.InlineMode.VALUE_TO_KEY) ?: continue
            resultConfigs.add(inlined)
        }
        return resultConfigs as List<T>
    }
}