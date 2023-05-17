package icu.windea.pls.lang.scope.impl

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
class ParadoxComplexTriggerModifierOverriddenScopeContextProvider : ParadoxOverriddenScopeContextProvider {
    override fun getOverriddenScopeContext(element: ParadoxScriptMemberElement, config: CwtDataConfig<*>): ParadoxScopeContext? {
        //重载complex_trigger_modifier = {...}中属性trigger的值对应的作用域上下文
        //兼容trigger_scope的值对应的作用域与当前作用域上下文不匹配的情况
        if(element !is ParadoxScriptProperty) return null
        if(config !is CwtPropertyConfig) return null
        if(config.key != "trigger") return null
        val aliasConfig = config.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return null
        if(aliasConfig.config.key != "alias[modifier_rule:complex_trigger_modifier]") return null
        val triggerScopeProperty = element.parent?.castOrNull<ParadoxScriptBlock>()?.findProperty("trigger_scope", inline = true) ?: return null
        val scopeContext = ParadoxScopeHandler.getScopeContext(triggerScopeProperty) ?: return null
        
        val scopeField = triggerScopeProperty.propertyValue?.stringText() ?: return null
        if(scopeField.isLeftQuoted()) return null
        val textRange = TextRange.create(0, scopeField.length)
        val configGroup = config.info.configGroup
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(scopeField, textRange, configGroup, true) ?: return null
        
        return ParadoxScopeHandler.resolveScopeContext(scopeFieldExpression, scopeContext)
    }
}