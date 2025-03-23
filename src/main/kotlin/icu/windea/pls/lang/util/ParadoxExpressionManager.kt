package icu.windea.pls.lang.util

import com.intellij.lang.annotation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import com.intellij.util.containers.*
import com.intellij.util.text.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.ep.configContext.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.ResultValue
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.model.*
import icu.windea.pls.script.editor.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*
import kotlin.collections.isNullOrEmpty

object ParadoxExpressionManager {
    object Keys : KeyRegistry() {
        val cachedConfigContext by createKey<CachedValue<CwtConfigContext>>(Keys)
        val cachedConfigsCache by createKey<CachedValue<MutableMap<String, List<CwtMemberConfig<*>>>>>(Keys)
        val cachedChildOccurrenceMapCache by createKey<CachedValue<MutableMap<String, Map<CwtDataExpression, Occurrence>>>>(Keys)

        val inBlockKeys by createKey<Set<String>>(this)
    }

    //region Common Methods

    fun isParameterized(text: String, conditionBlock: Boolean = true, full: Boolean = false): Boolean {
        //快速判断，不检测带参数后的语法是否合法
        if (text.length < 2) return false
        if (full) {
            // $PARAM$ - 仅限 高级插值语法 A
            if (!text.startsWith('$')) return false
            if (text.indexOf('$', 1).let { c -> c != text.lastIndex || text.isEscapedCharAt(c) }) return false
            return true
        }
        // a_$PARAM$_b - 高级插值语法 A
        if (text.indexOf('$').let { c -> c != -1 && !text.isEscapedCharAt(c) }) return true
        // a_[[PARAM]b]_c - 高级插值语法 B
        if (conditionBlock && text.indexOf('[').let { c -> c != -1 && !text.isEscapedCharAt(c) }) return true
        return false
    }

    fun getParameterName(text: String): String? {
        //$PARAM$ - 仅限 高级插值语法 A
        if (!isParameterized(text, full = true)) return null
        return text.substring(1, text.length - 1).substringBefore('|')
    }

    fun getParameterRanges(text: String, conditionBlock: Boolean = true): List<TextRange> {
        //比较复杂的实现逻辑
        val ranges = mutableListOf<TextRange>()
        // a_$PARAM$_b - 高级插值语法 A - 深度计数
        var depth1 = 0
        // a_[[PARAM]b]_c - 高级插值语法 B - 深度计数
        var depth2 = 0
        var startIndex = -1
        var endIndex = -1
        for ((i, c) in text.withIndex()) {
            if (c == '$' && !text.isEscapedCharAt(i)) {
                if (depth2 > 0) continue
                if (depth1 == 0) {
                    startIndex = i
                    endIndex = -1
                    depth1++
                } else {
                    endIndex = i
                    ranges += TextRange.create(startIndex, endIndex + 1)
                    depth1--

                }
            } else if (conditionBlock && c == '[' && !text.isEscapedCharAt(i)) {
                if (depth1 > 0) continue
                if (depth2 == 0) {
                    startIndex = i
                    endIndex = -1
                }
                depth2++
            } else if (conditionBlock && c == ']' && !text.isEscapedCharAt(i)) {
                if (depth1 > 0) continue
                if (depth2 <= 0) continue
                depth2--
                if (depth2 == 0) {
                    endIndex = i
                    ranges += TextRange.create(startIndex, endIndex + 1)
                }
            }
        }
        if (startIndex != -1 && endIndex == -1) {
            ranges += TextRange.create(startIndex, text.length)
        }
        return ranges
    }

    private val regex1 = """(?<!\\)\$.*?\$""".toRegex()
    private val regex2 = """(?<!\\)\[\[.*?](.*?)]""".toRegex()

    fun toRegex(text: String, conditionBlock: Boolean = true): Regex {
        var s = text
        s = """\Q$s\E"""
        s = s.replace(regex1, """\\E.*\\Q""")
        if (conditionBlock) {
            s = s.replace(regex2) { g ->
                val dv = g.groupValues[1]
                when {
                    dv == """\E.*\Q""" -> """\E.*\Q"""
                    else -> """\E(?:\Q$dv\E)?\Q"""
                }
            }
        }
        s = s.replace("""\Q\E""", "")
        return s.toRegex(RegexOption.IGNORE_CASE)
    }

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

    fun getParameterRangesInExpression(element: ParadoxExpressionElement): List<TextRange> {
        return doGetParameterRangesInExpressionFromCache(element)
    }

