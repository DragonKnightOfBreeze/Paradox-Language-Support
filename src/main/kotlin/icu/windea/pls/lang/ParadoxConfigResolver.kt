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
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

object ParadoxConfigResolver {
    fun getConfigs(element: PsiElement, allowDefinition: Boolean = element is ParadoxScriptValue, orDefault: Boolean = true, matchOptions: Int = ParadoxConfigMatcher.Options.Default): List<CwtMemberConfig<*>> {
        return when {
            element is ParadoxScriptDefinitionElement -> getPropertyConfigs(element, allowDefinition, orDefault, matchOptions)
            element is ParadoxScriptPropertyKey -> getPropertyConfigs(element, allowDefinition, orDefault, matchOptions)
            element is ParadoxScriptValue -> getValueConfigs(element, allowDefinition, orDefault, matchOptions)
            else -> throw UnsupportedOperationException()
        }
    }
    
    fun getPropertyConfigs(element: PsiElement, allowDefinition: Boolean = false, orDefault: Boolean = true, matchOptions: Int = ParadoxConfigMatcher.Options.Default): List<CwtPropertyConfig> {
        val configsMap = doGetConfigsCacheFromCache(element) ?: return emptyList()
        val cacheKey = buildString {
            append("property")
            append('#').append(allowDefinition.toIntString())
            append('#').append(orDefault.toIntString())
            append('#').append(matchOptions)
        }
        return configsMap.getOrPut(cacheKey) { doGetPropertyConfigs(element, allowDefinition, orDefault, matchOptions) }.cast()
    }
    
    fun getValueConfigs(element: PsiElement, allowDefinition: Boolean = true, orDefault: Boolean = true, matchOptions: Int = ParadoxConfigMatcher.Options.Default): List<CwtValueConfig> {
        val configsMap = doGetConfigsCacheFromCache(element) ?: return emptyList()
        val cacheKey = buildString {
            append("value")
            append('#').append(orDefault.toIntString())
            append('#').append(matchOptions)
        }
        return configsMap.getOrPut(cacheKey) { doGetValueConfigs(element, allowDefinition, orDefault, matchOptions) }.cast()
    }
    
