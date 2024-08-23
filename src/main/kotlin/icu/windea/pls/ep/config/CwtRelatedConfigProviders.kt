package icu.windea.pls.ep.config

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.modifier.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

class CwtBaseRelatedConfigProvider : CwtRelatedConfigProvider {
    override fun getRelatedConfigs(file: PsiFile, offset: Int): List<CwtConfig<*>> {
        //获取所有匹配的CWT规则，不存在匹配的CWT规则时，选用所有默认的CWT规则（对于propertyConfig来说是匹配key的，对于valueConfig来说是所有）
        //包括内联规则以及内联后的规则
        //包括其他一些相关的规则
        //目前基本上仅适用于脚本文件中的目标
        
        val result = mutableListOf<CwtConfig<*>>()
        val configGroup = getConfigGroup(file.project, selectGameType(file))
        
        run r0@{
            val element = ParadoxPsiManager.findScriptExpression(file, offset) ?: return@r0
            
            val orDefault = element is ParadoxScriptPropertyKey
            val matchOptions = Options.Default or Options.AcceptDefinition
            val configs = ParadoxExpressionManager.getConfigs(element, orDefault, matchOptions)
            for(config in configs) {
                result.add(config)
                config.resolvedOrNull()?.also { result.add(it) }
                if(element !is ParadoxScriptStringExpressionElement) continue
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
                        val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup)
                        modifierElement?.modifierConfig?.also { result.add(it) }
                    }
                }
            }
        }
        
        return result
    }
}

class CwtExtendedRelatedConfigProvider : CwtRelatedConfigProvider {
    override fun getRelatedConfigs(file: PsiFile, offset: Int): List<CwtConfig<*>> {
        //包括其他一些相关的规则（扩展的规则 - definitions gameRules onActions parameters complexEnumValues dynamicValues）
        //目前基本上仅适用于脚本文件中的目标
        
        val result = mutableSetOf<CwtConfig<*>>()
        val configGroup = getConfigGroup(file.project, selectGameType(file))
        
        run r0@{
            val findOptions = ParadoxPsiManager.FindScriptedVariableOptions.run { BY_NAME or BY_REFERENCE }
            val element = ParadoxPsiManager.findScriptVariable(file, offset, findOptions) ?: return@r0
            val name = element.name
            if(name.isNullOrEmpty()) return@r0
            if(name.isParameterized()) return@r0
            val config = configGroup.extendedScriptedVariables.findFromPattern(name, element, configGroup)
            if(config != null) result.add(config)
        }
        
        run r0@{
            val findOptions = ParadoxPsiManager.FindDefinitionOptions.run { BY_NAME or BY_ROOT_KEY or BY_REFERENCE }
            val element = ParadoxPsiManager.findDefinition(file, offset, findOptions) ?: return@r0
            val definition = element
            val definitionInfo = definition.definitionInfo ?: return@r0
            val definitionName = definitionInfo.name
            if(definitionName.isEmpty()) return@r0
            if(definitionName.isParameterized()) return@r0
            run r1@{
                val extendedConfigs = configGroup.extendedDefinitions.findFromPattern(definitionName, definition, configGroup).orEmpty()
                val matchedConfigs = extendedConfigs.filter { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) }
                result.addAll(matchedConfigs)
            }
            run r1@{
                if(definitionInfo.type != "game_rule") return@r1
                val extendedConfig = configGroup.extendedGameRules.findFromPattern(definitionName, element, configGroup)
                if(extendedConfig != null) result.add(extendedConfig)
            }
            run r1@{
                if(definitionInfo.type != "on_action") return@r1
                val extendedConfig = configGroup.extendedOnActions.findFromPattern(definitionName, element, configGroup)
                if(extendedConfig != null) result.add(extendedConfig)
            }
        }
        
        run r0@{
            val element = file.findElementAt(offset) {
                it.parents(false).firstNotNullOfOrNull { p -> ParadoxParameterManager.getParameterElement(p) }
            } ?: return@r0
            val extendedConfigs = configGroup.extendedParameters.findFromPattern(element.name, element, configGroup).orEmpty()
                .filterTo(result) { it.contextKey.matchFromPattern(element.contextKey, element, configGroup) }
            result.addAll(extendedConfigs)
        }
        
        run r0@{
            val element = ParadoxPsiManager.findScriptExpression(file, offset) ?: return@r0
            if(element !is ParadoxScriptStringExpressionElement) return@r0
            val name = element.name
            
            for(reference in element.references) {
                when {
                    ParadoxResolveConstraint.Parameter.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxParameterElement>() ?: continue
                        val extendedConfigs = configGroup.extendedParameters.findFromPattern(name, element, configGroup).orEmpty()
                            .filterTo(result) { it.contextKey.matchFromPattern(resolved.contextKey, element, configGroup) }
                        result.addAll(extendedConfigs)
                    }
                    ParadoxResolveConstraint.ComplexEnumValue.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxComplexEnumValueElement>() ?: continue
                        val extendedConfigs = configGroup.extendedComplexEnumValues[resolved.enumName] ?: continue
                        val extendedConfig = extendedConfigs.findFromPattern(resolved.name, element, configGroup) ?: continue
                        result.add(extendedConfig)
                    }
                    ParadoxResolveConstraint.DynamicValueStrictly.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxDynamicValueElement>() ?: continue
                        for(type in resolved.dynamicValueTypes) {
                            val extendedConfigs = configGroup.extendedDynamicValues[type] ?: continue
                            val extendedConfig = extendedConfigs.findFromPattern(resolved.name, element, configGroup) ?: continue
                            result.add(extendedConfig)
                        }
                    }
                }
            }
            
            val orDefault = element is ParadoxScriptPropertyKey
            val matchOptions = Options.Default or Options.AcceptDefinition
            val configs = ParadoxExpressionManager.getConfigs(element, orDefault, matchOptions)
            for(config in configs) {
                val configExpression = config.expression
                when {
                    configExpression.expressionString == ParadoxInlineScriptManager.inlineScriptPathExpressionString -> {
                        val extendedConfig = configGroup.extendedInlineScripts.findFromPattern(name, element, configGroup) ?: continue
                        result.add(extendedConfig)
                    }
                }
            }
        }
        
        return result.toList()
    }
}
