package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Options
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

object ParadoxConfigResolver {
    fun getConfigs(element: PsiElement, orDefault: Boolean = true, matchOptions: Int = Options.Default): List<CwtMemberConfig<*>> {
        return when {
            element is ParadoxScriptDefinitionElement -> getPropertyConfigs(element, orDefault, matchOptions)
            element is ParadoxScriptPropertyKey -> getPropertyConfigs(element, orDefault, matchOptions)
            element is ParadoxScriptValue -> getValueConfigs(element, orDefault, matchOptions)
            else -> throw UnsupportedOperationException()
        }
    }
    
    fun getPropertyConfigs(element: PsiElement, orDefault: Boolean = true, matchOptions: Int = Options.Default): List<CwtPropertyConfig> {
        val configsMap = doGetConfigsCacheFromCache(element) ?: return emptyList()
        val cacheKey = buildString {
            append('p')
            append('#').append(orDefault.toInt())
            append('#').append(matchOptions)
        }
        return configsMap.getOrPut(cacheKey) { doGetPropertyConfigs(element, orDefault, matchOptions) }.cast()
    }
    
    fun getValueConfigs(element: PsiElement, orDefault: Boolean = true, matchOptions: Int = Options.Default): List<CwtValueConfig> {
        val configsMap = doGetConfigsCacheFromCache(element) ?: return emptyList()
        val cacheKey = buildString {
            append('v')
            append('#').append(orDefault.toInt())
            append('#').append(matchOptions)
        }
        return configsMap.getOrPut(cacheKey) { doGetValueConfigs(element, orDefault, matchOptions) }.cast()
    }
    
