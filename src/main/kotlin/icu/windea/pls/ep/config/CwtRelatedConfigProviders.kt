package icu.windea.pls.ep.config

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.modifier.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.CwtConfigMatcher.Options
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.psi.*

class CwtBaseRelatedConfigProvider: CwtRelatedConfigProvider {
    override fun getRelatedConfigs(element: PsiElement): List<CwtConfig<*>> {
        //获取所有匹配的CWT规则，不存在匹配的CWT规则时，选用所有默认的CWT规则（对于propertyConfig来说是匹配key的，对于valueConfig来说是所有）
        //包括内联规则（例如alias与single_alias，显示时使用特殊的别名图标）
        //包括其他一些相关的规则
        
        if(element !is ParadoxScriptExpressionElement) return emptyList()
        
        val orDefault = element is ParadoxScriptPropertyKey
        val matchOptions = Options.Default or Options.AcceptDefinition
        val configs = CwtConfigHandler.getConfigs(element, orDefault, matchOptions)
        if(configs.isEmpty()) return emptyList()
        
        val result = mutableListOf<CwtConfig<*>>()
        val configGroup = getConfigGroup(element.project, selectGameType(element))
        for(config in configs) {
            result.add(config)
            config.resolvedOrNull()?.also { result.add(it) }
            
            if(element is ParadoxScriptStringExpressionElement) {
                val name = element.value
                val configExpression = config.expression
                when {
                    configExpression.type in CwtDataTypeGroups.DynamicValue -> {
                        val type = configExpression.value
                        if(type != null) {
                            configGroup.dynamicValueTypes[type]?.valueConfigMap?.get(name)?.also { result.add(it) }
                        }
                    }
                    configExpression.type == CwtDataTypes.EnumValue -> {
                        val enumName = configExpression.value
                        if(enumName != null) {
                            configGroup.enums[enumName]?.valueConfigMap?.get(name)?.also { result.add(it) }
                            configGroup.complexEnums[enumName]?.also { result.add(it) }
                        }
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
    override fun getRelatedConfigs(element: PsiElement): List<CwtConfig<*>> {
        //包括其他一些相关的规则（扩展的规则）
        //definitions gameRules onActions parameters complexEnumValues dynamicValues
        
        val result = mutableListOf<CwtConfig<*>>()
        val configGroup = getConfigGroup(element.project, selectGameType(element))
        
        run r0@{
            run r1@ {
                if(element !is ParadoxScriptScriptedVariable) return@r1
                val name = element.name
                if(name.isNullOrEmpty()) return@r1
                if(name.isParameterized()) return@r1
                val config = configGroup.extendedScriptedVariables.getByTemplate(name, element, configGroup)
                if(config != null) result.add(config)
            }
            run r1@{
                if(element !is ParadoxScriptedVariableReference) return@r1
                val name = element.name
                if(name.isNullOrEmpty()) return@r1
                if(name.isParameterized()) return@r1
                val config = configGroup.extendedScriptedVariables.getByTemplate(name, element, configGroup)
                if(config != null) result.add(config)
            }
        }
        
        run r0@{
            if(element !is ParadoxScriptPropertyKey) return@r0
            val definition = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return@r0
            val definitionInfo = definition.definitionInfo ?: return@r0
            val definitionName = definitionInfo.name
            if(definitionName.isEmpty()) return@r0
            if(definitionName.isParameterized()) return@r0
            run r1@{
                val configs = configGroup.extendedDefinitions.getAllByTemplate(definitionName, definition, configGroup)
                val matchedConfigs = configs.filter { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) }
                result.addAll(matchedConfigs)
            }
            run r1@{
                if(definitionInfo.type != "game_rule") return@r1
                val config = configGroup.extendedGameRules.getByTemplate(definitionName, element, configGroup)
                if(config != null) result.add(config)
            }
            run r1@{
                if(definitionInfo.type != "on_action") return@r1
                val config = configGroup.extendedOnActions.getByTemplate(definitionName, element, configGroup)
                if(config != null) result.add(config)
            }
        }
        
        run r0@{
            if(element !is ParadoxScriptExpressionElement) return@r0
            for(reference in element.references) {
                when {
                    ParadoxResolveConstraint.Parameter.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxParameterElement>() ?: continue
                        configGroup.extendedParameters.getAllByTemplate(element.name, element, configGroup)
                            .filterTo(result) { it.contextKey == resolved.contextKey }
                    }
                    ParadoxResolveConstraint.ComplexEnumValue.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxComplexEnumValueElement>() ?: continue
                        val configs = configGroup.extendedComplexEnumValues[resolved.enumName] ?: continue
                        val config = configs.getByTemplate(resolved.name, element, configGroup) ?: continue
                        result.add(config)
                    }
                    ParadoxResolveConstraint.DynamicValueStrictly.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxDynamicValueElement>() ?: continue
                        val configs = configGroup.extendedDynamicValues[resolved.dynamicValueType] ?: continue
                        val config = configs.getByTemplate(resolved.name, element, configGroup) ?: continue
                        result.add(config)
                    }
                }
            }
        }
        
        return result
    }
}