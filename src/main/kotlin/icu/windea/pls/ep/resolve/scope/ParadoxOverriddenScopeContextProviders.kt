package icu.windea.pls.ep.resolve.scope

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentsOfType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.originalConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxSwitchOverriddenScopeContextProvider : ParadoxOverriddenScopeContextProvider {
    // 重载 `switch = {...}` 中对应的规则为 `scalar` 的属性以及属性 `default` 对应的作用域上下文
    // 重载 `inverted_switch = {...}` 中对应的规则为 `scalar` 的属性以及属性 `default 对应的作用域上下文

    object Constants {
        const val caseKey = "scalar"
        const val defaultKey = "default"
        val triggerKeys = setOf("trigger", "on_trigger")
        val contextNames = setOf("switch", "inverted_switch")
    }

    override fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
        val finalConfig = config.originalConfig ?: config
        if (finalConfig !is CwtPropertyConfig) return null
        if (finalConfig.key != Constants.caseKey && finalConfig.key != Constants.defaultKey) return null
        val aliasConfig = finalConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return null
        if (aliasConfig.subName !in Constants.contextNames) return null
        ProgressManager.checkCanceled()
        val containerProperty = contextElement.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() in Constants.contextNames }
            .find { ParadoxConfigManager.getConfigs(it).any { c -> c is CwtPropertyConfig && c.aliasConfig == aliasConfig } }
            ?: return null
        // 基于 `trigger` 的值得到最终的 `scopeContext`，然后推断目标属性的 `scopeContext`
        val triggerProperty = selectScope { containerProperty.properties(inline = true).ofKeys(Constants.triggerKeys).one() } ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        if (CwtDataExpression.resolve(triggerName, false).type != CwtDataTypes.Constant) return null // must be a predefined trigger
        val configGroup = finalConfig.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return null
        val pushScope = resultTriggerConfigs.firstOrNull()?.config?.optionData?.pushScope
        return parentScopeContext?.resolveNext(pushScope) ?: ParadoxScopeContext.resolveAny().resolveNext(pushScope)
    }
}

class ParadoxTriggerWithParametersAwareOverriddenScopeContextProvider : ParadoxOverriddenScopeContextProvider {
    // 重载 `complex_trigger_modifier = {...}` 中属性 `trigger` 的值以及属性 `parameters` 对应的作用域上下文
    // 重载 `export_trigger_value_to_variable = {...}` 中属性 `trigger` 的值以及属性 `parameters` 对应的作用域上下文
    // 兼容 `trigger_scope` 的值对应的作用域与当前作用域上下文不匹配的情况

    object Constants {
        const val triggerKey = "trigger"
        const val triggerScopeKey = "trigger_scope"
        const val parametersKey = "parameters"
        val contextNames = setOf("complex_trigger_modifier", "export_trigger_value_to_variable")
    }

    override fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
        val finalConfig = config.originalConfig ?: config
        if (finalConfig !is CwtPropertyConfig) return null
        if (finalConfig.key != Constants.triggerKey && finalConfig.key != Constants.parametersKey) return null
        val aliasConfig = finalConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return null
        if (aliasConfig.subName !in Constants.contextNames) return null
        ProgressManager.checkCanceled()
        val containerProperty = contextElement.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() in Constants.contextNames }
            .find { ParadoxConfigManager.getConfigs(it).any { c -> c is CwtPropertyConfig && c.aliasConfig == aliasConfig } }
            ?: return null
        if (finalConfig.key == Constants.triggerKey) {
            // 基于 `trigger_scope` 的值得到最终的 `scopeContext`，然后推断属性 `trigger` 的值的 `scopeContext`
            val triggerScopeProperty = selectScope { containerProperty.properties(inline = true).ofKey(Constants.triggerScopeKey).one() } ?: return null
            val scopeContext = ParadoxScopeManager.getScopeContext(triggerScopeProperty) ?: return null
            val pv = triggerScopeProperty.propertyValue ?: return null
            val expressionString = pv.value
            val configGroup = finalConfig.configGroup
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionString, null, configGroup) ?: return null
            return ParadoxScopeManager.getScopeContext(pv, scopeFieldExpression, scopeContext)
        }
        // 基于 `trigger` 的值得到最终的 `scopeContext`，然后推断属性 `parameters` 的 `scopeContext`
        val triggerProperty = selectScope { containerProperty.properties(inline = true).ofKey(Constants.triggerKey).one() } ?: return null
        val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
        if (CwtDataExpression.resolve(triggerName, false).type != CwtDataTypes.Constant) return null // must be a predefined trigger
        val configGroup = finalConfig.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return null
        val pushScope = resultTriggerConfigs.firstOrNull()?.config?.optionData?.pushScope
        return parentScopeContext?.resolveNext(pushScope) ?: ParadoxScopeContext.resolveAny().resolveNext(pushScope)
    }
}