    private fun doGetConfigsCacheFromCache(element: PsiElement): MutableMap<String, List<CwtConfig<*>>>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigsCacheKey) {
            val value = ConcurrentHashMap<String, List<CwtConfig<*>>>()
            //invalidated on ScriptFileTracker
            //to optimize performance, do not invoke file.containingFile here
            val tracker = ParadoxPsiModificationTracker.getInstance(element.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetPropertyConfigs(element: PsiElement, orDefault: Boolean, matchOptions: Int): List<CwtPropertyConfig> {
        val memberElement = when {
            element is ParadoxScriptDefinitionElement -> element
            element is ParadoxScriptPropertyKey -> element.parent as? ParadoxScriptProperty ?: return emptyList()
            else -> throw UnsupportedOperationException()
        }
        val definitionMemberInfo = memberElement.definitionMemberInfo ?: return emptyList()
        if(definitionMemberInfo.elementPath.isEmpty() && !BitUtil.isSet(matchOptions, Options.AcceptDefinition)) return emptyList()
        
        val expression = when {
            element is ParadoxScriptProperty -> element.propertyValue?.let { ParadoxDataExpression.resolve(it, matchOptions) }
            element is ParadoxScriptFile -> BlockParadoxDataExpression
            element is ParadoxScriptPropertyKey -> element.propertyValue?.let { ParadoxDataExpression.resolve(it, matchOptions) }
            else -> throw UnsupportedOperationException()
        }
        
        //得到所有待匹配的结果
        val configs = ParadoxMemberConfigResolver.getConfigs(definitionMemberInfo, matchOptions).filterFast { it is CwtPropertyConfig }.cast<List<CwtPropertyConfig>>()
        
        //未填写属性的值 - 匹配所有
        if(expression == null) return configs
        
        //得到所有可能匹配的结果
        ProgressManager.checkCanceled()
        val configGroup = definitionMemberInfo.configGroup
        val matchResultValues = mutableListOf<ParadoxConfigMatcher.ResultValue<CwtPropertyConfig>>()
        configs.forEachFast f@{ config ->
            val matchResult = ParadoxConfigMatcher.matches(memberElement, expression, config.valueExpression, config, configGroup, matchOptions)
            if(matchResult == ParadoxConfigMatcher.Result.NotMatch) return@f
            matchResultValues.add(ParadoxConfigMatcher.ResultValue(config, matchResult))
        }
        //如果无结果且需要使用默认值，则返回所有可能匹配的规则
        if(matchResultValues.isEmpty() && orDefault) return configs
        
        val finalMatchResultValues = mutableListOf<ParadoxConfigMatcher.ResultValue<CwtPropertyConfig>>()
        doGetFinalResultValues(finalMatchResultValues, matchResultValues, matchOptions)
        if(finalMatchResultValues.isNotEmpty()) return finalMatchResultValues.mapFast { it.value }
        //如果仍然无结果且需要使用默认值，则返回所有待匹配的规则
        if(orDefault) return configs
        
        return emptyList()
    }
    
    private fun doGetValueConfigs(element: PsiElement, orDefault: Boolean, matchOptions: Int): List<CwtValueConfig> {
        val valueElement = when {
            element is ParadoxScriptValue -> element
            else -> throw UnsupportedOperationException()
        }
        val expression = ParadoxDataExpression.resolve(valueElement, matchOptions)
        val parent = element.parent
        when(parent) {
            //如果value是property的value
            is ParadoxScriptProperty -> {
                val property = parent
                val definitionMemberInfo = property.definitionMemberInfo ?: return emptyList()
                
                val memberConfigs = ParadoxMemberConfigResolver.getConfigs(definitionMemberInfo, matchOptions).filterFast { it is CwtPropertyConfig }.cast<List<CwtPropertyConfig>>()
                if(memberConfigs.isEmpty()) return emptyList()
                
                //得到所有可能匹配的结果
                ProgressManager.checkCanceled()
                val configGroup = definitionMemberInfo.configGroup
                val matchResultValues = mutableListOf<ParadoxConfigMatcher.ResultValue<CwtValueConfig>>()
                memberConfigs.forEachFast f@{ config ->
                    val valueConfig = config.valueConfig ?: return@f
                    val matchResult = ParadoxConfigMatcher.matches(valueElement, expression, valueConfig.expression, config, configGroup, matchOptions)
                    if(matchResult == ParadoxConfigMatcher.Result.NotMatch) return@f
                    matchResultValues.add(ParadoxConfigMatcher.ResultValue(valueConfig, matchResult))
                }
                //如果无结果且需要使用默认值，则返回所有可能匹配的规则
                if(matchResultValues.isEmpty() && orDefault) return memberConfigs.mapNotNullFast { it.valueConfig }
                
                val finalMatchResultValues = mutableListOf<ParadoxConfigMatcher.ResultValue<CwtValueConfig>>()
                doGetFinalResultValues(finalMatchResultValues, matchResultValues, matchOptions)
                if(finalMatchResultValues.isNotEmpty()) return finalMatchResultValues.mapFast { it.value }
                //如果仍然无结果且需要使用默认值，则返回所有待匹配的规则
                if(orDefault) return memberConfigs.mapNotNullFast { it.valueConfig }
                
                return emptyList()
            }
            //如果value是blockElement中的value
            is ParadoxScriptBlockElement -> {
                val property = parent.parent as? ParadoxScriptDefinitionElement ?: return emptyList()
                val definitionMemberInfo = property.definitionMemberInfo ?: return emptyList()
                
                val memberConfigs = ParadoxMemberConfigResolver.getChildConfigs(definitionMemberInfo, matchOptions).filterFast { it is CwtValueConfig }.cast<List<CwtValueConfig>>()
                if(memberConfigs.isEmpty()) return emptyList()
                
                //得到所有可能匹配的结果
                ProgressManager.checkCanceled()
                val configGroup = definitionMemberInfo.configGroup
                val matchResultValues = mutableListOf<ParadoxConfigMatcher.ResultValue<CwtValueConfig>>()
                memberConfigs.forEachFast f@{ config ->
                    val matchResult = ParadoxConfigMatcher.matches(valueElement, expression, config.valueExpression, config, configGroup, matchOptions)
                    if(matchResult == ParadoxConfigMatcher.Result.NotMatch) return@f
                    matchResultValues.add(ParadoxConfigMatcher.ResultValue(config, matchResult))
                }
                //如果无结果且需要使用默认值，则返回所有可能匹配的规则
                if(matchResultValues.isEmpty() && orDefault) return memberConfigs
                
                val finalMatchResultValues = mutableListOf<ParadoxConfigMatcher.ResultValue<CwtValueConfig>>()
                doGetFinalResultValues(finalMatchResultValues, matchResultValues, matchOptions)
                if(finalMatchResultValues.isNotEmpty()) return finalMatchResultValues.mapFast { it.value }
                //如果仍然无结果且需要使用默认值，则返回所有待匹配的规则
                if(orDefault) return memberConfigs
                
                return emptyList()
            }
            else -> return emptyList()
        }
    }
    
    private fun <T : CwtConfig<*>> doGetFinalResultValues(
        result: MutableList<ParadoxConfigMatcher.ResultValue<T>>,
        matchResultValues: MutableList<ParadoxConfigMatcher.ResultValue<T>>,
        matchOptions: Int
    ) {
        //* 首先尝试直接的精确匹配，如果有结果，则直接返回
        //* 然后，如果有多个需要检查子句/作用域上下文的匹配，则分别对它们进行进一步匹配，保留匹配的所有结果或者第一个结果（如果没有多个，直接认为是匹配的）
        //* 然后，进行进一步的匹配 （如果没有匹配结果，将不需要访问索引的匹配认为是匹配的）
        
        matchResultValues.filterFastTo(result) { v -> v.result == ParadoxConfigMatcher.Result.ExactMatch }
        if(result.isNotEmpty()) return
        
        var firstBlockAwareResult: ParadoxConfigMatcher.ResultValue<T>? = null
        var firstBlockAwareResultIndex = -1
        var firstScopeAwareResult: ParadoxConfigMatcher.ResultValue<T>? = null
        var firstScopeAwareResultIndex = -1
        for(i in matchResultValues.lastIndex downTo 0) {
            val v = matchResultValues[i]
            if(v.result is ParadoxConfigMatcher.Result.LazyBlockAwareMatch) {
                if(firstBlockAwareResult == null) {
                    firstBlockAwareResult = v
                    firstBlockAwareResultIndex = i
                } else {
                    if(firstBlockAwareResultIndex != -1) {
                        val r = firstBlockAwareResult.result.get(matchOptions)
                        if(!r) matchResultValues.removeAt(firstBlockAwareResultIndex)
                        firstBlockAwareResultIndex = -1
                    }
                    val r = v.result.get(matchOptions)
                    if(!r) matchResultValues.removeAt(i)
                }
            } else if(v.result is ParadoxConfigMatcher.Result.LazyScopeAwareMatch) {
                if(firstScopeAwareResult == null) {
                    firstScopeAwareResult = v
                    firstScopeAwareResultIndex = i
                } else {
                    if(firstScopeAwareResultIndex != -1) {
                        val r = firstScopeAwareResult.result.get(matchOptions)
                        if(!r) matchResultValues.removeAt(firstScopeAwareResultIndex)
                        firstScopeAwareResultIndex = -1
                    }
                    val r = v.result.get(matchOptions)
                    if(!r) matchResultValues.removeAt(i)
                }
            }
        }
        
        matchResultValues.filterFastTo(result) p@{ v ->
            if(v.result is ParadoxConfigMatcher.Result.LazyBlockAwareMatch) return@p true
            if(v.result is ParadoxConfigMatcher.Result.LazyScopeAwareMatch) return@p true
            v.result.get(matchOptions)
        }
        if(result.isNotEmpty()) return
        
        matchResultValues.filterFastTo(result) p@{ v ->
            if(v.result is ParadoxConfigMatcher.Result.LazyBlockAwareMatch) return@p true
            if(v.result is ParadoxConfigMatcher.Result.LazyScopeAwareMatch) return@p true
            if(v.result is ParadoxConfigMatcher.Result.LazySimpleMatch) return@p true
            v.result.get(matchOptions)
        }
    }
    
    //兼容需要考虑内联的情况（如内联脚本）
    //这里需要兼容匹配key的子句规则有多个的情况 - 匹配任意则使用匹配的首个规则，空子句或者都不匹配则使用合并的规则
    
    /**
     * 得到指定的[element]的作为值的子句中的子属性/值的出现次数信息。（先合并子规则）
     */
    fun getChildOccurrenceMap(element: ParadoxScriptMemberElement, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if(configs.isEmpty()) return emptyMap()
        val childConfigs = configs.flatMap { it.configs.orEmpty() }
        if(childConfigs.isEmpty()) return emptyMap()
        
        val childOccurrenceMap = doGetChildOccurrenceMapCacheFromCache(element) ?: return emptyMap()
        //NOTE cacheKey基于childConfigs即可，key相同而value不同的规则，上面的cardinality应当保证是一样的 
        val cacheKey = childConfigs.joinToString(" ")
        return childOccurrenceMap.getOrPut(cacheKey) { doGetChildOccurrenceMap(element, configs) }
    }
    
    private fun doGetChildOccurrenceMapCacheFromCache(element: ParadoxScriptMemberElement): MutableMap<String, Map<CwtDataExpression, Occurrence>>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedChildOccurrenceMapCacheKey) {
            val value = ConcurrentHashMap<String, Map<CwtDataExpression, Occurrence>>()
            //invalidated on ScriptFileTracker
            //to optimize performance, do not invoke file.containingFile here
            val tracker = ParadoxPsiModificationTracker.getInstance(element.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetChildOccurrenceMap(element: ParadoxScriptMemberElement, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if(configs.isEmpty()) return emptyMap()
        val configGroup = configs.first().info.configGroup
        //这里需要先按优先级排序
        val childConfigs = configs.flatMap { it.configs.orEmpty() }.sortedByPriority(configGroup) { it.expression }
        if(childConfigs.isEmpty()) return emptyMap()
        val project = configGroup.project
        val blockElement = when {
            element is ParadoxScriptDefinitionElement -> element.block
            element is ParadoxScriptBlockElement -> element
            else -> null
        }
        if(blockElement == null) return emptyMap()
        val occurrenceMap = mutableMapOf<CwtDataExpression, Occurrence>()
        for(childConfig in childConfigs) {
            occurrenceMap.put(childConfig.expression, childConfig.toOccurrence(element, project))
        }
        ProgressManager.checkCanceled()
        //注意这里需要考虑内联和可选的情况
        blockElement.processData(conditional = true, inline = true) p@{ data ->
            val expression = when {
                data is ParadoxScriptProperty -> ParadoxDataExpression.resolve(data.propertyKey)
                data is ParadoxScriptValue -> ParadoxDataExpression.resolve(data)
                else -> return@p true
            }
            val isParameterized = expression.type == ParadoxType.String && expression.text.isParameterized()
            //may contain parameter -> can't and should not get occurrences
            if(isParameterized) {
                occurrenceMap.clear()
                return@p true
            }
            val matched = childConfigs.find { childConfig ->
                if(childConfig is CwtPropertyConfig && data !is ParadoxScriptProperty) return@find false
                if(childConfig is CwtValueConfig && data !is ParadoxScriptValue) return@find false
                ParadoxConfigMatcher.matches(data, expression, childConfig.expression, childConfig, configGroup).get()
            }
            if(matched == null) return@p true
            val occurrence = occurrenceMap[matched.expression]
            if(occurrence == null) return@p true
            occurrence.actual += 1
            true
        }
        return occurrenceMap
    }
}