    private fun doGetConfigsCacheFromCache(element: PsiElement): MutableMap<String, List<CwtConfig<*>>>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigsCacheKey) {
            val value = ConcurrentHashMap<String, List<CwtConfig<*>>>()
            //invalidated on file modification or ScriptFileTracker
            val file = element.containingFile
            val tracker = ParadoxPsiModificationTracker.getInstance(file.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, file, tracker)
        }
    }
    
    private fun doGetPropertyConfigs(element: PsiElement, allowDefinition: Boolean, orDefault: Boolean, matchOptions: Int): List<CwtPropertyConfig> {
        val memberElement = when {
            element is ParadoxScriptDefinitionElement -> element
            element is ParadoxScriptPropertyKey -> element.parent as? ParadoxScriptProperty ?: return emptyList()
            else -> throw UnsupportedOperationException()
        }
        val definitionMemberInfo = memberElement.definitionMemberInfo ?: return emptyList()
        if(!allowDefinition && definitionMemberInfo.elementPath.isEmpty()) return emptyList()
        
        val expression = when {
            element is ParadoxScriptProperty -> element.propertyValue?.let { ParadoxDataExpression.resolve(it, matchOptions) }
            element is ParadoxScriptFile -> BlockParadoxDataExpression
            element is ParadoxScriptPropertyKey -> element.propertyValue?.let { ParadoxDataExpression.resolve(it, matchOptions) }
            else -> throw UnsupportedOperationException()
        }
        
        //如果无法匹配value，则取第一个
        val configs = definitionMemberInfo.getConfigs(matchOptions)
        val configGroup = definitionMemberInfo.configGroup
        val resultConfigs = SmartList<CwtPropertyConfig>()
        run {
            //不完整的属性 - 不匹配值
            if(expression == null) {
                for(config in configs) {
                    if(config !is CwtPropertyConfig) continue
                    resultConfigs.add(config)
                }
                return@run
            }
            //得到匹配结果
            val matchResults = SmartList<ParadoxConfigMatcher.Result>()
            configs.forEachFast f@{ config ->
                if(config !is CwtPropertyConfig) return@f
                matchResults.add(ParadoxConfigMatcher.matches(memberElement, expression, config.valueExpression, config, configGroup, matchOptions))
            }
            run m@{
                
            }
            //过滤得到最终的匹配结果
            //精确匹配
            for(config in configs) {
                if(config !is CwtPropertyConfig) continue
                ProgressManager.checkCanceled()
                if(ParadoxConfigMatcher.matches(memberElement, expression, config.valueExpression, config, configGroup, matchOptions).get(matchOptions)) {
                    resultConfigs.add(config)
                }
            }
            //精确匹配无结果 - 不精确匹配
            if(resultConfigs.isEmpty()) {
                val newMatchOptions = matchOptions or ParadoxConfigMatcher.Options.Relax
                for(config in configs) {
                    if(config !is CwtPropertyConfig) continue
                    ProgressManager.checkCanceled()
                    val configExpression = config.valueExpression
                    if(!ParadoxConfigHandler.requireNotExactMatch(configExpression)) continue
                    if(ParadoxConfigMatcher.matches(memberElement, expression, configExpression, config, configGroup, newMatchOptions).get(newMatchOptions)) {
                        resultConfigs.add(config)
                    }
                }
            }
            //仍然无结果 - 判断是否使用默认值
            if(orDefault && resultConfigs.isEmpty()) {
                configs.forEachFast { it.castOrNull<CwtPropertyConfig>()?.let<CwtPropertyConfig, Unit> { c -> resultConfigs.add(c) } }
            }
        }
        return resultConfigs
    }
    
    private fun doGetValueConfigs(element: PsiElement, allowDefinition: Boolean, orDefault: Boolean, matchOptions: Int): List<CwtValueConfig> {
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
                if(!allowDefinition && definitionMemberInfo.elementPath.isEmpty()) return emptyList()
                
                ProgressManager.checkCanceled()
                val configs = definitionMemberInfo.getConfigs(matchOptions)
                val configGroup = definitionMemberInfo.configGroup
                val resultConfigs = SmartList<CwtValueConfig>()
                run {
                    //精确匹配
                    for(config in configs) {
                        if(config !is CwtPropertyConfig) continue
                        ProgressManager.checkCanceled()
                        val valueConfig = config.valueConfig ?: continue
                        if(ParadoxConfigMatcher.matches(valueElement, expression, valueConfig.expression, config, configGroup, matchOptions).get(matchOptions)) {
                            resultConfigs.add(valueConfig)
                        }
                    }
                    //精确匹配无结果 - 不精确匹配
                    if(resultConfigs.isEmpty()) {
                        val newMatchOptions = matchOptions or ParadoxConfigMatcher.Options.Relax
                        for(config in configs) {
                            if(config !is CwtPropertyConfig) continue
                            ProgressManager.checkCanceled()
                            val valueConfig = config.valueConfig ?: continue
                            val configExpression = valueConfig.expression
                            if(!ParadoxConfigHandler.requireNotExactMatch(configExpression)) continue
                            if(ParadoxConfigMatcher.matches(valueElement, expression, configExpression, config, configGroup, newMatchOptions).get(newMatchOptions)) {
                                resultConfigs.add(valueConfig)
                            }
                        }
                    }
                    //仍然无结果 - 判断是否使用默认值
                    if(orDefault && resultConfigs.isEmpty()) {
                        configs.forEachFast { it.castOrNull<CwtPropertyConfig>()?.valueConfig?.let<CwtValueConfig, Unit> { c -> resultConfigs.add(c) } }
                    }
                }
                return resultConfigs
            }
            //如果value是blockElement中的value
            is ParadoxScriptBlockElement -> {
                val property = parent.parent as? ParadoxScriptDefinitionElement ?: return emptyList()
                val definitionMemberInfo = property.definitionMemberInfo ?: return emptyList()
                
                val childConfigs = definitionMemberInfo.getChildConfigs(matchOptions)
                if(childConfigs.isEmpty()) return emptyList()
                val configGroup = definitionMemberInfo.configGroup
                val resultConfigs = SmartList<CwtValueConfig>()
                run {
                    for(childConfig in childConfigs) {
                        if(childConfig !is CwtValueConfig) continue
                        ProgressManager.checkCanceled()
                        //精确匹配
                        if(ParadoxConfigMatcher.matches(valueElement, expression, childConfig.valueExpression, childConfig, configGroup, matchOptions).get(matchOptions)) {
                            resultConfigs.add(childConfig)
                        }
                    }
                    //精确匹配无结果 - 不精确匹配
                    if(resultConfigs.isEmpty()) {
                        val newMatchOptions = matchOptions or ParadoxConfigMatcher.Options.Relax
                        for(childConfig in childConfigs) {
                            if(childConfig !is CwtValueConfig) continue
                            ProgressManager.checkCanceled()
                            val configExpression = childConfig.valueExpression
                            if(!ParadoxConfigHandler.requireNotExactMatch(configExpression)) continue
                            if(ParadoxConfigMatcher.matches(valueElement, expression, configExpression, childConfig, configGroup, newMatchOptions).get(newMatchOptions)) {
                                resultConfigs.add(childConfig)
                            }
                        }
                    }
                    //仍然无结果 - 判断是否使用默认值
                    if(orDefault && resultConfigs.isEmpty()) {
                        childConfigs.singleOrNull { it is CwtValueConfig }?.cast<CwtValueConfig>()?.let { resultConfigs.add(it) }
                    }
                }
                return resultConfigs
            }
            else -> return emptyList()
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
            //invalidated on file modification or ScriptFileTracker
            val file = element.containingFile
            val tracker = ParadoxPsiModificationTracker.getInstance(file.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, file, tracker)
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