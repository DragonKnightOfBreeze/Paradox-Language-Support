package icu.windea.pls.lang.config

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.CwtConfigMatcher.Options
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*

class CwtBaseRelatedConfigProvider: CwtRelatedConfigProvider {
    override fun getRelatedConfigs(element: ParadoxScriptExpressionElement): List<CwtConfig<*>> {
        //获取所有匹配的CWT规则，不存在匹配的CWT规则时，选用所有默认的CWT规则（对于propertyConfig来说是匹配key的，对于valueConfig来说是所有）
        //包括内联规则（例如alias，显示时使用特殊的别名图标）
        //包括其他一些相关的规则
        
        val orDefault = element is ParadoxScriptPropertyKey
        val matchOptions = Options.Default or Options.AcceptDefinition
        val configs = CwtConfigHandler.getConfigs(element, orDefault, matchOptions)
        val result = mutableListOf<CwtConfig<*>>()
        for(config in configs) {
            val configGroup = config.info.configGroup
            
            result.add(config)
            config.resolvedOrNull()?.also { result.add(it) }
            
            if(element is ParadoxScriptStringExpressionElement) {
                val name = element.value
                val configExpression = config.expression
                when {
                    configExpression.type == CwtDataTypes.EnumValue -> {
                        configGroup.enums[name]?.also { result.add(it) }
                        configGroup.complexEnums[name]?.also { result.add(it) }
                    }
                    configExpression.type == CwtDataTypes.Modifier -> {
                        val modifierElement = ParadoxModifierHandler.resolveModifier(name, element, configGroup)
                        modifierElement?.modifierConfig?.also { result.add(it) }
                    }
                }
            }
        }
        return result
    }
}

class CwtExtendedRelatedConfigProvider: CwtRelatedConfigProvider {
    override fun getRelatedConfigs(element: ParadoxScriptExpressionElement): List<CwtConfig<*>> {
        //包括其他一些相关的规则（扩展的规则）
        
        val orDefault = element is ParadoxScriptPropertyKey
        val matchOptions = Options.Default or Options.AcceptDefinition
        val configs = CwtConfigHandler.getConfigs(element, orDefault, matchOptions)
        val result = mutableListOf<CwtConfig<*>>()
        for(config in configs) {
            val configGroup = config.info.configGroup
            
            if(element is ParadoxScriptStringExpressionElement) {
                val name = element.value
                val configExpression = config.expression
                when {
                    config.isRoot -> {
                        when {
                            name == "game_rule" -> {
                                configGroup.gameRules.getByTemplate(name, element, configGroup)?.also { result.add(it) }
                            }
                            name == "on_action" -> {
                                configGroup.onActions.getByTemplate(name, element, configGroup)?.also { result.add(it) }
                            }
                        }
                    }
                    configExpression.type.isDynamicValueType() -> {
                        configGroup.dynamicValues[name]?.also { result.add(it) }
                    }
                    configExpression.type == CwtDataTypes.Parameter -> {
                        val parameterElement = element.reference?.resolve()?.castOrNull<ParadoxParameterElement>() ?: continue
                        val contextKey = parameterElement.contextKey
                        configGroup.parameters.getAllByTemplate(element.name, element, configGroup)
                            .filterTo(result) { it.contextKey == contextKey }
                    }
                }
            }
        }
        return result
    }
}