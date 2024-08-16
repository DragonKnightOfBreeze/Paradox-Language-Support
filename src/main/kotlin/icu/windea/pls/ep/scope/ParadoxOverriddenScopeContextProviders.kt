package icu.windea.pls.ep.scope

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

class ParadoxSwitchOverriddenScopeContextProvider: ParadoxOverriddenScopeContextProvider {
    object Constants {
        const val CASE_KEY = "scalar"
        const val DEFAULT_KEY = "default"
        val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
        val CONTEXT_NAMES = arrayOf("switch", "inverted_switch")
    }
    
    override fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
        //重载switch = {...}中对应的CWT规则为scalar的属性以及属性default对应的作用域上下文
        //重载inverted_switch = {...}中对应的CWT规则为scalar的属性以及属性default对应的作用域上下文
        val config1 = config.originalConfig
        if(config1 !is CwtPropertyConfig) return null
        if(config1.key != Constants.CASE_KEY && config1.key != Constants.DEFAULT_KEY) return null
        val aliasConfig = config1.parentConfig?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.subName !in Constants.CONTEXT_NAMES) return null
        ProgressManager.checkCanceled()
        val containerProperty = contextElement.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() in Constants.CONTEXT_NAMES }
            .find { ParadoxExpressionHandler.getConfigs(it).any { c -> c.inlineableConfig == aliasConfig } }
            ?: return null
        //基于trigger的值得到最终的scopeContext，然后推断目标属性的scopeContext
        val triggerProperty = containerProperty.findProperty(inline = true) { it in Constants.TRIGGER_KEYS }  ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        if(CwtDataExpression.resolve(triggerName, false).type != CwtDataTypes.Constant) return null //must be a predefined trigger
        val configGroup = config1.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return null
        val pushScope = resultTriggerConfigs.firstOrNull()?.config?.pushScope
        return parentScopeContext?.resolveNext(pushScope) ?: ParadoxScopeHandler.getAnyScopeContext().resolveNext(pushScope)
    }
}

class ParadoxTriggerWithParametersAwareOverriddenScopeContextProvider : ParadoxOverriddenScopeContextProvider {
    object Constants {
        const val TRIGGER_KEY = "trigger"
        const val TRIGGER_SCOPE_KEY = "trigger_scope"
        const val PARAMETERS_KEY = "parameters"
        val CONTEXT_NAMES = arrayOf("complex_trigger_modifier", "export_trigger_value_to_variable")
    }
    
    override fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
        //重载complex_trigger_modifier = {...}中属性trigger的值以及属性parameters对应的作用域上下文
        //重载export_trigger_value_to_variable = {...}中属性trigger的值以及属性parameters对应的作用域上下文
        //兼容trigger_scope的值对应的作用域与当前作用域上下文不匹配的情况
        val config1 = config.originalConfig
        if(config1 !is CwtPropertyConfig) return null
        if(config1.key != Constants.TRIGGER_KEY && config1.key != Constants.PARAMETERS_KEY) return null
        val aliasConfig = config1.parentConfig?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.subName !in Constants.CONTEXT_NAMES) return null
        ProgressManager.checkCanceled()
        val containerProperty = contextElement.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() in Constants.CONTEXT_NAMES }
            .find { ParadoxExpressionHandler.getConfigs(it).any { c -> c.inlineableConfig == aliasConfig } }
            ?: return null
        if(config1.key == Constants.TRIGGER_KEY) {
            //基于trigger_scope的值得到最终的scopeContext，然后推断属性trigger的值的scopeContext
            val triggerScopeProperty = containerProperty.findProperty(Constants.TRIGGER_SCOPE_KEY, inline = true) ?: return null
            val scopeContext = ParadoxScopeHandler.getSwitchedScopeContext(triggerScopeProperty) ?: return null
            val pv = triggerScopeProperty.propertyValue ?: return null
            val expressionString = pv.value
            val textRange =  TextRange.create(0, expressionString.length)
            val configGroup = config1.configGroup
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionString, textRange, configGroup) ?: return null
            return ParadoxScopeHandler.getSwitchedScopeContext(pv, scopeFieldExpression, scopeContext)
        }
        //基于trigger的值得到最终的scopeContext，然后推断属性parameters的scopeContext
        val triggerProperty = containerProperty.findProperty(Constants.TRIGGER_KEY, inline = true) ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        if(CwtDataExpression.resolve(triggerName, false).type != CwtDataTypes.Constant) return null //must be a predefined trigger
        val configGroup = config1.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return null
        val pushScope = resultTriggerConfigs.firstOrNull()?.config?.pushScope
        return parentScopeContext?.resolveNext(pushScope) ?: ParadoxScopeHandler.getAnyScopeContext().resolveNext(pushScope)
    }
}
