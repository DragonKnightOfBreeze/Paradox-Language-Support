@file:Suppress("UnusedReceiverParameter", "UNUSED_PARAMETER")

package icu.windea.pls.lang

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.progress.*
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
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.CwtConfigMatcher.Options
import icu.windea.pls.lang.CwtConfigMatcher.ResultValue
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.configGroup.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.isNullOrEmpty

object CwtConfigHandler {
    //region Core Methods
    fun getContainingConfigGroup(element: PsiElement): CwtConfigGroup? {
        if(element.language != CwtLanguage) return null
        val file = element.containingFile ?: return null
        val vFile = file.virtualFile ?: return null
        val project = file.project
        return CwtConfigGroupFileProvider.EP_NAME.extensionList.firstNotNullOfOrNull { fileProvider ->
            fileProvider.getContainingConfigGroup(vFile, project)
        }
    }
    
    fun getConfigPath(element: PsiElement): CwtConfigPath? {
        if(element is CwtFile) return CwtConfigPath.EMPTY
        if(element !is CwtProperty && element !is CwtValue) return null
        return doGetConfigPathFromCache(element)
    }
    
    private fun doGetConfigPathFromCache(element: PsiElement): CwtConfigPath? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigPath) {
            val value = doGetConfigPath(element)
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    private fun doGetConfigPath(element: PsiElement): CwtConfigPath? {
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
        return CwtConfigPath.resolve(subPaths.joinToString("/"))
    }
    
    fun getConfigType(element: PsiElement): CwtConfigType? {
        if(element !is CwtProperty && element !is CwtValue) return null
        return doGetConfigTypeFromCache(element)
    }
    
    private fun doGetConfigTypeFromCache(element: PsiElement): CwtConfigType? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigType) {
            val file = element.containingFile ?: return@getCachedValue null
            val value = when(element) {
                is CwtProperty -> doGetConfigType(element)
                is CwtValue -> doGetConfigType(element)
                else -> null
            }
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun doGetConfigType(element: CwtProperty): CwtConfigType? {
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
                CwtConfigType.DynamicValueType
            }
            path.matchesAntPath("inline[*]") -> {
                CwtConfigType.Inline
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
            path.matchesAntPath("links/*") -> {
                CwtConfigType.Link
            }
            path.matchesAntPath("localisation_links/*") -> {
                CwtConfigType.LocalisationLink
            }
            path.matchesAntPath("localisation_commands/*") -> {
                CwtConfigType.LocalisationCommand
            }
            path.matchesAntPath("modifier_categories/*") -> {
                CwtConfigType.ModifierCategory
            }
            path.matchesAntPath("modifiers/*") -> {
                CwtConfigType.Modifier
            }
            path.matchesAntPath("scopes/*") -> {
                CwtConfigType.Scope
            }
            path.matchesAntPath("scope_groups/*") -> {
                CwtConfigType.ScopeGroup
            }
            path.matchesAntPath("system_links/*") -> {
                CwtConfigType.SystemLink
            }
            path.matchesAntPath("localisation_locales/*") -> {
                CwtConfigType.LocalisationLocale
            }
            path.matchesAntPath("localisation_predefined_parameters/*") -> {
                CwtConfigType.LocalisationPredefinedParameter
            }
            path.matchesAntPath("definitions/*") -> {
                CwtConfigType.Definition
            }
            path.matchesAntPath("game_rules/*") -> {
                CwtConfigType.GameRule
            }
            path.matchesAntPath("on_actions/*") -> {
                CwtConfigType.OnAction
            }
            else -> null
        }
    }
    
    private fun doGetConfigType(element: CwtValue): CwtConfigType? {
        val configPath = element.configPath
        if(configPath == null || configPath.isEmpty()) return null
        val path = configPath.path
        return when {
            path.matchesAntPath("enums/enum[*]/*") -> {
                CwtConfigType.EnumValue
            }
            path.matchesAntPath("values/value[*]/*") -> {
                CwtConfigType.DynamicValue
            }
            path.matchesAntPath("definitions/*") -> {
                CwtConfigType.Definition
            }
            path.matchesAntPath("game_rules/*") -> {
                CwtConfigType.GameRule
            }
            path.matchesAntPath("on_actions/*") -> {
                CwtConfigType.OnAction
            }
            else -> null
        }
    }
    
    fun getConfigContext(element: PsiElement): CwtConfigContext? {
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return null
        return doGetConfigContextFromCache(memberElement)
    }
    
    private fun doGetConfigContextFromCache(element: ParadoxScriptMemberElement): CwtConfigContext? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigContext) {
            val value = doGetConfigContext(element)
            //invalidated on ScriptFileTracker and LocalisationFileTracker (for loc references)
            val project = element.project
            val tracker1 = ParadoxModificationTrackerProvider.getInstance(project).ScriptFileTracker
            val tracker2 = ParadoxModificationTrackerProvider.getInstance(project).LocalisationFileTracker
            CachedValueProvider.Result.create(value, tracker1, tracker2)
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
                                    if(inlinedConfigs is MutableList) {
                                        CwtInjectedConfigProvider.injectConfigs(parentConfig, inlinedConfigs)
                                    }
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
        return configsMap.getOrPut(cacheKey) {
            val result = doGetConfigs(memberElement, orDefault, matchOptions)
            result.sortedByPriority({ it.expression }, { it.info.configGroup })
        }
    }
    
    private fun doGetConfigsCacheFromCache(element: PsiElement): MutableMap<String, List<CwtMemberConfig<*>>> {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigsCache) {
            val value = doGetConfigsCache()
            //invalidated on ScriptFileTracker and LocalisationFileTracker (for loc references)
            val project = element.project
            val tracker1 = ParadoxModificationTrackerProvider.getInstance(project).ScriptFileTracker
            val tracker2 = ParadoxModificationTrackerProvider.getInstance(project).LocalisationFileTracker
            CachedValueProvider.Result.create(value, tracker1, tracker2)
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
        ProgressManager.checkCanceled()
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
        //如果无结果，则返回空列表
        if(contextConfigsToMatch.isEmpty()) return emptyList()
        
        //如果无法获取valueExpression，则返回所有可能匹配的规则
        if(valueExpression == null) return contextConfigsToMatch
        
        //得到所有可能匹配的结果
        ProgressManager.checkCanceled()
        val matchResultValues = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
        contextConfigsToMatch.forEachFast f@{ config ->
            val matchResult = CwtConfigMatcher.matches(element, valueExpression, config.valueExpression, config, configGroup, matchOptions)
            if(matchResult == CwtConfigMatcher.Result.NotMatch) return@f
            matchResultValues.add(ResultValue(config, matchResult))
        }
        //如果无结果且需要使用默认值，则返回所有可能匹配的规则
        if(matchResultValues.isEmpty() && orDefault) return contextConfigsToMatch
        
        //进行进一步的匹配
        ProgressManager.checkCanceled()
        val finalMatchedConfigs = doGetFinalMatchedConfigs(matchResultValues, matchOptions)
        if(finalMatchedConfigs.isNotEmpty()) return finalMatchedConfigs
        
        //如果仍然无结果且需要使用默认值，则返回所有可能匹配的规则，否则返回空列表
        if(orDefault) return contextConfigsToMatch
        return emptyList()
    }
    
    private fun doGetFinalMatchedConfigs(matchResultValues: MutableList<ResultValue<CwtMemberConfig<*>>>, matchOptions: Int): List<CwtMemberConfig<*>> {
        //* 首先尝试直接的精确匹配，如果有结果，则直接返回
        //* 然后，尝试需要检测子句的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        //* 然后，尝试需要检测作用域上下文的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        //* 然后，尝试非回退的匹配，如果有结果，则直接返回
        //* 最后加入回退的匹配
        
        val exactMatched = matchResultValues.filterFast { it.result is CwtConfigMatcher.Result.ExactMatch }
        if(exactMatched.isNotEmpty()) return exactMatched.mapFast { it.value }
        
        val matched = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
        
        fun addLazyMatchedConfigs(predicate: (ResultValue<CwtMemberConfig<*>>) -> Boolean) {
            val lazyMatched = matchResultValues.filterFast(predicate)
            val lazyMatchedSize = lazyMatched.size
            if(lazyMatchedSize == 1) {
                matched += lazyMatched.first()
            } else if(lazyMatchedSize > 1){
                val oldMatchedSize = matched.size
                lazyMatched.filterToFast(matched) { it.result.get(matchOptions) }
                if(oldMatchedSize == matched.size) matched += lazyMatched.first()
            }
        }
        
        addLazyMatchedConfigs { it.result is CwtConfigMatcher.Result.LazyBlockAwareMatch }
        addLazyMatchedConfigs { it.result is CwtConfigMatcher.Result.LazyScopeAwareMatch }
        
        matchResultValues.filterToFast(matched) p@{
            if(it.result is CwtConfigMatcher.Result.LazyBlockAwareMatch) return@p false //已经匹配过
            if(it.result is CwtConfigMatcher.Result.LazyScopeAwareMatch) return@p false //已经匹配过
            if(it.result is CwtConfigMatcher.Result.LazySimpleMatch) return@p true //直接认为是匹配的
            if(it.result is CwtConfigMatcher.Result.FallbackMatch) return@p false //之后再匹配
            it.result.get(matchOptions)
        }
        if(matched.isNotEmpty()) return matched.mapFast { it.value }
        
        val fallbackMatched = matchResultValues.filterFast { it.result is CwtConfigMatcher.Result.FallbackMatch }
        if(fallbackMatched.isNotEmpty()) return fallbackMatched.mapFast { it.value }
        
        return emptyList()
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
                val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(element, config)
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
        return childOccurrenceMap.getOrPut(cacheKey) { doGetChildOccurrenceMap(element, configs) }
    }
    
    private fun doGetChildOccurrenceMapCacheFromCache(element: ParadoxScriptMemberElement): MutableMap<String, Map<CwtDataExpression, Occurrence>>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedChildOccurrenceMapCache) {
            val value = doGetChildOccurrenceMapCache()
            //invalidated on ScriptFileTracker and LocalisationFileTracker (for loc references)
            val project = element.project
            val tracker1 = ParadoxModificationTrackerProvider.getInstance(project).ScriptFileTracker
            val tracker2 = ParadoxModificationTrackerProvider.getInstance(project).LocalisationFileTracker
            CachedValueProvider.Result.create(value, tracker1, tracker2)
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
    fun getExpressionText(element: ParadoxScriptExpressionElement, rangeInElement: TextRange? = null): String {
        return when {
            element is ParadoxScriptBlock -> "" //should not be used
            element is ParadoxScriptInlineMath -> "" //should not be used
            else -> rangeInElement?.substring(element.text) ?: element.text.unquote()
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
        if(range.isEmpty) return
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
                    val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(context.contextElement!!, config)
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
                val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(context.contextElement!!, config)
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
        if(expression.type == CwtDataTypes.AliasName) return true
        val actualCount = occurrenceMap[expression]?.actual ?: 0
        //如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
        //如果上限是动态的值（如，基于define的值），也不作限制
        val cardinality = config.cardinality
        val maxCount = when {
            cardinality == null -> if(expression.type == CwtDataTypes.Constant) 1 else null
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
            cardinality == null -> if(expression.type == CwtDataTypes.Constant) 1 else null
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
                            infoMap.getOrPut(it) { mutableListOf() }.add(typeConfig to null)
                        }
                        typeConfig.subtypes.values.forEach { subtypeConfig ->
                            subtypeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                infoMap.getOrPut(it) { mutableListOf() }.add(typeConfig to subtypeConfig)
                            }
                        }
                    }
                } else {
                    for(skipConfig in skipRootKeyConfig) {
                        val relative = elementPath.relativeTo(skipConfig) ?: continue
                        if(relative.isEmpty()) {
                            typeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                infoMap.getOrPut(it) { mutableListOf() }.add(typeConfig to null)
                            }
                            typeConfig.subtypes.values.forEach { subtypeConfig ->
                                subtypeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                    infoMap.getOrPut(it) { mutableListOf() }.add(typeConfig to subtypeConfig)
                                }
                            }
                        } else {
                            infoMap.getOrPut(relative) { mutableListOf() }
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
            val icon = if(config != null) PlsIcons.Nodes.Definition(typeToUse) else PlsIcons.Nodes.Property
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
                .withIcon(PlsIcons.Nodes.TemplateExpression)
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
    
    fun completeDynamicValueExpression(context: ProcessingContext, result: CompletionResultSet) {
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
            val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(keyword, textRange, configGroup, config) ?: return
            return dynamicValueExpression.complete(context, result)
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
                .withIcon(PlsIcons.Nodes.SystemScope)
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
                .withIcon(PlsIcons.Nodes.Scope)
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
        if(dataSourceNodeToCheck is ParadoxDynamicValueExpression) {
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
                .withIcon(PlsIcons.Nodes.ValueLinkValue)
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
        
        if(dataSourceNodeToCheck is ParadoxDynamicValueExpression) {
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
    
    fun completeDynamicValue(context: ProcessingContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val config = context.config
        val configs = context.configs
        
        if(configs.isNotNullOrEmpty()) {
            for(c in configs) {
                doCompleteDynamicValue(context, result, c)
            }
        } else if(config != null) {
            doCompleteDynamicValue(context, result, config)
        }
    }
    
    private fun doCompleteDynamicValue(context: ProcessingContext, result: CompletionResultSet, config: CwtConfig<*>) {
        val keyword = context.keyword
        val contextElement = context.contextElement!!
        val configGroup = context.configGroup!!
        val project = configGroup.project
        
        val configExpression = config.expression ?: return
        val dynamicValueType = configExpression.value ?: return
        //提示预定义的value
        run {
            ProgressManager.checkCanceled()
            if(configExpression.type == CwtDataTypes.Value || configExpression.type == CwtDataTypes.DynamicValue) {
                completePredefinedDynamicValue(dynamicValueType, result, context)
            }
        }
        //提示来自脚本文件的value
        run {
            ProgressManager.checkCanceled()
            val tailText = " by $configExpression"
            val selector = dynamicValueSelector(project, contextElement).distinctByName()
            ParadoxDynamicValueSearch.search(dynamicValueType, selector).processQueryAsync p@{ info ->
                ProgressManager.checkCanceled()
                if(info.name == keyword) return@p true //排除和当前输入的同名的
                val element = ParadoxDynamicValueElement(contextElement, info, project)
                //去除后面的作用域信息
                val icon = PlsIcons.Nodes.DynamicValue(dynamicValueType)
                //不显示typeText
                val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, info.name)
                    .withIcon(icon)
                    .withTailText(tailText)
                result.addScriptExpressionElement(context, builder)
                true
            }
        }
    }
    
    fun completePredefinedDynamicValue(dynamicValueType: String, result: CompletionResultSet, context: ProcessingContext) {
        ProgressManager.checkCanceled()
        val configGroup = context.configGroup!!
        val config = context.config
        
        val tailText = getScriptExpressionTailText(config)
        val valueConfig = configGroup.dynamicValues[dynamicValueType] ?: return
        val dynamicValueConfigs = valueConfig.valueConfigMap.values
        if(dynamicValueConfigs.isEmpty()) return
        for(dynamicValueConfig in dynamicValueConfigs) {
            val name = dynamicValueConfig.value
            val element = dynamicValueConfig.pointer.element ?: continue
            val typeFile = valueConfig.pointer.containingFile
            val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
                .withIcon(PlsIcons.Nodes.DynamicValue)
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
                .withIcon(PlsIcons.LocalisationNodes.CommandScope)
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
                .withIcon(PlsIcons.LocalisationNodes.CommandField)
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
        
        val eventTargetSelector = dynamicValueSelector(project, file).contextSensitive().distinctByName()
        ParadoxDynamicValueSearch.search(ParadoxDynamicValueHandler.EVENT_TARGETS, eventTargetSelector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            if(info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxDynamicValueElement(contextElement, info, project)
            val icon = PlsIcons.Nodes.DynamicValue
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
            val icon = PlsIcons.Nodes.Definition("scripted_loc")
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
        
        val variableSelector = dynamicValueSelector(project, file).contextSensitive().distinctByName()
        ParadoxDynamicValueSearch.search("variable", variableSelector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            if(info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxDynamicValueElement(contextElement, info, project)
            val icon = PlsIcons.Nodes.Variable
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
            val icon = PlsIcons.LocalisationNodes.Concept
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
        
        val expression = getExpressionText(element, rangeInElement)
        
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
        
        val expression = getExpressionText(element, rangeInElement)
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
        
        val expression = getExpressionText(element, rangeInElement)
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
        
        if(configExpression.type == CwtDataTypes.Constant) return true
        if(configExpression.type == CwtDataTypes.EnumValue && configExpression.value?.let { configGroup.enums[it]?.values?.contains(expression.text) } == true) return true
        if(configExpression.type == CwtDataTypes.Value && configExpression.value?.let { configGroup.dynamicValues[it]?.values?.contains(expression.text) } == true) return true
        return false
    }
    
    fun isAliasEntryConfig(propertyConfig: CwtPropertyConfig): Boolean {
        return propertyConfig.keyExpression.type == CwtDataTypes.AliasName && propertyConfig.valueExpression.type == CwtDataTypes.AliasMatchLeft
    }
    
    fun isSingleAliasEntryConfig(propertyConfig: CwtPropertyConfig): Boolean {
        return propertyConfig.valueExpression.type == CwtDataTypes.SingleAliasRight
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
        return config.keyExpression.type == CwtDataTypes.Constant && config.cardinality?.isRequired() != false
    }
    //endregion
}