    private fun doGetParameterRangesInExpressionFromCache(element: ParadoxExpressionElement): List<TextRange> {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedParameterRanges) {
            val value = doGetParameterRangesInExpression(element)
            value.withDependencyItems(element)
        }
    }

    private fun doGetParameterRangesInExpression(element: ParadoxExpressionElement): List<TextRange> {
        var parameterRanges: MutableList<TextRange>? = null
        element.processChild { e ->
            if (isParameterElementInExpression(e)) {
                if (parameterRanges == null) parameterRanges = mutableListOf()
                parameterRanges?.add(e.textRange)
            }
            true
        }
        return parameterRanges.orEmpty()
    }

    fun isParameterElementInExpression(element: PsiElement): Boolean {
        return element is ParadoxParameter || element is ParadoxScriptInlineParameterCondition
            || element is ParadoxLocalisationPropertyReference
    }

    fun isUnaryOperatorAwareParameter(text: String, parameterRanges: List<TextRange>): Boolean {
        return text.firstOrNull()?.let { it == '+' || it == '-' } == true
            && parameterRanges.singleOrNull()?.let { it.startOffset == 1 && it.endOffset == text.length } == true
    }

    //endregion

    //region Core Methods

    fun getConfigContext(element: PsiElement): CwtConfigContext? {
        ProgressManager.checkCanceled()
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return null
        return doGetConfigContextFromCache(memberElement)
    }

    private fun doGetConfigContextFromCache(element: ParadoxScriptMemberElement): CwtConfigContext? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigContext) {
            val value = doGetConfigContext(element)
            //also depends on localisation files (for loc references)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetConfigContext(element: ParadoxScriptMemberElement): CwtConfigContext? {
        return CwtConfigContextProvider.getContext(element)
    }

    fun getConfigsForConfigContext(
        element: ParadoxScriptMemberElement,
        rootConfigs: List<CwtMemberConfig<*>>,
        elementPathFromRoot: ParadoxExpressionPath,
        configGroup: CwtConfigGroup,
        matchOptions: Int = Options.Default
    ): List<CwtMemberConfig<*>> {
        val result = doGetConfigsForConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        return result.sortedByPriority({ it.expression }, { it.configGroup })
    }

    private fun doGetConfigsForConfigContext(
        element: ParadoxScriptMemberElement,
        rootConfigs: List<CwtMemberConfig<*>>,
        elementPathFromRoot: ParadoxExpressionPath,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): List<CwtMemberConfig<*>> {
        val isPropertyValue = element is ParadoxScriptValue && element.isPropertyValue()

        var result: List<CwtMemberConfig<*>> = rootConfigs

        val subPaths = elementPathFromRoot.subPaths
        val originalSubPaths = elementPathFromRoot.originalSubPaths
        subPaths.forEachIndexed f1@{ i, subPath ->
            ProgressManager.checkCanceled()

            //如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
            //如果整个过程中的某个key匹配内联规则的名字（如，inline_script），则需要内联此内联规则

            val isQuoted = subPath != originalSubPaths[i]
            val isParameterized = subPath.isParameterized()
            val isFullParameterized = subPath.isParameterized(full = true)
            val shift = subPaths.lastIndex - i
            val matchesKey = isPropertyValue || shift > 0
            val expression = ParadoxDataExpression.resolve(subPath, isQuoted, true)
            val nextResult = mutableListOf<CwtMemberConfig<*>>()

            val parameterizedKeyConfigs by lazy {
                if (!isParameterized) return@lazy null
                if (!isFullParameterized) return@lazy emptyList() //must be full parameterized yet
                ParadoxParameterManager.getParameterizedKeyConfigs(element, shift)
            }

            run r1@{
                result.forEach f2@{ parentConfig ->
                    val configs = parentConfig.configs
                    if (configs.isNullOrEmpty()) return@f2

                    val exactMatchedConfigs = mutableListOf<CwtMemberConfig<*>>()
                    val relaxMatchedConfigs = mutableListOf<CwtMemberConfig<*>>()

                    fun addToMatchedConfigs(config: CwtMemberConfig<*>) {
                        if (config is CwtPropertyConfig) {
                            val m = doMatchParameterizedKeyConfigs(parameterizedKeyConfigs, config.keyExpression)
                            when (m) {
                                null -> nextResult += config
                                true -> exactMatchedConfigs += config
                                false -> relaxMatchedConfigs += config
                            }
                        } else if (config is CwtValueConfig) {
                            nextResult += config
                        }
                    }

                    fun collectMatchedConfigs() {
                        if (exactMatchedConfigs.isNotEmpty()) {
                            nextResult += exactMatchedConfigs
                        } else if (relaxMatchedConfigs.size == 1) {
                            nextResult += relaxMatchedConfigs
                        }
                    }

                    configs.forEach f3@{ config ->
                        if (config is CwtPropertyConfig) {
                            if (subPath == "-") return@f3
                            if (matchesKey) {
                                val matchResult = ParadoxExpressionMatcher.matches(element, expression, config.keyExpression, config, configGroup, matchOptions)
                                if (!matchResult.get(matchOptions)) return@f3
                            }
                            val inlinedConfigs = doInlineConfigForConfigContext(element, subPath, isQuoted, config, matchOptions)
                            if (inlinedConfigs.isEmpty()) {
                                addToMatchedConfigs(config)
                            } else {
                                inlinedConfigs.forEach { inlinedConfig -> addToMatchedConfigs(inlinedConfig) }
                            }
                        } else if (config is CwtValueConfig) {
                            if (subPath != "-") return@f3
                            addToMatchedConfigs(config)
                        }
                    }

                    collectMatchedConfigs()
                }
            }

            result = nextResult

            run r1@{
                if (!matchesKey) return@r1
                val pathToMatch = ParadoxExpressionPath.resolve(originalSubPaths.dropLast(shift))
                ProgressManager.checkCanceled()
                val elementToMatch = element.findParentByPath(pathToMatch.path)
                if (elementToMatch == null) return@r1
                val resultValuesMatchKey = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
                result.forEach f@{ config ->
                    val matchResult = ParadoxExpressionMatcher.matches(elementToMatch, expression, config.expression, config, configGroup, matchOptions)
                    if (matchResult == ParadoxExpressionMatcher.Result.NotMatch) return@f
                    resultValuesMatchKey += ResultValue(config, matchResult)
                }
                val optimizedResult = optimizeMatchedConfigs(elementToMatch, expression, resultValuesMatchKey, true, matchOptions)
                result = optimizedResult
            }
        }

        if (isPropertyValue) {
            result = result.mapNotNull { if (it is CwtPropertyConfig) it.valueConfig else null }
        }

        return result
    }

    private fun doInlineConfigForConfigContext(
        element: ParadoxScriptMemberElement,
        key: String,
        isQuoted: Boolean,
        config: CwtPropertyConfig,
        matchOptions: Int
    ): List<CwtMemberConfig<*>> {
        val configGroup = config.configGroup
        val result = mutableListOf<CwtMemberConfig<*>>()
        run {
            val configValueExpression = config.valueExpression
            if (configValueExpression.type == CwtDataTypes.SingleAliasRight) {
                result += CwtConfigManipulator.inlineSingleAlias(config) ?: return@run
            } else if (configValueExpression.type == CwtDataTypes.AliasMatchLeft) {
                val aliasName = configValueExpression.value ?: return@run
                val aliasGroup = configGroup.aliasGroups[aliasName] ?: return@run
                val aliasSubNames = getAliasSubNames(element, key, isQuoted, aliasName, configGroup, matchOptions)
                aliasSubNames.forEach f1@{ aliasSubName ->
                    val aliasConfigs = aliasGroup[aliasSubName] ?: return@f1
                    aliasConfigs.forEach f2@{ aliasConfig ->
                        val aliasConfigInlined = aliasConfig.inline(config)
                        if (aliasConfigInlined.valueExpression.type == CwtDataTypes.SingleAliasRight) {
                            result += CwtConfigManipulator.inlineSingleAlias(aliasConfigInlined) ?: return@f2
                        } else {
                            result += aliasConfigInlined
                        }
                    }
                }
            }
        }
        if (result.isEmpty()) return emptyList()
        val parentConfig = config.parentConfig
        if (parentConfig != null) CwtInjectedConfigProvider.injectConfigs(parentConfig, result)
        return result
    }

    fun getConfigs(
        element: PsiElement,
        orDefault: Boolean = true,
        matchOptions: Int = Options.Default
    ): List<CwtMemberConfig<*>> {
        ProgressManager.checkCanceled()
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return emptyList()
        val configsMap = doGetConfigsCacheFromCache(memberElement)
        val cacheKey = buildString {
            append('#').append(orDefault.toInt())
            append('#').append(matchOptions)
        }
        return configsMap.getOrPut(cacheKey) {
            val result = doGetConfigs(memberElement, orDefault, matchOptions).optimized()
            result.sortedByPriority({ it.expression }, { it.configGroup })
        }
    }

    private fun doGetConfigsCacheFromCache(element: PsiElement): MutableMap<String, List<CwtMemberConfig<*>>> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigsCache) {
            val value = doGetConfigsCache()
            //also depends on localisation files (for loc references)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
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
        if (isDefinition && element is ParadoxScriptDefinitionElement && !BitUtil.isSet(matchOptions, Options.AcceptDefinition)) return emptyList()
        val configGroup = configContext.configGroup

        ProgressManager.checkCanceled()
        val contextConfigs = configContext.getConfigs(matchOptions)
        if (contextConfigs.isEmpty()) return emptyList()

        //匹配键
        val resultMatchKey = when {
            keyExpression != null -> {
                val resultValuesMatchKey = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
                contextConfigs.forEach f@{ config ->
                    if (config !is CwtPropertyConfig) return@f
                    val matchResult = ParadoxExpressionMatcher.matches(element, keyExpression, config.keyExpression, config, configGroup, matchOptions)
                    if (matchResult == ParadoxExpressionMatcher.Result.NotMatch) return@f
                    resultValuesMatchKey += ResultValue(config, matchResult)
                }
                optimizeMatchedConfigs(element, keyExpression, resultValuesMatchKey, true, matchOptions)
            }
            else -> contextConfigs.filterIsInstance<CwtValueConfig>()
        }
        if (resultMatchKey.isEmpty()) return emptyList()

        //如果无法获取valueExpression，则返回所有匹配键的规则
        if (valueExpression == null) return resultMatchKey

        //得到所有可能匹配的结果
        ProgressManager.checkCanceled()
        val resultValues = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
        resultMatchKey.forEach f@{ config ->
            val matchResult = ParadoxExpressionMatcher.matches(element, valueExpression, config.valueExpression, config, configGroup, matchOptions)
            if (matchResult == ParadoxExpressionMatcher.Result.NotMatch) return@f
            resultValues += ResultValue(config, matchResult)
        }
        //如果无结果且需要使用默认值，则返回所有可能匹配的规则
        if (resultValues.isEmpty() && orDefault) return resultMatchKey

        //优化匹配结果
        ProgressManager.checkCanceled()
        val optimizedResult = optimizeMatchedConfigs(element, valueExpression, resultValues, false, matchOptions)
        //如果仍然无结果且需要使用默认值，则返回所有可能匹配的规则
        if (optimizedResult.isEmpty() && orDefault) return resultMatchKey
        return optimizedResult
    }

    private fun doMatchParameterizedKeyConfigs(pkConfigs: List<CwtValueConfig>?, configExpression: CwtDataExpression): Boolean? {
        //如果作为参数的键的规则类型可以（从扩展的CWT规则）推断出来且是匹配的，则需要继续向下匹配
        //目前要求推断结果必须是唯一的
        //目前不支持从参数的使用处推断 - 这可能会导致规则上下文的递归解析

        if (pkConfigs == null) return null //不是作为参数的键，不作特殊处理
        if (pkConfigs.size != 1) return false //推断结果不是唯一的，要求后续宽松匹配的结果是唯一的，否则认为没有最终匹配的结果
        return CwtConfigManipulator.mergeAndMatchValueConfig(pkConfigs, configExpression)
    }

    fun optimizeMatchedConfigs(
        element: PsiElement,
        expression: ParadoxDataExpression,
        resultValues: List<ResultValue<CwtMemberConfig<*>>>,
        postHandle: Boolean,
        matchOptions: Int = Options.Default
    ): List<CwtMemberConfig<*>> {
        if (resultValues.isEmpty()) return emptyList()

        val configGroup = resultValues.first().value.configGroup

        //首先尝试直接的精确匹配，如果有结果，则直接返回
        //然后，尝试需要检测子句的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        //然后，尝试需要检测作用域上下文的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        //然后，尝试非回退的匹配，如果有结果，则直接返回
        //然后，尝试复杂表达式的回退的匹配（可以解析，但存在错误），如果有结果，则直接返回
        //然后，尝试回退的匹配，如果有结果，则直接返回
        //如果到这里仍然无法匹配，则直接返回空列表

        val result = run r1@{
            val exactMatched = resultValues.filter { it.result is ParadoxExpressionMatcher.Result.ExactMatch }
            if (exactMatched.isNotEmpty()) return@r1 exactMatched.map { it.value }

            val matched = mutableListOf<ResultValue<CwtMemberConfig<*>>>()

            fun addLazyMatchedConfigs(predicate: (ResultValue<CwtMemberConfig<*>>) -> Boolean) {
                val lazyMatched = resultValues.filter(predicate)
                val lazyMatchedSize = lazyMatched.size
                if (lazyMatchedSize == 1) {
                    matched += lazyMatched.first()
                } else if (lazyMatchedSize > 1) {
                    val oldMatchedSize = matched.size
                    lazyMatched.filterTo(matched) { it.result.get(matchOptions) }
                    if (oldMatchedSize == matched.size) matched += lazyMatched.first()
                }
            }

            addLazyMatchedConfigs { it.result is ParadoxExpressionMatcher.Result.LazyBlockAwareMatch }
            addLazyMatchedConfigs { it.result is ParadoxExpressionMatcher.Result.LazyScopeAwareMatch }

            resultValues.filterTo(matched) p@{
                if (it.result is ParadoxExpressionMatcher.Result.LazyBlockAwareMatch) return@p false //已经匹配过
                if (it.result is ParadoxExpressionMatcher.Result.LazyScopeAwareMatch) return@p false //已经匹配过
                if (it.result is ParadoxExpressionMatcher.Result.LazySimpleMatch) return@p true //直接认为是匹配的
                if (it.result is ParadoxExpressionMatcher.Result.PartialMatch) return@p false //之后再匹配
                if (it.result is ParadoxExpressionMatcher.Result.FallbackMatch) return@p false //之后再匹配
                it.result.get(matchOptions)
            }
            if (matched.isNotEmpty()) return@r1 matched.map { it.value }

            resultValues.filterTo(matched) { it.result is ParadoxExpressionMatcher.Result.PartialMatch }
            if (matched.isNotEmpty()) return@r1 matched.map { it.value }

            resultValues.filterTo(matched) { it.result is ParadoxExpressionMatcher.Result.FallbackMatch }
            if (matched.isNotEmpty()) return@r1 matched.map { it.value }

            emptyList()
        }
        if (result.isEmpty() || !postHandle) return result.optimized()

        var newResult = result

        //后续处理

        //如果要匹配的是字符串，且匹配结果中存在作为常量匹配的规则，则仅保留这些规则
        run r1@{
            if (newResult.size <= 1) return@r1
            if (expression.type != ParadoxType.String) return@r1
            val result1 = newResult.filter { isConstantMatch(expression, it.expression, configGroup) }
            if (result1.isEmpty()) return@r1
            newResult = result1
        }

        //如果匹配结果中存在键相同的规则，且其值是子句，则尝试根据子句进行进一步的匹配
        run r1@{
            if (newResult.isEmpty()) return@r1
            val blockElement = element.castOrNull<ParadoxScriptProperty>()?.block ?: return@r1
            val blockExpression = ParadoxDataExpression.BlockExpression
            val configsToRemove = mutableSetOf<CwtPropertyConfig>()
            val group: Collection<List<CwtPropertyConfig>> = newResult.filterIsInstance<CwtPropertyConfig>().groupBy { it.key }.values
            group.forEach f1@{ configs ->
                if (configs.size <= 1) return@f1
                val configs1 = configs.filter { it.isBlock }
                if (configs1.size <= 1) return@r1
                configs.forEach f2@{ config ->
                    val valueConfig = config.valueConfig ?: return@f2
                    val matchResult = ParadoxExpressionMatcher.matches(blockElement, blockExpression, valueConfig.expression, valueConfig, configGroup, matchOptions)
                    if (matchResult.get(matchOptions)) return@f2
                    configsToRemove += config
                }
            }
            if (configsToRemove.isEmpty()) return@r1
            val result1 = newResult.filter { it !in configsToRemove }
            newResult = result1
        }

        //如果结果不为空且结果中存在需要重载的规则，则全部替换成重载后的规则
        run r1@{
            if (newResult.isEmpty()) return@r1
            val result1 = mutableListOf<CwtMemberConfig<*>>()
            newResult.forEach f1@{ config ->
                val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(element, config)
                if (overriddenConfigs.isNullOrEmpty()) {
                    result1 += config
                    return@f1
                }
                //这里需要再次进行匹配
                overriddenConfigs.forEach { c ->
                    val matchResult = ParadoxExpressionMatcher.matches(element, expression, c.expression, c, configGroup, matchOptions)
                    if (matchResult.get(matchOptions)) {
                        result1 += c
                    }
                }
            }
            newResult = result1
        }

        return newResult.optimized()
    }

    //兼容需要考虑内联的情况（如内联脚本）
    //这里需要兼容匹配key的子句规则有多个的情况 - 匹配任意则使用匹配的首个规则，空子句或者都不匹配则使用合并的规则

    /**
     * 得到指定的[element]的作为值的子句中的子属性/值的出现次数信息。（先合并子规则）
     */
    fun getChildOccurrenceMap(element: ParadoxScriptMemberElement, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if (configs.isEmpty()) return emptyMap()
        val childConfigs = configs.flatMap { it.configs.orEmpty() }
        if (childConfigs.isEmpty()) return emptyMap()

        ProgressManager.checkCanceled()
        val childOccurrenceMap = doGetChildOccurrenceMapCacheFromCache(element) ?: return emptyMap()
        //NOTE cacheKey基于childConfigs即可，key相同而value不同的规则，上面的cardinality应当保证是一样的
        val cacheKey = childConfigs.joinToString(" ")
        return childOccurrenceMap.getOrPut(cacheKey) { doGetChildOccurrenceMap(element, configs).optimized() }
    }

    private fun doGetChildOccurrenceMapCacheFromCache(element: ParadoxScriptMemberElement): MutableMap<String, Map<CwtDataExpression, Occurrence>>? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedChildOccurrenceMapCache) {
            val value = doGetChildOccurrenceMapCache()
            //also depends on localisation files (for loc references)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetChildOccurrenceMapCache(): MutableMap<String, Map<CwtDataExpression, Occurrence>> {
        //use soft values to optimize memory
        return ContainerUtil.createConcurrentSoftValueMap()
    }

    private fun doGetChildOccurrenceMap(element: ParadoxScriptMemberElement, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if (configs.isEmpty()) return emptyMap()
        val configGroup = configs.first().configGroup
        //这里需要先按优先级排序
        val childConfigs = configs.flatMap { it.configs.orEmpty() }.sortedByPriority({ it.expression }, { configGroup })
        if (childConfigs.isEmpty()) return emptyMap()
        val project = configGroup.project
        val blockElement = when {
            element is ParadoxScriptDefinitionElement -> element.block
            element is ParadoxScriptBlockElement -> element
            else -> null
        }
        if (blockElement == null) return emptyMap()
        val occurrenceMap = mutableMapOf<CwtDataExpression, Occurrence>()
        for (childConfig in childConfigs) {
            occurrenceMap.put(childConfig.expression, childConfig.toOccurrence(element, project))
        }
        ProgressManager.checkCanceled()
        //注意这里需要考虑内联和可选的情况
        blockElement.processMember(conditional = true, inline = true) p@{ data ->
            val expression = when {
                data is ParadoxScriptProperty -> ParadoxDataExpression.resolve(data.propertyKey)
                data is ParadoxScriptValue -> ParadoxDataExpression.resolve(data)
                else -> return@p true
            }
            val isParameterized = expression.type == ParadoxType.String && expression.value.isParameterized()
            //may contain parameter -> can't and should not get occurrences
            if (isParameterized) {
                occurrenceMap.clear()
                return@p true
            }
            val matched = childConfigs.find { childConfig ->
                if (childConfig is CwtPropertyConfig && data !is ParadoxScriptProperty) return@find false
                if (childConfig is CwtValueConfig && data !is ParadoxScriptValue) return@find false
                ParadoxExpressionMatcher.matches(data, expression, childConfig.expression, childConfig, configGroup).get()
            }
            if (matched == null) return@p true
            val occurrence = occurrenceMap[matched.expression]
            if (occurrence == null) return@p true
            occurrence.actual += 1
            true
        }
        return occurrenceMap
    }

    //endregion

    //region Annotate Methods

    fun annotateExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, holder: AnnotationHolder) {
        val expressionText = getExpressionText(element, rangeInElement)
        ParadoxScriptExpressionSupport.annotate(element, rangeInElement, expressionText, holder, config)
    }

    fun annotateExpression(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder) {
        val expressionText = getExpressionText(element, rangeInElement)
        ParadoxLocalisationExpressionSupport.annotate(element, rangeInElement, expressionText, holder)
    }

    fun annotateComplexExpression(element: ParadoxExpressionElement, expression: ParadoxComplexExpression, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        doAnnotateComplexExpression(element, expression, holder, config)
    }

    private fun doAnnotateComplexExpression(element: ParadoxExpressionElement, expressionNode: ParadoxComplexExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        val attributesKey = expressionNode.getAttributesKey(element)

        run {
            val mustUseAttributesKey = attributesKey != ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY && attributesKey != ParadoxScriptAttributesKeys.STRING_KEY
            if (attributesKey != null && mustUseAttributesKey) {
                annotateExpressionNodeByAttributesKey(element, expressionNode, attributesKey, holder)
                return@run
            }
            if (element is ParadoxScriptStringExpressionElement) {
                val attributesKeyConfig = expressionNode.getAttributesKeyConfig(element)
                if (attributesKeyConfig != null) {
                    val rangeInElement = expressionNode.rangeInExpression.shiftRight(if (element.text.isLeftQuoted()) 1 else 0)
                    annotateExpression(element, rangeInElement, attributesKeyConfig, holder)
                    return@run
                }
            }
            if (attributesKey != null) {
                annotateExpressionNodeByAttributesKey(element, expressionNode, attributesKey, holder)
            }
        }

        if (expressionNode.nodes.isNotEmpty()) {
            for (node in expressionNode.nodes) {
                doAnnotateComplexExpression(element, node, holder, config)
            }
        }
    }

    fun annotateExpressionByAttributesKey(element: ParadoxExpressionElement, range: TextRange, attributesKey: TextAttributesKey, holder: AnnotationHolder) {
        if (range.isEmpty) return
        //skip parameter ranges
        val parameterRanges = getParameterRangesInExpression(element)
        if (parameterRanges.isEmpty()) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
            return
        }
        val finalRanges = TextRangeUtil.excludeRanges(range, parameterRanges)
        for (r in finalRanges) {
            if (r.isEmpty) continue
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(r).textAttributes(attributesKey).create()
        }
    }

    private fun annotateExpressionNodeByAttributesKey(element: ParadoxExpressionElement, expressionNode: ParadoxComplexExpressionNode, attributesKey: TextAttributesKey, holder: AnnotationHolder) {
        val rangeToAnnotate = expressionNode.rangeInExpression.shiftRight(element.textRange.unquote(element.text).startOffset)

        //merge text attributes from HighlighterColors.TEXT and attributesKey for token nodes (in case foreground is not set)
        if (expressionNode is ParadoxTokenNode) {
            val schema = EditorColorsManager.getInstance().schemeForCurrentUITheme
            val textAttributes1 = schema.getAttributes(HighlighterColors.TEXT)
            val textAttributes2 = schema.getAttributes(attributesKey)
            val textAttributes = TextAttributes.merge(textAttributes1, textAttributes2)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(rangeToAnnotate).enforcedTextAttributes(textAttributes).create()
            return
        }

        annotateExpressionByAttributesKey(element, rangeToAnnotate, attributesKey, holder)
    }

    //endregion

    //region Reference Methods

    fun getExpressionReferences(element: ParadoxExpressionElement): Array<out PsiReference> {
        ProgressManager.checkCanceled()
        return when (element) {
            is ParadoxScriptExpressionElement -> doGetExpressionReferencesFromCache(element)
            is ParadoxLocalisationExpressionElement -> doGetExpressionReferencesFromCache(element)
            else -> PsiReference.EMPTY_ARRAY
        }
    }

    private fun doGetExpressionReferencesFromCache(element: ParadoxScriptExpressionElement): Array<out PsiReference> {
        if (!element.isExpression()) return PsiReference.EMPTY_ARRAY

        //尝试兼容可能包含参数的情况
        //if(element.text.isParameterized()) return PsiReference.EMPTY_ARRAY

        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedLocalisationExpressionReferences) {
            val value = doGetExpressionReferences(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetExpressionReferences(element: ParadoxScriptExpressionElement): Array<out PsiReference> {
        //尝试基于CWT规则进行解析
        val isKey = element is ParadoxScriptPropertyKey
        val configs = getConfigs(element, orDefault = isKey)
        val config = configs.firstOrNull() ?: return PsiReference.EMPTY_ARRAY
        val textRange = getExpressionTextRange(element) //unquoted text
        val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
        return reference.collectReferences()
    }

    private fun doGetExpressionReferencesFromCache(element: ParadoxLocalisationExpressionElement): Array<out PsiReference> {
        if (!element.isComplexExpression()) return PsiReference.EMPTY_ARRAY

        //尝试兼容可能包含参数的情况
        //if(text.isParameterized()) return PsiReference.EMPTY_ARRAY

        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedLocalisationExpressionReferences) {
            val value = doGetExpressionReferences(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetExpressionReferences(element: ParadoxLocalisationExpressionElement): Array<out PsiReference> {
        //尝试解析为复杂表达式
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        val reference = ParadoxLocalisationExpressionPsiReference(element, textRange)
        return reference.collectReferences()
    }


    fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null): Array<out PsiReference>? {
        ProgressManager.checkCanceled()
        if (configExpression == null) return null
        val expressionText = getExpressionText(element, rangeInElement)

        val result = ParadoxScriptExpressionSupport.getReferences(element, rangeInElement, expressionText, config, isKey)
        return result.orNull()
    }

    fun getReferences(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?): Array<out PsiReference>? {
        ProgressManager.checkCanceled()
        val expressionText = getExpressionText(element, rangeInElement)

        val result = ParadoxLocalisationExpressionSupport.getReferences(element, rangeInElement, expressionText)
        return result.orNull()
    }

    //endregion

    //region Resolve Methods

    fun resolveExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        ProgressManager.checkCanceled()
        if (configExpression == null) return null
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return null //排除引用文本带参数的情况

        val result = ParadoxScriptExpressionSupport.resolve(element, rangeInElement, expressionText, config, isKey, exact)
        if (result != null) return result

        val configGroup = config.configGroup
        if (configExpression.isKey && configExpression.type in CwtDataTypeGroups.KeyReference) {
            return getResolvedConfigElement(element, config, configGroup)
        }
        return null
    }

    fun multiResolveExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        if (configExpression == null) return emptySet()
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return emptySet() //排除引用文本带参数的情况

        val result = ParadoxScriptExpressionSupport.multiResolve(element, rangeInElement, expressionText, config, isKey)
        if (result.isNotEmpty()) return result

        val configGroup = config.configGroup
        if (configExpression.isKey && configExpression.type in CwtDataTypeGroups.KeyReference) {
            return getResolvedConfigElement(element, config, configGroup).toSingletonSetOrEmpty()
        }
        return emptySet()
    }

    private fun getResolvedConfigElement(element: ParadoxScriptExpressionElement, config: CwtConfig<*>, configGroup: CwtConfigGroup): PsiElement? {
        val resolvedConfig = config.resolved()
        if (resolvedConfig is CwtMemberConfig<*> && resolvedConfig.pointer.isEmpty()) {
            //特殊处理合成的CWT规则
            val gameType = configGroup.gameType ?: return null
            val project = configGroup.project
            return CwtMemberConfigElement(element, resolvedConfig, gameType, project)
        }
        return resolvedConfig.pointer.element
    }

    fun resolveExpression(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?): PsiElement? {
        ProgressManager.checkCanceled()
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return null //排除引用文本带参数的情况

        val result = ParadoxLocalisationExpressionSupport.resolve(element, rangeInElement, expressionText)
        return result
    }

    fun multiResolveExpression(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return emptySet() //排除引用文本带参数的情况

        val result = ParadoxLocalisationExpressionSupport.multiResolve(element, rangeInElement, expressionText)
        return result
    }

    fun resolveModifier(element: ParadoxScriptExpressionElement, name: String, configGroup: CwtConfigGroup): PsiElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null
        return ParadoxModifierManager.resolveModifier(name, element, configGroup)
    }

    fun resolveTemplateExpression(element: ParadoxScriptExpressionElement, text: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null
        val templateConfigExpression = CwtTemplateExpression.resolve(configExpression.expressionString)
        return CwtTemplateExpressionManager.resolve(text, element, templateConfigExpression, configGroup)
    }

    fun resolvePredefinedScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val systemScopeConfig = configGroup.systemScopes[name] ?: return null
        val resolved = systemScopeConfig.pointer.element?.bindConfig(systemScopeConfig) ?: return null
        return resolved
    }

    fun resolveScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.links[name]?.takeIf { it.forScope() && !it.fromData } ?: return null
        val resolved = linkConfig.pointer.element?.bindConfig(linkConfig) ?: return null
        return resolved
    }

    fun resolveValueField(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.links[name]?.takeIf { it.forValue() && !it.fromData } ?: return null
        val resolved = linkConfig.pointer.element?.bindConfig(linkConfig) ?: return null
        return resolved
    }

    fun resolvePredefinedEnumValue(name: String, enumName: String, configGroup: CwtConfigGroup): PsiElement? {
        val enumConfig = configGroup.enums[enumName] ?: return null
        val enumValueConfig = enumConfig.valueConfigMap.get(name) ?: return null
        val resolved = enumValueConfig.pointer.element?.bindConfig(enumValueConfig) ?: return null
        return resolved
    }

    fun resolvePredefinedLocalisationScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.localisationLinks[name] ?: return null
        val resolved = linkConfig.pointer.element?.bindConfig(linkConfig) ?: return null
        return resolved
    }

    fun resolvePredefinedLocalisationCommand(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val commandConfig = configGroup.localisationCommands[name] ?: return null
        val resolved = commandConfig.pointer.element?.bindConfig(commandConfig) ?: return null
        return resolved
    }

    //endregion

    //region Misc Methods

    fun isConstantMatch(expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        //注意这里可能需要在同一循环中同时检查keyExpression和valueExpression，因此这里需要特殊处理
        if (configExpression.isKey && expression.isKey == false) return false
        if (!configExpression.isKey && expression.isKey == true) return false

        if (configExpression.type == CwtDataTypes.Constant) return true
        if (configExpression.type == CwtDataTypes.EnumValue && configExpression.value?.let { configGroup.enums[it]?.values?.contains(expression.value) } == true) return true
        if (configExpression.type == CwtDataTypes.Value && configExpression.value?.let { configGroup.dynamicValueTypes[it]?.values?.contains(expression.value) } == true) return true
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
        if (constKey != null) return constKey
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return null
        val expression = ParadoxDataExpression.resolve(key, quoted, true)
        return keys.find { ParadoxExpressionMatcher.matches(element, expression, CwtDataExpression.resolve(it, true), null, configGroup, matchOptions).get(matchOptions) }
    }

    fun getAliasSubNames(element: PsiElement, key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, matchOptions: Int = Options.Default): List<String> {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) //不区分大小写
        if (constKey != null) return listOf(constKey)
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return emptyList()
        val expression = ParadoxDataExpression.resolve(key, quoted, true)
        return keys.filter { ParadoxExpressionMatcher.matches(element, expression, CwtDataExpression.resolve(it, true), null, configGroup, matchOptions).get(matchOptions) }
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
                config.inlineConfig?.let { return getEntryConfigs(it) }
                config.aliasConfig?.let { return getEntryConfigs(it) }
                config.singleAliasConfig?.let { return getEntryConfigs(it) }
                config.parentConfig?.configs?.filter { it is CwtPropertyConfig && it.key == config.key }?.let { return it }
                config.toSingletonList()
            }
            config is CwtValueConfig -> {
                config.propertyConfig?.let { return getEntryConfigs(it) }
                config.parentConfig?.configs?.filterIsInstance<CwtValueConfig>()?.let { return it }
                config.toSingletonList()
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

    fun getInBlockKeys(config: CwtMemberConfig<*>): Set<String> {
        return config.getOrPutUserData(Keys.inBlockKeys) {
            val keys = caseInsensitiveStringSet()
            config.configs?.forEach {
                if (it is CwtPropertyConfig && isInBlockKey(it)) {
                    keys.add(it.key)
                }
            }
            when {
                config is CwtPropertyConfig -> {
                    val propertyConfig = config
                    propertyConfig.parentConfig?.configs?.forEach { c ->
                        if (c is CwtPropertyConfig && c.key.equals(propertyConfig.key, true) && c.pointer != propertyConfig.pointer) {
                            c.configs?.forEach { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                        }
                    }
                }
                config is CwtValueConfig -> {
                    val propertyConfig = config.propertyConfig
                    propertyConfig?.parentConfig?.configs?.forEach { c ->
                        if (c is CwtPropertyConfig && c.key.equals(propertyConfig.key, true) && c.pointer != propertyConfig.pointer) {
                            c.configs?.forEach { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
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
