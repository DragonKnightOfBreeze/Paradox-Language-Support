package icu.windea.pls.lang.util

import com.intellij.lang.annotation.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.text.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.CwtConfigMatcher.Options
import icu.windea.pls.lang.util.CwtConfigMatcher.ResultValue
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.model.path.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*
import kotlin.collections.isNullOrEmpty

object CwtConfigHandler {
    //region Core Methods
    fun getConfigContext(element: PsiElement): CwtConfigContext? {
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return null
        return doGetConfigContextFromCache(memberElement)
    }
    
    private fun doGetConfigContextFromCache(element: ParadoxScriptMemberElement): CwtConfigContext? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigContext) {
            val value = doGetConfigContext(element)
            val trackers = buildList<Any> {
                this += ParadoxModificationTrackers.ScriptFileTracker
                this += ParadoxModificationTrackers.LocalisationFileTracker //for loc references
            }
            value.withDependencyItems(trackers)
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
        return result.sortedByPriority({ it.expression }, { it.configGroup })
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
        
        val subPaths = elementPathFromRoot.subPaths
        val originalSubPaths = elementPathFromRoot.originalSubPaths
        subPaths.forEachIndexedFast f1@{ i, subPath ->
            ProgressManager.checkCanceled()
            
            //如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
            //如果整个过程中的某个key匹配内联规则的名字（如，inline_script），则需要内联此内联规则
            
            val isQuoted = subPath != originalSubPaths[i]
            val isParameterized = subPath.isParameterized()
            val isFullParameterized = subPath.isFullParameterized()
            val shift = subPaths.lastIndex - i
            val matchesKey = isPropertyValue || shift > 0
            val expression = ParadoxDataExpression.resolve(subPath, isQuoted, true)
            val nextResult = mutableListOf<CwtMemberConfig<*>>()
            
            val parameterizedKeyConfigs by lazy {
                if(!isParameterized) return@lazy null
                if(!isFullParameterized) return@lazy null //must be full parameterized yet
                ParadoxParameterHandler.getParameterizedKeyConfigs(element, shift)
            }
            
            run r1@{
                result.forEachFast f2@{ parentConfig ->
                    val configs = parentConfig.configs
                    if(configs.isNullOrEmpty()) return@f2
                    
                    var count = 0
                    configs.forEachFast f3@{ config ->
                        if(config is CwtPropertyConfig) {
                            fun processFinalConfig(config: CwtPropertyConfig) {
                                doMatchParameterizedKeyConfigs(parameterizedKeyConfigs, config.keyExpression)?.let {
                                    if(!it) return
                                    nextResult.add(config)
                                    return
                                }
                                count++
                                nextResult.add(config)
                            }
                            
                            if(subPath == "-") return@f3
                            val matchesPropertyConfig = !matchesKey || CwtConfigMatcher.matches(element, expression, config.keyExpression, config, configGroup, matchOptions).get(matchOptions)
                            if(!matchesPropertyConfig) return@f3
                            val inlinedConfigs = CwtConfigManipulator.inlineSingleAliasOrAlias(element, subPath, isQuoted, config, matchOptions)
                            if(inlinedConfigs.isEmpty()) {
                                processFinalConfig(config)
                            } else {
                                inlinedConfigs.forEachFast { processFinalConfig(it) }
                            }
                        } else if(config is CwtValueConfig) {
                            if(subPath != "-") return@f3
                            nextResult.add(config)
                        }
                    }
                    
                    //如果需要匹配键，且匹配带参数的子路径时，初始能够匹配到多个结果，则直接返回空列表
                    //因为参数值可能是任意值，此时实际上并不能确定具体的上下文是什么
                    
                    if(matchesKey && isParameterized && count > 1) {
                        return emptyList()
                    }
                }
            }
            
            result = nextResult
            
            if(matchesKey) result = doOptimizeContextConfigs(element, result, expression, matchOptions)
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
            result.sortedByPriority({ it.expression }, { it.configGroup })
        }
    }
    
    private fun doGetConfigsCacheFromCache(element: PsiElement): MutableMap<String, List<CwtMemberConfig<*>>> {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigsCache) {
            val value = doGetConfigsCache()
            val trackers = buildList<Any> {
                this += ParadoxModificationTrackers.ScriptFileTracker
                this += ParadoxModificationTrackers.LocalisationFileTracker //for loc references
            }
            value.withDependencyItems(trackers)
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
            element is ParadoxScriptFile -> ParadoxDataExpression.BlockExpression
            element is ParadoxScriptProperty -> element.propertyValue?.let { ParadoxDataExpression.resolve(it, matchOptions) }
            element is ParadoxScriptValue -> ParadoxDataExpression.resolve(element, matchOptions)
            else -> return emptyList()
        }
        
        val configContext = getConfigContext(element) ?: return emptyList()
        val isDefinition = configContext.isDefinition()
        if(isDefinition && element is ParadoxScriptDefinitionElement && !BitUtil.isSet(matchOptions, Options.AcceptDefinition)) return emptyList()
        val configGroup = configContext.configGroup
        
        val parameterizedKeyConfigs by lazy {
            val expression = keyExpression ?: return@lazy null
            if(!expression.isParameterized()) return@lazy null
            if(!expression.isFullParameterized()) return@lazy null //must be full parameterized yet
            ParadoxParameterHandler.getParameterizedKeyConfigs(element, 0)
        }
        
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
                        doMatchParameterizedKeyConfigs(parameterizedKeyConfigs, config.keyExpression)?.let { return@run it }
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
    
    private fun doMatchParameterizedKeyConfigs(pkConfigs: List<CwtValueConfig>?, configExpression: CwtDataExpression): Boolean? {
        //如果作为参数的键的规则类型可以（从扩展的CWT规则）推断出来且是匹配的，则需要继续向下匹配
        //目前要求推断结果必须是唯一的
        //目前不支持从使用推断 - 这可能会导致规则上下文的递归解析
        
        if(pkConfigs == null) return null
        if(pkConfigs.size != 1) return null //must be unique yet
        return CwtConfigManipulator.mergeAndMatchesValueConfig(pkConfigs, configExpression)
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
            } else if(lazyMatchedSize > 1) {
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
        
        val configGroup = configs.first().configGroup
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
            val trackers = buildList<Any> {
                this += ParadoxModificationTrackers.ScriptFileTracker
                this += ParadoxModificationTrackers.LocalisationFileTracker //for loc references
            }
            value.withDependencyItems(trackers)
        }
    }
    
    private fun doGetChildOccurrenceMapCache(): MutableMap<String, Map<CwtDataExpression, Occurrence>> {
        //use soft values to optimize memory
        return ContainerUtil.createConcurrentSoftValueMap()
    }
    
    private fun doGetChildOccurrenceMap(element: ParadoxScriptMemberElement, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if(configs.isEmpty()) return emptyMap()
        val configGroup = configs.first().configGroup
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
            val isParameterized = expression.type == ParadoxType.String && expression.value.isParameterized()
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
    fun getExpressionText(element: ParadoxExpressionElement, rangeInElement: TextRange? = null): String {
        return when {
            element is ParadoxScriptBlock -> "" //should not be used
            element is ParadoxScriptInlineMath -> "" //should not be used
            rangeInElement != null -> rangeInElement.substring(element.text)
            element is ParadoxScriptStringExpressionElement -> element.text.unquote()
            else -> element.text
        }
    }
    
    fun getExpressionTextRange(element: ParadoxExpressionElement): TextRange {
        return when {
            element is ParadoxScriptBlock -> TextRange.create(0, 1) //"{"
            element is ParadoxScriptInlineMath -> element.firstChild.textRangeInParent //"@[" or "@\["
            element is ParadoxScriptStringExpressionElement -> TextRange.create(0, element.text.length).unquote(element.text)
            else -> TextRange.create(0, element.text.length)
        }
    }
    
    fun getExpressionOffset(element: ParadoxExpressionElement): Int {
        return when {
            element is ParadoxScriptStringExpressionElement && element.text.isLeftQuoted() -> 1
            else -> 0
        }
    }
    
    fun getParameterRanges(element: ParadoxExpressionElement): List<TextRange> {
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
    
    fun getParameterRangesInExpression(element: ParadoxExpressionElement): List<TextRange> {
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
    
    private fun doAnnotateComplexExpression(element: ParadoxScriptStringExpressionElement, expressionNode: ParadoxComplexExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = expressionNode.getAttributesKey(element.language)
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
    
    private fun doAnnotateComplexExpressionByAttributesKey(expressionNode: ParadoxComplexExpressionNode, element: ParadoxScriptStringExpressionElement, holder: AnnotationHolder, attributesKey: TextAttributesKey) {
        val rangeToAnnotate = expressionNode.rangeInExpression.shiftRight(element.textRange.unquote(element.text).startOffset)
        annotateScriptExpression(element, rangeToAnnotate, attributesKey, holder)
    }
    //endregion
    
    //region Resolve Methods
    fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null): Array<out PsiReference>? {
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
    fun resolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        ProgressManager.checkCanceled()
        if(configExpression == null) return null
        
        val expression = getExpressionText(element, rangeInElement)
        if(expression.isParameterized()) return null //排除引用文本带参数的情况
        
        val result = ParadoxScriptExpressionSupport.resolve(element, rangeInElement, expression, config, isKey, exact)
        if(result != null) return result
        
        val configGroup = config.configGroup
        if(configExpression.isKey && configExpression.type in CwtDataTypeGroups.KeyReference) {
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
    
    fun multiResolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        if(configExpression == null) return emptySet()
        
        val expression = getExpressionText(element, rangeInElement)
        if(expression.isParameterized()) return emptySet() //排除引用文本带参数的情况
        
        val result = ParadoxScriptExpressionSupport.multiResolve(element, rangeInElement, expression, config, isKey)
        if(result.isNotEmpty()) return result
        
        val configGroup = config.configGroup
        if(configExpression.isKey && configExpression.type in CwtDataTypeGroups.KeyReference) {
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
    
    fun resolvePredefinedEnumValue(name: String, enumName: String, configGroup: CwtConfigGroup): PsiElement? {
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
        if(configExpression.isKey && expression.isKey == false) return false
        if(!configExpression.isKey && expression.isKey == true) return false
        
        if(configExpression.type == CwtDataTypes.Constant) return true
        if(configExpression.type == CwtDataTypes.EnumValue && configExpression.value?.let { configGroup.enums[it]?.values?.contains(expression.value) } == true) return true
        if(configExpression.type == CwtDataTypes.Value && configExpression.value?.let { configGroup.dynamicValueTypes[it]?.values?.contains(expression.value) } == true) return true
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
        return keys.find { CwtConfigMatcher.matches(element, expression, CwtDataExpression.resolve(it, true), null, configGroup, matchOptions).get(matchOptions) }
    }
    
    fun getAliasSubNames(element: PsiElement, key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, matchOptions: Int = Options.Default): List<String> {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) //不区分大小写
        if(constKey != null) return listOf(constKey)
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return emptyList()
        val expression = ParadoxDataExpression.resolve(key, quoted, true)
        return keys.filter { CwtConfigMatcher.matches(element, expression, CwtDataExpression.resolve(it, true), null, configGroup, matchOptions).get(matchOptions) }
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
        val configGroup = config.configGroup
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
            config is CwtInlineConfig -> {
                config.config.toSingletonListOrEmpty()
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
