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

class ParadoxTriggerWithParametersAwareOverriddenScopeContextProvider : ParadoxOverriddenScopeContextProvider {
    object Data {
        const val TRIGGER_KEY = "trigger"
        const val TRIGGER_SCOPE_KEY = "trigger_scope"
        const val PARAMETERS_KEY = "parameters"
        val CONTEXT_NAMES = arrayOf("complex_trigger_modifier", "export_trigger_value_to_variable")
    }
    
    override fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
        //重载complex_trigger_modifier = {...}中属性trigger和parameters的值对应的作用域上下文
        //重载export_trigger_value_to_variable = {...}中属性trigger和parameters的值对应的作用域上下文
        //兼容trigger_scope的值对应的作用域与当前作用域上下文不匹配的情况
        if(config !is CwtPropertyConfig) return null
        if(config.key != Data.TRIGGER_KEY && config.key != Data.PARAMETERS_KEY) return null
        val aliasConfig = config.parentConfig?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.subName !in Data.CONTEXT_NAMES) return null
        ProgressManager.checkCanceled()
        val complexTriggerModifierProperty = contextElement.parentsOfType<ParadoxScriptProperty>(false)
            .filter { it.name.lowercase() in Data.CONTEXT_NAMES }
            .find { CwtConfigHandler.getConfigs(it).any { c -> c.inlineableConfig == aliasConfig } }
            ?: return null
        when {
            config.key == Data.TRIGGER_KEY -> {
                //基于trigger_scope的值得到最终的scopeContext，然后推断作为trigger的值的scopeContext
                val triggerScopeProperty = complexTriggerModifierProperty.findProperty(Data.TRIGGER_SCOPE_KEY, inline = true) ?: return null
                val scopeContext = ParadoxScopeHandler.getScopeContext(triggerScopeProperty) ?: return null
                val scopeField = triggerScopeProperty.propertyValue?.stringText() ?: return null
                if(scopeField.isLeftQuoted()) return null
                val textRange = TextRange.create(0, scopeField.length)
                val configGroup = config.configGroup
                val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(scopeField, textRange, configGroup) ?: return null
                return ParadoxScopeHandler.getScopeContext(contextElement, scopeFieldExpression, scopeContext)
            }
            config.key == Data.PARAMETERS_KEY -> {
                //基于trigger的值得到最终的scopeContext，然后推断作为parameters的值的scopeContext
                val triggerProperty = complexTriggerModifierProperty.findProperty(Data.TRIGGER_KEY, inline = true) ?: return null
                val triggerName = triggerProperty.propertyValue?.stringValue() ?: return null
                if(CwtDataExpression.resolve(triggerName, false).type != CwtDataTypes.Constant) return null //must be predefined trigger
                val configGroup = config.configGroup
                val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return null
                val pushScope = resultTriggerConfigs.firstOrNull()?.config?.pushScope
                return parentScopeContext?.resolveNext(pushScope) ?: ParadoxScopeHandler.getAnyScopeContext().resolveNext(pushScope)
            }
            else -> return null
        }
    }
}