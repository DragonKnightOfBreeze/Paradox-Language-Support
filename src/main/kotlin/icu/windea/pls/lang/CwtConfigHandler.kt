@file:Suppress("UnusedReceiverParameter", "UNUSED_PARAMETER")

package icu.windea.pls.lang

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.text.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.api.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.CwtConfigMatcher.Options
import icu.windea.pls.lang.CwtConfigMatcher.ResultValue
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.overridden.*
import icu.windea.pls.model.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.isNullOrEmpty

object CwtConfigHandler {
    //region Core Methods
    fun getPath(element: PsiElement): CwtConfigPath? {
        if(element is CwtFile) return EmptyCwtConfigPath
        if(element !is CwtProperty && element !is CwtValue) return null
        return doGetPathFromCache(element)
    }
    
    private fun doGetPathFromCache(element: PsiElement): CwtConfigPath? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigPath) {
            val value = doGetPath(element)
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    private fun doGetPath(element: PsiElement): CwtConfigPath? {
        var current: PsiElement = element
        var depth = 0
        val subPaths = LinkedList<String>()
        while(current !is PsiFile) {
            when {
                current is CwtProperty -> {
                    subPaths.addFirst(current.name)
                    depth++
                }
                current is CwtValue && current.isBlockValue() -> {
                    subPaths.addFirst("-")
                    depth++
                }
            }
            current = current.parent ?: break
        }
        if(current !is CwtFile) return null //unexpected
        return CwtConfigPath.resolve(subPaths)
    }
    
    fun getConfigType(element: PsiElement): CwtConfigType? {
        if(element !is CwtProperty && element !is CwtValue) return null
        return doGetConfigTypeFromCache(element)
    }
    
    private fun doGetConfigTypeFromCache(element: PsiElement): CwtConfigType? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigType) {
            val file = element.containingFile ?: return@getCachedValue null
            val value = when(element) {
                is CwtProperty -> doGetConfigType(element, file)
                is CwtValue -> doGetConfigType(element, file)
                else -> null
            }
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun doGetConfigType(element: CwtProperty, file: PsiFile): CwtConfigType? {
        val fileKey = file.name.substringBefore('.')
        val configPath = element.configPath
        if(configPath == null || configPath.isEmpty()) return null
        val path = configPath.path
        return when {
            path.matchesAntPath("types/type[*]") -> {
                CwtConfigType.Type
            }
            path.matchesAntPath("types/type[*]/subtype[*]") -> {
                CwtConfigType.Subtype
            }
            path.matchesAntPath("types/type[*]/modifiers/**") -> {
                when {
                    configPath.get(3).surroundsWith("subtype[", "]") -> {
                        if(configPath.length == 5) return CwtConfigType.Modifier
                    }
                    else -> {
                        if(configPath.length == 4) return CwtConfigType.Modifier
                    }
                }
                null
            }
            path.matchesAntPath("enums/enum[*]") -> {
                CwtConfigType.Enum
            }
            path.matchesAntPath("enums/complex_enum[*]") -> {
                CwtConfigType.ComplexEnum
            }
            path.matchesAntPath("values/value[*]") -> {
                CwtConfigType.ValueSet
            }
            fileKey == "on_actions" && path.matchesAntPath("on_actions/*") -> {
                CwtConfigType.OnAction
            }
            path.matchesAntPath("single_alias[*]") -> {
                CwtConfigType.SingleAlias
            }
            path.matchesAntPath("alias[*]") -> {
                val aliasName = configPath.get(0).substringIn('[', ']', "").substringBefore(':', "")
                when {
                    aliasName == "modifier" -> return CwtConfigType.Modifier
                    aliasName == "trigger" -> return CwtConfigType.Trigger
                    aliasName == "effect" -> return CwtConfigType.Effect
                }
                CwtConfigType.Alias
            }
            fileKey == "links" && path.matchesAntPath("links/*") -> {
                CwtConfigType.Link
            }
            fileKey == "localisation" && path.matchesAntPath("localisation_links/*") -> {
                CwtConfigType.LocalisationLink
            }
            fileKey == "localisation" && path.matchesAntPath("localisation_commands/*") -> {
                CwtConfigType.LocalisationCommand
            }
            fileKey == "modifier_categories" && path.matchesAntPath("modifier_categories/*") -> {
                CwtConfigType.ModifierCategory
            }
            fileKey == "modifiers" && path.matchesAntPath("modifiers/*") -> {
                CwtConfigType.Modifier
            }
            fileKey == "scopes" && path.matchesAntPath("scopes/*") -> {
                CwtConfigType.Scope
            }
            fileKey == "scopes" && path.matchesAntPath("scope_groups/*") -> {
                CwtConfigType.ScopeGroup
            }
            fileKey == "system_links" && path.matchesAntPath("system_links/*") -> {
                CwtConfigType.SystemLink
            }
            fileKey == "localisation_locales" && path.matchesAntPath("localisation_locales/*") -> {
                CwtConfigType.LocalisationLocale
            }
            fileKey == "localisation_predefined_parameters" && path.matchesAntPath("localisation_predefined_parameters/*") -> {
                CwtConfigType.LocalisationPredefinedParameter
            }
            else -> null
        }
    }
    
    @Suppress("UNUSED_VARIABLE")
    private fun doGetConfigType(element: CwtValue, file: PsiFile): CwtConfigType? {
        val fileKey = file.name.substringBefore('.')
        val configPath = element.configPath
        if(configPath == null || configPath.isEmpty()) return null
        val path = configPath.path
        return when {
            path.matchesAntPath("enums/enum[*]/*") -> CwtConfigType.EnumValue
            path.matchesAntPath("values/value[*]/*") -> CwtConfigType.ValueSetValue
            else -> null
        }
    }
    
    fun getConfigContext(element: PsiElement): CwtConfigContext? {
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return null
        return doGetConfigContextFromCache(memberElement)
    }
    
    private fun doGetConfigContextFromCache(element: ParadoxScriptMemberElement): CwtConfigContext? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigContext) {
            ProgressManager.checkCanceled()
            //invalidated on ScriptFileTracker
            val tracker = ParadoxPsiModificationTracker.getInstance(element.project).ScriptFileTracker
            val value = doGetConfigContext(element)
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetConfigContext(element: ParadoxScriptMemberElement): CwtConfigContext? {
        return CwtConfigContextProvider.getContext(element)
    }
    
    fun getConfigsForConfigContext(
        element: ParadoxScriptMemberElement,
        rootConfigs: List<CwtMemberConfig<*>>,
        elementPathFromRoot: ParadoxElementPath,
        configGroup: CwtConfigGroup,
        matchOptions: Int = Options.Default
    ): List<CwtMemberConfig<*>> {
        val result = doGetConfigsForConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        return result.sortedByPriority({ it.expression }, { it.info.configGroup })
    }
    
    private fun doGetConfigsForConfigContext(
        element: ParadoxScriptMemberElement,
        rootConfigs: List<CwtMemberConfig<*>>,
        elementPathFromRoot: ParadoxElementPath,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): List<CwtMemberConfig<*>> {
        val isPropertyValue = element is ParadoxScriptValue && element.isPropertyValue()
        
        var result: List<CwtMemberConfig<*>> = rootConfigs
        
        elementPathFromRoot.subPaths.forEachIndexedFast f1@{ i, info ->
            ProgressManager.checkCanceled()
            
            //如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
            //如果整个过程中的某个key匹配内联规则的名字（如，inline_script），则内联此内联规则
            
            val (_, subPath, isQuoted) = info
            val subPathIsParameterized = subPath.isParameterized()
            val matchKey = isPropertyValue || i < elementPathFromRoot.subPaths.lastIndex
            val expression = ParadoxDataExpression.resolve(subPath, isQuoted, true)
            val nextResult = mutableListOf<CwtMemberConfig<*>>()
            
            run r1@{
                result.forEachFast f2@{ parentConfig ->
                    val configs = parentConfig.configs
                    if(configs.isNullOrEmpty()) return@f2
                    //如果匹配带参数的子路径时，初始能够匹配到多个结果，则直接返回空列表
                    //参数值可能是任意值，如果初始能够匹配到多个结果，实际上并不能确定具体的上下文是什么
                    var matchCount = 0
                    configs.forEachFast f3@{ config ->
                        if(config is CwtPropertyConfig) {
                            if(subPathIsParameterized || !matchKey || CwtConfigMatcher.matches(element, expression, config.keyExpression, config, configGroup, matchOptions).get(matchOptions)) {
                                matchCount++
                                if(subPathIsParameterized && matchCount > 1) return emptyList()
                                val inlinedConfigs = CwtConfigManipulator.inlineByConfig(element, subPath, isQuoted, config, matchOptions)
                                if(inlinedConfigs.isEmpty()) {
                                    nextResult.add(config)
                                } else {
                                    nextResult.addAll(inlinedConfigs)
                                }
                            }
                        } else if(config is CwtValueConfig) {
                            nextResult.add(config)
                        }
                    }
                }
            }
            
            result = nextResult
            
            if(matchKey) result = doOptimizeContextConfigs(element, result, expression, matchOptions)
        }
        
        if(isPropertyValue) {
            result = result.mapNotNullToFast(mutableListOf<CwtMemberConfig<*>>()) { if(it is CwtPropertyConfig) it.valueConfig else null }
        }
        
        return result
    }
    
    fun getConfigs(
        element: PsiElement,
        orDefault: Boolean = true,
        matchOptions: Int = Options.Default
    ): List<CwtMemberConfig<*>> {
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return emptyList()
        val configsMap = doGetConfigsCacheFromCache(memberElement)
        val cacheKey = buildString {
            append('#').append(orDefault.toInt())
            append('#').append(matchOptions)
        }
        return configsMap.computeIfAbsent(cacheKey) {
            val result = doGetConfigs(memberElement, orDefault, matchOptions)
            result.sortedByPriority({ it.expression }, { it.info.configGroup })
        }
    }
    
    private fun doGetConfigsCacheFromCache(element: PsiElement): MutableMap<String, List<CwtMemberConfig<*>>> {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigsCache) {
            //invalidated on ScriptFileTracker
            val tracker = ParadoxPsiModificationTracker.getInstance(element.project).ScriptFileTracker
            val value = doGetConfigsCache()
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetConfigsCache(): MutableMap<String, List<CwtMemberConfig<*>>> {
        //use soft values to optimize memory
        return ContainerUtil.createConcurrentSoftValueMap()
    }
    
    private fun doGetConfigs(element: PsiElement, orDefault: Boolean, matchOptions: Int): List<CwtMemberConfig<*>> {
        //未填写属性的值 - 匹配所有
        val keyExpression = when {
            element is ParadoxScriptFile -> null
            element is ParadoxScriptProperty -> element.propertyKey.let { ParadoxDataExpression.resolve(it, matchOptions) }
            element is ParadoxScriptValue -> null
            else -> return emptyList()
        }
        val valueExpression = when {
            element is ParadoxScriptFile -> BlockParadoxDataExpression
            element is ParadoxScriptProperty -> element.propertyValue?.let { ParadoxDataExpression.resolve(it, matchOptions) }
            element is ParadoxScriptValue -> ParadoxDataExpression.resolve(element, matchOptions)
            else -> return emptyList()
        }
        
        val configContext = getConfigContext(element) ?: return emptyList()
        val isDefinition = configContext.isDefinition()
        if(isDefinition && element is ParadoxScriptDefinitionElement && !BitUtil.isSet(matchOptions, Options.AcceptDefinition)) return emptyList()
        val configGroup = configContext.configGroup
        
        //得到所有待匹配的结果
        val contextConfigs = configContext.getConfigs(matchOptions)
        if(contextConfigs.isEmpty()) return emptyList()
        val contextConfigsToMatch = contextConfigs
            .filterFast { config ->
                when {
                    element is ParadoxScriptProperty -> config is CwtPropertyConfig && run {
                        if(keyExpression == null) return@run true
                        if(isDefinition) return@run true
                        CwtConfigMatcher.matches(element, keyExpression, config.keyExpression, config, configGroup, matchOptions).get(matchOptions)
                    }
                    
                    element is ParadoxScriptValue -> config is CwtValueConfig
                    else -> true
                }
            }
            .let { configs -> doOptimizeContextConfigs(element, configs, keyExpression, matchOptions) }
        if(contextConfigsToMatch.isEmpty()) return emptyList()
        
        //得到所有可能匹配的结果
        ProgressManager.checkCanceled()
        if(valueExpression == null) return contextConfigsToMatch
        val matchResultValues = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
        contextConfigsToMatch.forEachFast f@{ config ->
            val matchResult = CwtConfigMatcher.matches(element, valueExpression, config.valueExpression, config, configGroup, matchOptions)
            if(matchResult == CwtConfigMatcher.Result.NotMatch) return@f
            matchResultValues.add(ResultValue(config, matchResult))
        }
        //如果无结果且需要使用默认值，则返回所有可能匹配的规则
        if(matchResultValues.isEmpty() && orDefault) return contextConfigsToMatch
        
        ProgressManager.checkCanceled()
        val finalMatchResultValues = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
        doGetFinalResultValues(finalMatchResultValues, matchResultValues, matchOptions)
        if(finalMatchResultValues.isNotEmpty()) return finalMatchResultValues.mapFast { it.value }
        //如果仍然无结果且需要使用默认值，则返回所有待匹配的规则
        if(orDefault) return contextConfigsToMatch
        
        return emptyList()
    }
    
    private fun doGetFinalResultValues(result: MutableList<ResultValue<CwtMemberConfig<*>>>, matchResultValues: MutableList<ResultValue<CwtMemberConfig<*>>>, matchOptions: Int) {
        //* 首先尝试直接的精确匹配，如果有结果，则直接返回
        //* 然后，如果有多个需要检查子句/作用域上下文的匹配，则分别对它们进行进一步匹配，保留匹配的所有结果或者第一个结果（如果没有多个，直接认为是匹配的）
        //* 然后，进行进一步的匹配 （如果没有匹配结果，将不需要访问索引的匹配认为是匹配的）
        
        matchResultValues.filterToFast(result) { v -> v.result == CwtConfigMatcher.Result.ExactMatch }
        if(result.isNotEmpty()) return
        
        var firstBlockAwareResult: ResultValue<CwtMemberConfig<*>>? = null
        var firstBlockAwareResultIndex = -1
        var firstScopeAwareResult: ResultValue<CwtMemberConfig<*>>? = null
        var firstScopeAwareResultIndex = -1
        for(i in matchResultValues.lastIndex downTo 0) {
            val v = matchResultValues[i]
            if(v.result is CwtConfigMatcher.Result.LazyBlockAwareMatch) {
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
            } else if(v.result is CwtConfigMatcher.Result.LazyScopeAwareMatch) {
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
        
        matchResultValues.filterToFast(result) p@{ v ->
            if(v.result is CwtConfigMatcher.Result.LazyBlockAwareMatch) return@p true
            if(v.result is CwtConfigMatcher.Result.LazyScopeAwareMatch) return@p true
            v.result.get(matchOptions)
        }
        if(result.isNotEmpty()) return
        
        matchResultValues.filterToFast(result) p@{ v ->
            if(v.result is CwtConfigMatcher.Result.LazyBlockAwareMatch) return@p true
            if(v.result is CwtConfigMatcher.Result.LazyScopeAwareMatch) return@p true
            if(v.result is CwtConfigMatcher.Result.LazySimpleMatch) return@p true
            v.result.get(matchOptions)
        }
    }
    
    private fun doOptimizeContextConfigs(element: PsiElement, configs: List<CwtMemberConfig<*>>, expression: ParadoxDataExpression?, matchOptions: Int): List<CwtMemberConfig<*>> {
        if(configs.isEmpty()) return emptyList()
        if(expression == null) return configs
        
        val configGroup = configs.first().info.configGroup
        var result = configs
        
        //如果结果不为空且结果中存在需要重载的规则，则全部替换成重载后的规则
        run r1@{
            if(result.isEmpty()) return@r1
            val optimizedResult = mutableListOf<CwtMemberConfig<*>>()
            result.forEachFast { config ->
                val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(element, config)
                if(overriddenConfigs.isNotNullOrEmpty()) {
                    //这里需要再次进行匹配
                    overriddenConfigs.forEachFast { overriddenConfig ->
                        if(CwtConfigMatcher.matches(element, expression, overriddenConfig.expression, overriddenConfig, configGroup, matchOptions).get(matchOptions)) {
                            optimizedResult.add(overriddenConfig)
                        }
                    }
                } else {
                    optimizedResult.add(config)
                }
                result = optimizedResult
            }
        }
        
        //如果要匹配的是字符串，且匹配结果中存在作为常量匹配的规则，则仅保留这些规则
        run r1@{
            if(expression.type != ParadoxType.String) return@r1
            if(result.size <= 1) return@r1
            val constantConfigs = result.filterFast { isConstantMatch(it.expression, expression, configGroup) }
            if(constantConfigs.isEmpty()) return@r1
            val optimizedConfigs = mutableListOf<CwtMemberConfig<*>>()
            optimizedConfigs.addAll(constantConfigs)
            result = optimizedConfigs
        }
        
        return result
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
        return childOccurrenceMap.computeIfAbsent(cacheKey) { doGetChildOccurrenceMap(element, configs) }
    }
    
    private fun doGetChildOccurrenceMapCacheFromCache(element: ParadoxScriptMemberElement): MutableMap<String, Map<CwtDataExpression, Occurrence>>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedChildOccurrenceMapCache) {
            //invalidated on ScriptFileTracker
            val tracker = ParadoxPsiModificationTracker.getInstance(element.project).ScriptFileTracker
            val value = doGetChildOccurrenceMapCache()
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetChildOccurrenceMapCache(): MutableMap<String, Map<CwtDataExpression, Occurrence>> {
        //use soft values to optimize memory
        return ContainerUtil.createConcurrentSoftValueMap()
    }
    
    private fun doGetChildOccurrenceMap(element: ParadoxScriptMemberElement, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if(configs.isEmpty()) return emptyMap()
        val configGroup = configs.first().info.configGroup
        //这里需要先按优先级排序
        val childConfigs = configs.flatMap { it.configs.orEmpty() }.sortedByPriority({ it.expression }, { configGroup })
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
            val matched = childConfigs.findFast { childConfig ->
                if(childConfig is CwtPropertyConfig && data !is ParadoxScriptProperty) return@findFast false
                if(childConfig is CwtValueConfig && data !is ParadoxScriptValue) return@findFast false
                CwtConfigMatcher.matches(data, expression, childConfig.expression, childConfig, configGroup).get()
            }
            if(matched == null) return@p true
            val occurrence = occurrenceMap[matched.expression]
            if(occurrence == null) return@p true
            occurrence.actual += 1
            true
        }
        return occurrenceMap
    }
    //endregion
    
    //region Expression Methods
    fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Int {
        return when(configExpression.type) {
            CwtDataType.Block -> 100
            CwtDataType.Bool -> 100
            CwtDataType.Int -> 90
            CwtDataType.Float -> 90
            CwtDataType.Scalar -> 90
            CwtDataType.ColorField -> 90
            CwtDataType.PercentageField -> 90
            CwtDataType.DateField -> 90
            CwtDataType.Localisation -> 60
            CwtDataType.SyncedLocalisation -> 60
            CwtDataType.InlineLocalisation -> 60
            CwtDataType.StellarisNameFormat -> 60
            CwtDataType.AbsoluteFilePath -> 70
            CwtDataType.Icon -> 70
            CwtDataType.FilePath -> 70
            CwtDataType.FileName -> 70
            CwtDataType.Definition -> 70
            CwtDataType.EnumValue -> {
                val enumName = configExpression.value ?: return 0 //unexpected
                if(configGroup.enums.containsKey(enumName)) return 80
                if(configGroup.complexEnums.containsKey(enumName)) return 45
                return 0 //不期望匹配到，规则有误！
            }
            CwtDataType.Value, CwtDataType.ValueOrValueSet -> {
                val valueSetName = configExpression.value ?: return 0 //unexpected
                if(configGroup.values.containsKey(valueSetName)) return 80
                return 40
            }
            CwtDataType.ValueSet -> 40
            CwtDataType.ScopeField -> 50
            CwtDataType.Scope -> 50
            CwtDataType.ScopeGroup -> 50
            CwtDataType.ValueField -> 45
            CwtDataType.IntValueField -> 45
            CwtDataType.VariableField -> 45
            CwtDataType.IntVariableField -> 45
            CwtDataType.Modifier -> 75 //higher than Definition
            CwtDataType.Parameter -> 10
            CwtDataType.ParameterValue -> 90 //same to Scalar
            CwtDataType.LocalisationParameter -> 10
            CwtDataType.ShaderEffect -> 85 // (80,90)
            CwtDataType.SingleAliasRight -> 0 //unexpected
            CwtDataType.AliasName -> 0 //unexpected
            CwtDataType.AliasKeysField -> 0 //unexpected
            CwtDataType.AliasMatchLeft -> 0 //unexpected
            CwtDataType.Template -> 65
            CwtDataType.Constant -> 100
            CwtDataType.Any -> 1
            CwtDataType.Other -> 0 //unexpected
        }
    }
    
    fun getExpressionText(element: ParadoxScriptExpressionElement, rangeInElement: TextRange? = null): String {
        return when {
            element is ParadoxScriptBlock -> "" //should not be used
            element is ParadoxScriptInlineMath -> "" //should not be used
            else -> rangeInElement?.substring(element.text) ?: element.text
        }
    }
    
    fun getExpressionTextRange(element: ParadoxScriptExpressionElement): TextRange {
        return when {
            element is ParadoxScriptBlock -> TextRange.create(0, 1) //left curly brace
            element is ParadoxScriptInlineMath -> element.firstChild.textRangeInParent
            else -> {
                val text = element.text
                TextRange.create(0, text.length).unquote(text) //unquoted text 
            }
        }
    }
    
    fun getExpressionTextRange(element: ParadoxScriptExpressionElement, text: String): TextRange {
        return when {
            element is ParadoxScriptBlock -> TextRange.create(0, 1) //left curly brace
            element is ParadoxScriptInlineMath -> element.firstChild.textRangeInParent
            else -> {
                TextRange.create(0, text.length).unquote(text) //unquoted text 
            }
        }
    }
    
    fun getParameterRanges(element: ParadoxScriptExpressionElement): List<TextRange> {
        var parameterRanges: MutableList<TextRange>? = null
        element.processChild { parameter ->
            if(parameter is ParadoxParameter) {
                if(parameterRanges == null) parameterRanges = mutableListOf()
                parameterRanges?.add(parameter.textRange)
            }
            true
        }
        return parameterRanges.orEmpty()
    }
    
    fun getParameterRangesInExpression(element: ParadoxScriptExpressionElement): List<TextRange> {
        var parameterRanges: MutableList<TextRange>? = null
        element.processChild { parameter ->
            if(parameter is ParadoxParameter) {
                if(parameterRanges == null) parameterRanges = mutableListOf()
                parameterRanges?.add(parameter.textRangeInParent)
            }
            true
        }
        return parameterRanges.orEmpty()
    }
    
    fun getParameterRangesInExpression(expression: String): List<TextRange> {
        val indices = expression.indicesOf('$')
        if(indices.size <= 1) return emptyList()
        return indices.windowed(2, 2, false) { TextRange.create(it[0], it[1] + 1) }
    }
    
    fun isUnaryOperatorAwareParameter(text: String, parameterRanges: List<TextRange>): Boolean {
        return text.firstOrNull()?.let { it == '+' || it == '-' } == true
            && parameterRanges.singleOrNull()?.let { it.startOffset == 1 && it.endOffset == text.length } == true
    }
    
    fun inParameterRanges(parameterRanges: List<TextRange>, index: Int): Boolean {
        return parameterRanges.any { index in it }
    }
    //endregion
    
    //region Annotate Methods
    fun annotateScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, holder: AnnotationHolder) {
        val expression = getExpressionText(element, rangeInElement)
        
        ParadoxScriptExpressionSupport.annotate(element, rangeInElement, expression, holder, config)
    }
    
    fun annotateScriptExpression(element: ParadoxScriptExpressionElement, range: TextRange, attributesKey: TextAttributesKey, holder: AnnotationHolder) {
        if(element !is ParadoxScriptStringExpressionElement) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
            return
        }
        //进行特殊代码高亮时，可能需要跳过字符串表达式中的参数部分
        val parameterRanges = element.getUserData(PlsKeys.parameterRanges).orEmpty()
        if(parameterRanges.isEmpty()) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
        } else {
            val finalRanges = TextRangeUtil.excludeRanges(range, parameterRanges)
            finalRanges.forEach { r ->
                if(!r.isEmpty) {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(r).textAttributes(attributesKey).create()
                }
            }
        }
    }
    
    fun annotateComplexExpression(element: ParadoxScriptExpressionElement, expression: ParadoxComplexExpression, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        doAnnotateComplexExpression(element, expression, holder, config)
    }
    
    private fun doAnnotateComplexExpression(element: ParadoxScriptStringExpressionElement, expressionNode: ParadoxExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = expressionNode.getAttributesKey()
        val mustUseAttributesKey = attributesKey != ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY && attributesKey != ParadoxScriptAttributesKeys.STRING_KEY
        if(attributesKey != null && mustUseAttributesKey) {
            doAnnotateComplexExpressionByAttributesKey(expressionNode, element, holder, attributesKey)
        } else {
            val attributesKeyConfig = expressionNode.getAttributesKeyConfig(element)
            if(attributesKeyConfig != null) {
                val rangeInElement = expressionNode.rangeInExpression.shiftRight(if(element.text.isLeftQuoted()) 1 else 0)
                annotateScriptExpression(element, rangeInElement, attributesKeyConfig, holder)
            } else if(attributesKey != null) {
                doAnnotateComplexExpressionByAttributesKey(expressionNode, element, holder, attributesKey)
            }
        }
        
        if(expressionNode.nodes.isNotEmpty()) {
            for(node in expressionNode.nodes) {
                doAnnotateComplexExpression(element, node, holder, config)
            }
        }
    }
    
    private fun doAnnotateComplexExpressionByAttributesKey(expressionNode: ParadoxExpressionNode, element: ParadoxScriptStringExpressionElement, holder: AnnotationHolder, attributesKey: TextAttributesKey) {
        val rangeToAnnotate = expressionNode.rangeInExpression.shiftRight(element.textRange.unquote(element.text).startOffset)
        if(expressionNode is ParadoxTokenExpressionNode) {
            //override default highlight by highlighter (property key or string)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(rangeToAnnotate).textAttributes(HighlighterColors.TEXT).create()
        }
        annotateScriptExpression(element, rangeToAnnotate, attributesKey, holder)
    }
    //endregion
    
    //region Complete Methods
    fun addRootKeyCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val originalFile = context.originalFile!!
        val project = originalFile.project
        val gameType = selectGameType(originalFile) ?: return
        val configGroup = getConfigGroup(project, gameType)
        val elementPath = ParadoxElementPathHandler.get(memberElement, PlsConstants.maxDefinitionDepth) ?: return
        
        context.isKey = true
        context.configGroup = configGroup
        
        completeRootKey(context, result, elementPath)
    }
    
    fun addKeyCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = getConfigContext(memberElement)
        if(configContext == null) return
        if(!configContext.isRootOrMember()) {
            //仅提示不在定义声明中的rootKey    
            addRootKeyCompletions(memberElement, context, result)
            return
        }
        
        val configGroup = configContext.configGroup
        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = Options.Default or Options.Relax or Options.AcceptDefinition
        val parentConfigs = getConfigs(memberElement, matchOptions = matchOptions)
        val configs = mutableListOf<CwtPropertyConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if(c2 is CwtPropertyConfig) {
                    configs.add(c2)
                }
            }
        }
        if(configs.isEmpty()) return
        val occurrenceMap = getChildOccurrenceMap(memberElement, parentConfigs)
        
        context.isKey = true
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(memberElement)
        
        configs.groupBy { it.key }.forEach { (_, configsWithSameKey) ->
            for(config in configsWithSameKey) {
                if(shouldComplete(config, occurrenceMap)) {
                    val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(context.contextElement!!, config)
                    if(overriddenConfigs.isNotNullOrEmpty()) {
                        for(overriddenConfig in overriddenConfigs) {
                            context.config = overriddenConfig
                            completeScriptExpression(context, result)
                        }
                        continue
                    }
                    context.config = config
                    context.configs = configsWithSameKey
                    completeScriptExpression(context, result)
                }
            }
        }
        
        context.config = null
        context.configs = emptyList()
        return
    }
    
    fun addValueCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = getConfigContext(memberElement)
        if(configContext == null) return
        if(!configContext.isRootOrMember()) return
        
        val configGroup = configContext.configGroup
        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchOptions = Options.Default or Options.Relax or Options.AcceptDefinition
        val parentConfigs = getConfigs(memberElement, matchOptions = matchOptions)
        val configs = mutableListOf<CwtValueConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if(c2 is CwtValueConfig) {
                    configs.add(c2)
                }
            }
        }
        if(configs.isEmpty()) return
        val occurrenceMap = getChildOccurrenceMap(memberElement, parentConfigs)
        
        context.isKey = false
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(memberElement)
        
        for(config in configs) {
            if(shouldComplete(config, occurrenceMap)) {
                val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(context.contextElement!!, config)
                if(overriddenConfigs.isNotNullOrEmpty()) {
                    for(overriddenConfig in overriddenConfigs) {
                        context.config = overriddenConfig
                        completeScriptExpression(context, result)
                    }
                    continue
                }
                context.config = config
                completeScriptExpression(context, result)
            }
        }
        
        context.config = null
        context.configs = emptyList()
        return
    }
    
    fun addPropertyValueCompletions(element: ParadoxScriptStringExpressionElement, propertyElement: ParadoxScriptProperty, context: ProcessingContext, result: CompletionResultSet) {
        val configContext = getConfigContext(element)
        if(configContext == null) return
        if(!configContext.isRootOrMember()) return
        
        val configGroup = configContext.configGroup
        val configs = configContext.getConfigs()
        if(configs.isEmpty()) return
        
        context.isKey = false
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(propertyElement)
        
        for(config in configs) {
            if(config is CwtValueConfig) {
                context.config = config
                completeScriptExpression(context, result)
            }
        }
        
        context.config = null
        return
    }
    
    private fun shouldComplete(config: CwtPropertyConfig, occurrenceMap: Map<CwtDataExpression, Occurrence>): Boolean {
        val expression = config.keyExpression
        //如果类型是aliasName，则无论cardinality如何定义，都应该提供补全（某些cwt规则文件未正确编写）
        if(expression.type == CwtDataType.AliasName) return true
        val actualCount = occurrenceMap[expression]?.actual ?: 0
        //如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
        //如果上限是动态的值（如，基于define的值），也不作限制
        val cardinality = config.cardinality
        val maxCount = when {
            cardinality == null -> if(expression.type == CwtDataType.Constant) 1 else null
            config.cardinalityMaxDefine != null -> null
            else -> cardinality.max
        }
        return maxCount == null || actualCount < maxCount
    }
    
    private fun shouldComplete(config: CwtValueConfig, occurrenceMap: Map<CwtDataExpression, Occurrence>): Boolean {
        val expression = config.valueExpression
        val actualCount = occurrenceMap[expression]?.actual ?: 0
        //如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
        //如果上限是动态的值（如，基于define的值），也不作限制
        val cardinality = config.cardinality
        val maxCount = when {
            cardinality == null -> if(expression.type == CwtDataType.Constant) 1 else null
            config.cardinalityMaxDefine != null -> null
            else -> cardinality.max
        }
        return maxCount == null || actualCount < maxCount
    }
    
    fun completeRootKey(context: ProcessingContext, result: CompletionResultSet, elementPath: ParadoxElementPath) {
        val fileInfo = context.originalFile!!.fileInfo ?: return
        val gameType = fileInfo.rootInfo.gameType
        val configGroup = context.configGroup!!
        val path = fileInfo.pathToEntry //这里使用pathToEntry
        val infoMap = mutableMapOf<String, MutableList<Tuple2<CwtTypeConfig, CwtSubtypeConfig?>>>()
        for(typeConfig in configGroup.types.values) {
            if(ParadoxDefinitionHandler.matchesTypeWithUnknownDeclaration(path, null, null, typeConfig)) {
                val skipRootKeyConfig = typeConfig.skipRootKey
                if(skipRootKeyConfig.isNullOrEmpty()) {
                    if(elementPath.isEmpty()) {
                        typeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                            infoMap.computeIfAbsent(it) { mutableListOf() }.add(typeConfig to null)
                        }
                        typeConfig.subtypes.values.forEach { subtypeConfig ->
                            subtypeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                infoMap.computeIfAbsent(it) { mutableListOf() }.add(typeConfig to subtypeConfig)
                            }
                        }
                    }
                } else {
                    for(skipConfig in skipRootKeyConfig) {
                        val relative = elementPath.relativeTo(skipConfig) ?: continue
                        if(relative.isEmpty()) {
                            typeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                infoMap.computeIfAbsent(it) { mutableListOf() }.add(typeConfig to null)
                            }
                            typeConfig.subtypes.values.forEach { subtypeConfig ->
                                subtypeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                    infoMap.computeIfAbsent(it) { mutableListOf() }.add(typeConfig to subtypeConfig)
                                }
                            }
                        } else {
                            infoMap.computeIfAbsent(relative) { mutableListOf() }
                        }
                        break
                    }
                }
            }
        }
        for((key, tuples) in infoMap) {
            if(key == "any") return //skip any wildcard
            val typeConfigToUse = tuples.map { it.first }.distinctBy { it.name }.singleOrNull()
            val typeToUse = typeConfigToUse?.name
            //需要考虑不指定子类型的情况
            val subtypesToUse = when {
                typeConfigToUse == null || tuples.isEmpty() -> null
                else -> tuples.mapNotNull { it.second }.ifEmpty { null }?.distinctBy { it.name }?.map { it.name }
            }
            val config = if(typeToUse == null) null else {
                val declarationConfig = configGroup.declarations.get(typeToUse)
                if(declarationConfig == null) null else {
                    val configContext = CwtDeclarationConfigContextProvider.getContext(context.contextElement!!, null, typeToUse, subtypesToUse, gameType, configGroup)
                    configContext?.getConfig(declarationConfig)
                }
            }
            val element = config?.pointer?.element
            val icon = if(config != null) PlsIcons.Definition else PlsIcons.Property
            val tailText = if(tuples.isEmpty()) null
            else tuples.joinToString(", ", " for ") { (typeConfig, subTypeConfig) ->
                if(subTypeConfig != null) "${typeConfig.name}.${subTypeConfig.name}" else typeConfig.name
            }
            val typeFile = config?.pointer?.containingFile
            context.config = config
            val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, key)
                .withIcon(icon)
                .withTailText(tailText)
                .withTypeText(typeFile?.name)
                .withTypeIcon(typeFile?.icon)
                .withForceInsertCurlyBraces(tuples.isEmpty())
                .bold()
                .caseInsensitive()
                .withPriority(PlsCompletionPriorities.rootKeyPriority)
            result.addScriptExpressionElement(context, builder)
            context.config = null
        }
    }
    
    fun completeScriptExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configExpression = context.config!!.expression ?: return
        val config = context.config!!
        val configGroup = context.configGroup!!
        val scopeMatched = context.scopeMatched
        val scopeContext = context.scopeContext
        
        if(configExpression.isEmpty()) return
        
        //匹配作用域
        if(scopeMatched) {
            val supportedScopes = when {
                config is CwtPropertyConfig -> config.supportedScopes
                config is CwtAliasConfig -> config.supportedScopes
                config is CwtLinkConfig -> config.inputScopes
                else -> null
            }
            val scopeMatched1 = when {
                scopeContext == null -> true
                else -> ParadoxScopeHandler.matchesScope(scopeContext, supportedScopes, configGroup)
            }
            if(!scopeMatched1 && getSettings().completion.completeOnlyScopeIsMatched) return
            context.scopeMatched = scopeMatched1
        }
        
        ParadoxScriptExpressionSupport.complete(context, result)
        
        context.scopeMatched = true
        context.scopeContext = scopeContext
    }
    
    fun completeAliasName(aliasName: String, context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config!!
        val configs = context.configs
        
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
        for(aliasConfigs in aliasGroup.values) {
            if(context.isKey == true) {
                context.config = aliasConfigs.first()
                context.configs = aliasConfigs
                completeScriptExpression(context, result)
            } else {
                context.config = aliasConfigs.first()
                completeScriptExpression(context, result)
            }
            context.config = config
            context.configs = configs
        }
    }
    
    fun completeTemplateExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val contextElement = context.contextElement!!
        val configGroup = context.configGroup!!
        val config = context.config!!
        val scopeMatched = context.scopeMatched
        
        if(contextElement !is ParadoxScriptStringExpressionElement) return
        val configExpression = config.expression ?: return
        val template = CwtTemplateExpression.resolve(configExpression.expressionString)
        val tailText = getScriptExpressionTailText(config)
        template.processResolveResult(contextElement, configGroup) { expression ->
            val templateExpressionElement = resolveTemplateExpression(contextElement, expression, configExpression, configGroup)
            val builder = ParadoxScriptExpressionLookupElementBuilder.create(templateExpressionElement, expression)
                .withIcon(PlsIcons.TemplateExpression)
                .withTailText(tailText)
                .caseInsensitive()
                .withScopeMatched(scopeMatched)
            result.addScriptExpressionElement(context, builder)
            true
        }
    }
    
    fun completeScopeFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val quoted = context.quoted
        val keyword = context.keyword
        val configGroup = context.configGroup!!
        if(quoted) return
        
        //基于当前位置的代码补全
        val keywordOffset = context.keywordOffset
        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        try {
            PlsContext.incompleteComplexExpression.set(true)
            val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(keyword, textRange, configGroup) ?: return
            return scopeFieldExpression.complete(context, result)
        } finally {
            PlsContext.incompleteComplexExpression.remove()
        }
    }
    
    fun completeValueFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val quoted = context.quoted
        val keyword = context.keyword
        val configGroup = context.configGroup!!
        
        if(quoted) return
        
        //基于当前位置的代码补全
        val keywordOffset = context.keywordOffset
        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        try {
            PlsContext.incompleteComplexExpression.set(true)
            val valueFieldExpression = ParadoxValueFieldExpression.resolve(keyword, textRange, configGroup) ?: return
            return valueFieldExpression.complete(context, result)
        } finally {
            PlsContext.incompleteComplexExpression.remove()
        }
    }
    
    fun completeVariableFieldExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val quoted = context.quoted
        val keyword = context.keyword
        val configGroup = context.configGroup!!
        
        if(quoted) return
        
        //基于当前位置的代码补全
        val keywordOffset = context.keywordOffset
        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        try {
            PlsContext.incompleteComplexExpression.set(true)
            val variableFieldExpression = ParadoxVariableFieldExpression.resolve(keyword, textRange, configGroup) ?: return
            return variableFieldExpression.complete(context, result)
        } finally {
            PlsContext.incompleteComplexExpression.remove()
        }
    }
    
    fun completeValueSetValueExpression(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val quoted = context.quoted
        val keyword = context.keyword
        val configGroup = context.configGroup!!
        val config = context.config!!
        
        if(quoted) return
        
        //基于当前位置的代码补全
        val keywordOffset = context.keywordOffset
        val textRange = TextRange.create(keywordOffset, keywordOffset + keyword.length)
        try {
            PlsContext.incompleteComplexExpression.set(true)
            val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(keyword, textRange, configGroup, config) ?: return
            return valueSetValueExpression.complete(context, result)
        } finally {
            PlsContext.incompleteComplexExpression.remove()
        }
    }
    
    fun completeSystemScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        
        //总是提示，无论作用域是否匹配
        val systemLinkConfigs = configGroup.systemLinks
        for(systemLinkConfig in systemLinkConfigs.values) {
            val name = systemLinkConfig.id
            val element = systemLinkConfig.pointer.element ?: continue
            val tailText = " from system scopes"
            val typeFile = systemLinkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.SystemScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withPriority(PlsCompletionPriorities.systemScopePriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completeScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val linkConfigs = configGroup.linksAsScopeNotData
        for(scope in linkConfigs.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, scope.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = scope.name
            val element = scope.pointer.element ?: continue
            val tailText = " from scopes"
            val typeFile = scope.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Scope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.scopePriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completeScopeLinkPrefix(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val linkConfigs = configGroup.linksAsScopeWithPrefix
        for(linkConfig in linkConfigs.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from scope link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.ScopeLinkPrefix)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.scopeLinkPrefixPriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completeScopeLinkDataSource(context: ProcessingContext, result: CompletionResultSet, prefix: String?, dataSourceNodeToCheck: ParadoxExpressionNode?) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs
        val scopeContext = context.scopeContext
        
        val linkConfigs = when {
            prefix == null -> configGroup.linksAsScopeWithoutPrefix.values
            else -> configGroup.linksAsScopeWithPrefix.values.filter { prefix == it.prefix }
        }
        
        if(dataSourceNodeToCheck is ParadoxScopeFieldExpressionNode) {
            completeForScopeExpressionNode(dataSourceNodeToCheck, context, result)
            context.scopeContext = scopeContext
            return
        }
        if(dataSourceNodeToCheck is ParadoxValueSetValueExpression) {
            dataSourceNodeToCheck.complete(context, result)
            return
        }
        
        context.configs = linkConfigs
        for(linkConfig in linkConfigs) {
            context.config = linkConfig
            completeScriptExpression(context, result)
        }
        context.config = config
        context.configs = configs
        context.scopeMatched = true
    }
    
    fun completeValueLinkValue(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val linkConfigs = configGroup.linksAsValueNotData
        for(linkConfig in linkConfigs.values) {
            //排除input_scopes不匹配前一个scope的output_scope的情况
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = linkConfig.name
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from values"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.ValueLinkValue)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
            result.addElement(lookupElement)
        }
    }
    
    fun completeValueLinkPrefix(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val linkConfigs = configGroup.linksAsValueWithPrefix
        for(linkConfig in linkConfigs.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = linkConfig.prefix ?: continue
            val element = linkConfig.pointer.element ?: continue
            val tailText = " from value link ${linkConfig.name}"
            val typeFile = linkConfig.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.ValueLinkPrefix)
                .withBoldness(true)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withPriority(PlsCompletionPriorities.valueLinkPrefixPriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completeValueLinkDataSource(context: ProcessingContext, result: CompletionResultSet, prefix: String?, dataSourceNodeToCheck: ParadoxExpressionNode?, variableOnly: Boolean = false) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        val configs = context.configs
        
        val linkConfigs = when {
            prefix == null -> configGroup.linksAsValueWithoutPrefix.values
            else -> configGroup.linksAsValueWithPrefix.values.filter { prefix == it.prefix }
        }
        
        if(dataSourceNodeToCheck is ParadoxValueSetValueExpression) {
            dataSourceNodeToCheck.complete(context, result)
            return
        }
        if(dataSourceNodeToCheck is ParadoxScriptValueExpression) {
            dataSourceNodeToCheck.complete(context, result)
            return
        }
        
        context.configs = linkConfigs
        for(linkConfig in linkConfigs) {
            context.config = linkConfig
            completeScriptExpression(context, result)
        }
        context.config = config
        context.configs = configs
        context.scopeMatched = true
    }
    
    fun completeValueSetValue(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val config = context.config
        val configs = context.configs
        
        if(configs.isNotNullOrEmpty()) {
            for(c in configs) {
                doCompleteValueSetValue(context, result, c)
            }
        } else if(config != null) {
            doCompleteValueSetValue(context, result, config)
        }
    }
    
    private fun doCompleteValueSetValue(context: ProcessingContext, result: CompletionResultSet, config: CwtConfig<*>) {
        val keyword = context.keyword
        val contextElement = context.contextElement!!
        val configGroup = context.configGroup!!
        val project = configGroup.project
        
        val configExpression = config.expression ?: return
        val valueSetName = configExpression.value ?: return
        //提示预定义的value
        run {
            ProgressManager.checkCanceled()
            if(configExpression.type == CwtDataType.Value || configExpression.type == CwtDataType.ValueOrValueSet) {
                completePredefinedValueSetValue(valueSetName, result, context)
            }
        }
        //提示来自脚本文件的value
        run {
            ProgressManager.checkCanceled()
            val tailText = " by $configExpression"
            val selector = valueSetValueSelector(project, contextElement).distinctByName()
            ParadoxValueSetValueSearch.search(valueSetName, selector).processQueryAsync p@{ info ->
                ProgressManager.checkCanceled()
                if(info.name == keyword) return@p true //排除和当前输入的同名的
                val element = ParadoxValueSetValueElement(contextElement, info, project)
                //去除后面的作用域信息
                val icon = PlsIcons.ValueSetValue(valueSetName)
                //不显示typeText
                val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, info.name)
                    .withIcon(icon)
                    .withTailText(tailText)
                result.addScriptExpressionElement(context, builder)
                true
            }
        }
    }
    
    fun completePredefinedValueSetValue(valueSetName: String, result: CompletionResultSet, context: ProcessingContext) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        
        val tailText = getScriptExpressionTailText(config)
        val valueConfig = configGroup.values[valueSetName] ?: return
        val valueSetValueConfigs = valueConfig.valueConfigMap.values
        if(valueSetValueConfigs.isEmpty()) return
        for(valueSetValueConfig in valueSetValueConfigs) {
            val name = valueSetValueConfig.value
            val element = valueSetValueConfig.pointer.element ?: continue
            val typeFile = valueConfig.pointer.containingFile
            val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.PredefinedValueSetValue)
                .withTailText(tailText)
                .withTypeText(typeFile?.name)
                .withTypeIcon(typeFile?.icon)
            result.addScriptExpressionElement(context, builder)
        }
    }
    
    fun completePredefinedLocalisationScope(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val localisationLinks = configGroup.localisationLinks
        for(localisationScope in localisationLinks.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, localisationScope.inputScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = localisationScope.name
            val element = localisationScope.pointer.element ?: continue
            val tailText = " from localisation scopes"
            val typeFile = localisationScope.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.LocalisationCommandScope)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
                .withPriority(PlsCompletionPriorities.scopePriority)
            result.addElement(lookupElement)
        }
    }
    
    fun completePredefinedLocalisationCommand(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val scopeContext = context.scopeContext
        
        val localisationCommands = configGroup.localisationCommands
        for(localisationCommand in localisationCommands.values) {
            val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, localisationCommand.supportedScopes, configGroup)
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
            
            val name = localisationCommand.name
            val element = localisationCommand.pointer.element ?: continue
            val tailText = " from localisation commands"
            val typeFile = localisationCommand.pointer.containingFile
            val lookupElement = LookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.LocalisationCommandField)
                .withTailText(tailText, true)
                .withTypeText(typeFile?.name, typeFile?.icon, true)
                .withCaseSensitivity(false) //忽略大小写
                .withScopeMatched(scopeMatched)
            result.addElement(lookupElement)
        }
    }
    
    fun completeEventTarget(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val contextElement = context.contextElement!!
        val keyword = context.keyword
        val file = context.originalFile!!
        val project = file.project
        
        val eventTargetSelector = valueSetValueSelector(project, file).contextSensitive().distinctByName()
        ParadoxValueSetValueSearch.search(ParadoxValueSetValueHandler.EVENT_TARGETS, eventTargetSelector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            if(info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxValueSetValueElement(contextElement, info, project)
            val icon = PlsIcons.ValueSetValue
            val tailText = " from value[event_target]"
            val lookupElement = LookupElementBuilder.create(element, info.name)
                .withIcon(icon)
                .withTailText(tailText, true)
                .withCaseSensitivity(false) //忽略大小写
            result.addElement(lookupElement)
            true
        }
    }
    
    fun completeScriptedLoc(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val file = context.originalFile!!
        val project = file.project
        
        val scriptedLocSelector = definitionSelector(project, file).contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search("scripted_loc", scriptedLocSelector).processQueryAsync p@{ scriptedLoc ->
            ProgressManager.checkCanceled()
            val name = scriptedLoc.definitionInfo?.name ?: return@p true //不应该为空
            val icon = PlsIcons.Definition
            val tailText = " from <scripted_loc>"
            val typeFile = scriptedLoc.containingFile
            val lookupElement = LookupElementBuilder.create(scriptedLoc, name).withIcon(icon)
                .withTailText(tailText, true)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withCaseSensitivity(false) //忽略大小写
            result.addElement(lookupElement)
            true
        }
    }
    
    fun completeVariable(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val contextElement = context.contextElement!!
        val keyword = context.keyword
        val file = context.originalFile!!
        val project = file.project
        
        val variableSelector = valueSetValueSelector(project, file).contextSensitive().distinctByName()
        ParadoxValueSetValueSearch.search("variable", variableSelector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            if(info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxValueSetValueElement(contextElement, info, project)
            val icon = PlsIcons.Variable
            val tailText = " from variables"
            val lookupElement = LookupElementBuilder.create(element, info.name)
                .withIcon(icon)
                .withTailText(tailText, true)
                .withCaseSensitivity(false) //忽略大小写
            result.addElement(lookupElement)
            true
        }
    }
    
    fun completeConcept(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val file = context.originalFile!!
        val project = file.project
        
        val conceptSelector = definitionSelector(project, file).contextSensitive().distinctByName()
        val keysToDistinct = mutableSetOf<String>()
        ParadoxDefinitionSearch.search("game_concept", conceptSelector).processQueryAsync p@{ element ->
            val tailText = " from concepts"
            val icon = PlsIcons.LocalisationConceptName
            run action@{
                val key = element.name
                if(!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(element, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                result.addElement(lookupElement)
            }
            element.getData<StellarisGameConceptDataProvider.Data>()?.alias?.forEach action@{ alias ->
                val key = alias
                if(!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(element, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                result.addElement(lookupElement)
            }
            true
        }
    }
    
    fun getScriptExpressionTailText(config: CwtConfig<*>?, withExpression: Boolean = true): String? {
        val configExpression = config?.expression ?: return null
        val fileName = config.resolved().pointer.containingFile?.name
        if(withExpression) {
            if(fileName != null) {
                return " by $configExpression in $fileName"
            } else {
                return " by $configExpression"
            }
        } else {
            if(fileName != null) {
                return " in $fileName"
            } else {
                return null
            }
        }
    }
    //endregion
    
    //region Resolve Methods
    fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, isKey: Boolean? = null): Array<out PsiReference>? {
        ProgressManager.checkCanceled()
        if(configExpression == null) return null
        
        val expression = getExpressionText(element, rangeInElement).unquote()
        
        val result = ParadoxScriptExpressionSupport.getReferences(element, rangeInElement, expression, config, isKey)
        if(result.isNotNullOrEmpty()) return result
        
        return null
    }
    
    /**
     * @param element 需要解析的PSI元素。
     * @param rangeInElement 需要解析的文本在需要解析的PSI元素对应的整个文本中的位置。
     */
    fun resolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        ProgressManager.checkCanceled()
        if(configExpression == null) return null
        
        val expression = getExpressionText(element, rangeInElement).unquote()
        if(expression.isParameterized()) return null //排除引用文本带参数的情况
        
        val result = ParadoxScriptExpressionSupport.resolve(element, rangeInElement, expression, config, isKey, exact)
        if(result != null) return result
        
        if(configExpression is CwtKeyExpression && configExpression.type.isKeyReferenceType()) {
            val resolvedConfig = config.resolved()
            if(resolvedConfig is CwtMemberConfig<*> && resolvedConfig.pointer.isEmpty()) {
                //特殊处理合成的CWT规则
                val gameType = configGroup.gameType ?: return null
                val project = configGroup.project
                return CwtMemberConfigElement(element, resolvedConfig, gameType, project)
            }
            return resolvedConfig.pointer.element
        }
        return null
    }
    
    fun multiResolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, isKey: Boolean? = null): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        if(configExpression == null) return emptySet()
        
        val expression = getExpressionText(element, rangeInElement).unquote()
        if(expression.isParameterized()) return emptySet() //排除引用文本带参数的情况
        
        val result = ParadoxScriptExpressionSupport.multiResolve(element, rangeInElement, expression, config, isKey)
        if(result.isNotEmpty()) return result
        
        if(configExpression is CwtKeyExpression && configExpression.type.isKeyReferenceType()) {
            val resolvedConfig = config.resolved()
            if(resolvedConfig is CwtMemberConfig<*> && resolvedConfig.pointer.isEmpty()) {
                //特殊处理合成的CWT规则
                val gameType = configGroup.gameType ?: return emptySet()
                val project = configGroup.project
                return CwtMemberConfigElement(element, resolvedConfig, gameType, project).toSingletonSetOrEmpty()
            }
            return resolvedConfig.pointer.element.toSingletonSetOrEmpty()
        }
        return emptySet()
    }
    
    fun resolveModifier(element: ParadoxScriptExpressionElement, name: String, configGroup: CwtConfigGroup): PsiElement? {
        if(element !is ParadoxScriptStringExpressionElement) return null
        ProgressManager.checkCanceled()
        return ParadoxModifierHandler.resolveModifier(name, element, configGroup)
    }
    
    fun resolveTemplateExpression(element: ParadoxScriptExpressionElement, text: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
        if(element !is ParadoxScriptStringExpressionElement) return null
        ProgressManager.checkCanceled()
        val templateConfigExpression = CwtTemplateExpression.resolve(configExpression.expressionString)
        return templateConfigExpression.resolve(text, element, configGroup)
    }
    
    fun resolvePredefinedScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val systemLink = configGroup.systemLinks[name] ?: return null
        val resolved = systemLink.pointer.element ?: return null
        resolved.putUserData(PlsKeys.cwtConfig, systemLink)
        return resolved
    }
    
    fun resolveScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.linksAsScopeNotData[name] ?: return null
        val resolved = linkConfig.pointer.element ?: return null
        resolved.putUserData(PlsKeys.cwtConfig, linkConfig)
        return resolved
    }
    
    fun resolveValueLinkValue(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.linksAsValueNotData[name] ?: return null
        val resolved = linkConfig.pointer.element ?: return null
        resolved.putUserData(PlsKeys.cwtConfig, linkConfig)
        return resolved
    }
    
    fun resolvePredefinedEnumValue(element: ParadoxScriptExpressionElement, name: String, enumName: String, configGroup: CwtConfigGroup): PsiElement? {
        val enumConfig = configGroup.enums[enumName] ?: return null
        val enumValueConfig = enumConfig.valueConfigMap.get(name) ?: return null
        val resolved = enumValueConfig.pointer.element ?: return null
        resolved.putUserData(PlsKeys.cwtConfig, enumValueConfig)
        return resolved
    }
    
    fun resolvePredefinedLocalisationScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.localisationLinks[name] ?: return null
        val resolved = linkConfig.pointer.element ?: return null
        resolved.putUserData(PlsKeys.cwtConfig, linkConfig)
        return resolved
    }
    
    fun resolvePredefinedLocalisationCommand(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val commandConfig = configGroup.localisationCommands[name] ?: return null
        val resolved = commandConfig.pointer.element ?: return null
        resolved.putUserData(PlsKeys.cwtConfig, commandConfig)
        return resolved
    }
    //endregion
    
    //region Misc Methods
    fun isConstantMatch(configExpression: CwtDataExpression, expression: ParadoxDataExpression, configGroup: CwtConfigGroup): Boolean {
        //注意这里可能需要在同一循环中同时检查keyExpression和valueExpression，因此这里需要特殊处理
        if(configExpression is CwtKeyExpression && expression.isKey == false) return false
        if(configExpression is CwtValueExpression && expression.isKey == true) return false
        
        if(configExpression.type == CwtDataType.Constant) return true
        if(configExpression.type == CwtDataType.EnumValue && configExpression.value?.let { configGroup.enums[it]?.values?.contains(expression.text) } == true) return true
        if(configExpression.type == CwtDataType.Value && configExpression.value?.let { configGroup.values[it]?.values?.contains(expression.text) } == true) return true
        return false
    }
    
    fun isAliasEntryConfig(propertyConfig: CwtPropertyConfig): Boolean {
        return propertyConfig.keyExpression.type == CwtDataType.AliasName && propertyConfig.valueExpression.type == CwtDataType.AliasMatchLeft
    }
    
    fun isSingleAliasEntryConfig(propertyConfig: CwtPropertyConfig): Boolean {
        return propertyConfig.valueExpression.type == CwtDataType.SingleAliasRight
    }
    
    @InferApi
    fun getConfigGroupFromCwtFile(file: PsiFile, project: Project): CwtConfigGroup? {
        val virtualFile = file.virtualFile ?: return null
        val path = virtualFile.path
        val gameTypeId = path.substringAfter("config/", "").substringBefore("/", "")
        if(gameTypeId.isEmpty()) return null
        return getConfigGroup(project, ParadoxGameType.resolve(gameTypeId))
    }
    
    fun getAliasSubName(element: PsiElement, key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, matchOptions: Int = Options.Default): String? {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) //不区分大小写
        if(constKey != null) return constKey
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return null
        val expression = ParadoxDataExpression.resolve(key, quoted, true)
        return keys.find { CwtConfigMatcher.matches(element, expression, CwtKeyExpression.resolve(it), null, configGroup, matchOptions).get(matchOptions) }
    }
    
    fun getAliasSubNames(element: PsiElement, key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, matchOptions: Int = Options.Default): List<String> {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) //不区分大小写
        if(constKey != null) return listOf(constKey)
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return emptyList()
        val expression = ParadoxDataExpression.resolve(key, quoted, true)
        return keys.filter { CwtConfigMatcher.matches(element, expression, CwtKeyExpression.resolve(it), null, configGroup, matchOptions).get(matchOptions) }
    }
    
    fun getEntryName(config: CwtConfig<*>): String? {
        return when {
            config is CwtPropertyConfig -> config.key
            config is CwtValueConfig && config.propertyConfig != null -> getEntryName(config.propertyConfig!!)
            config is CwtValueConfig -> null
            config is CwtAliasConfig -> config.subName
            else -> null
        }
    }
    
    fun getEntryConfigs(config: CwtConfig<*>): List<CwtMemberConfig<*>> {
        val configGroup = config.info.configGroup
        return when {
            config is CwtPropertyConfig -> {
                config.inlineableConfig?.let { getEntryConfigs(it) }
                    ?: config.parentConfig?.castOrNull<CwtPropertyConfig>()?.configs?.filterFast { it is CwtPropertyConfig && it.key == config.key }
                    ?: config.toSingletonList()
            }
            config is CwtValueConfig && config.propertyConfig != null -> {
                getEntryConfigs(config.propertyConfig!!)
            }
            config is CwtValueConfig -> {
                config.parentConfig?.castOrNull<CwtPropertyConfig>()?.configs?.filterIsInstance<CwtValueConfig>()
                    ?: config.toSingletonList()
            }
            config is CwtSingleAliasConfig -> {
                config.config.toSingletonListOrEmpty()
            }
            config is CwtAliasConfig -> {
                configGroup.aliasGroups.get(config.name)?.get(config.subName)?.map { it.config }.orEmpty()
            }
            else -> {
                emptyList()
            }
        }
    }
    
    val inBlockKeysKey = createKey<Set<String>>("cwt.config.inBlockKeys")
    
    fun getInBlockKeys(config: CwtMemberConfig<*>): Set<String> {
        return config.getOrPutUserData(inBlockKeysKey) {
            val keys = caseInsensitiveStringSet()
            config.configs?.forEach {
                if(it is CwtPropertyConfig && isInBlockKey(it)) {
                    keys.add(it.key)
                }
            }
            when {
                config is CwtPropertyConfig -> {
                    val propertyConfig = config
                    propertyConfig.parentConfig?.configs?.forEach { c ->
                        if(c is CwtPropertyConfig && c.key.equals(propertyConfig.key, true) && c.pointer != propertyConfig.pointer) {
                            c.configs?.forEach { if(it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                        }
                    }
                }
                config is CwtValueConfig -> {
                    val propertyConfig = config.propertyConfig
                    propertyConfig?.parentConfig?.configs?.forEach { c ->
                        if(c is CwtPropertyConfig && c.key.equals(propertyConfig.key, true) && c.pointer != propertyConfig.pointer) {
                            c.configs?.forEach { if(it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                        }
                    }
                }
            }
            return keys
        }
    }
    
    private fun isInBlockKey(config: CwtPropertyConfig): Boolean {
        return config.keyExpression.type == CwtDataType.Constant && config.cardinality?.isRequired() != false
    }
    //endregion
}
