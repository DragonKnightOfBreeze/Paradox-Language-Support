@file:Suppress("unused")

package icu.windea.pls.lang.util

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.startOffset
import com.intellij.util.BitUtil
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.text.TextRangeUtil
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.bindConfig
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtInlineConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.config.toOccurrence
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configContext.isDefinition
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.aliasGroups
import icu.windea.pls.config.configGroup.aliasKeysGroupConst
import icu.windea.pls.config.configGroup.aliasKeysGroupNoConst
import icu.windea.pls.config.configGroup.dynamicValueTypes
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.config.configGroup.links
import icu.windea.pls.config.configGroup.localisationCommands
import icu.windea.pls.config.configGroup.localisationLinks
import icu.windea.pls.config.configGroup.systemScopes
import icu.windea.pls.config.resolved
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collectReferences
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.isEmpty
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.processChild
import icu.windea.pls.core.toInt
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.listOrEmpty
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.ep.config.CwtOverriddenConfigProvider
import icu.windea.pls.ep.configContext.CwtConfigContextProvider
import icu.windea.pls.ep.expression.ParadoxCsvExpressionSupport
import icu.windea.pls.ep.expression.ParadoxLocalisationExpressionSupport
import icu.windea.pls.ep.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.ep.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.isInlineScriptUsage
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.mock.CwtMemberConfigElement
import icu.windea.pls.lang.references.csv.ParadoxCsvExpressionPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationExpressionPsiReference
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTokenNode
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.ResultValue
import icu.windea.pls.lang.util.dataFlow.options
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.Occurrence
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInlineParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.findParentByPath
import icu.windea.pls.script.psi.isExpression
import icu.windea.pls.script.psi.isPropertyValue
import icu.windea.pls.script.psi.members

object ParadoxExpressionManager {
    object Keys : KeyRegistry() {
        val cachedParameterRanges by createKey<CachedValue<List<TextRange>>>(Keys)

        val cachedConfigContext by createKey<CachedValue<CwtConfigContext>>(Keys)
        val cachedConfigsCache by createKey<CachedValue<MutableMap<String, List<CwtMemberConfig<*>>>>>(Keys)
        val cachedChildOccurrenceMapCache by createKey<CachedValue<MutableMap<String, Map<CwtDataExpression, Occurrence>>>>(Keys)

        val cachedExpressionReferences by createKey<CachedValue<Array<out PsiReference>>>(Keys)
        val cachedExpressionReferencesForMergedIndex by createKey<CachedValue<Array<out PsiReference>>>(Keys)

        val inBlockKeys by createKey<Set<String>>(Keys)
    }

    // region Common Methods

    fun isParameterized(text: String, conditionBlock: Boolean = true, full: Boolean = false): Boolean {
        // 快速判断，不检测带参数后的语法是否合法
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
        if (conditionBlock && text.indexOf("[[").let { c -> c != -1 && !text.isEscapedCharAt(c) }) return true
        return false
    }

    fun getParameterName(text: String): String? {
        // $PARAM$ - 仅限 高级插值语法 A
        if (!isParameterized(text, full = true)) return null
        return text.substring(1, text.length - 1).substringBefore('|')
    }

    fun getParameterRanges(text: String, conditionBlock: Boolean = true): List<TextRange> {
        // 比较复杂的实现逻辑
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
            element is ParadoxScriptBlock -> "" // should not be used
            element is ParadoxScriptInlineMath -> "" // should not be used
            rangeInElement != null -> rangeInElement.substring(element.text)
            element is ParadoxScriptStringExpressionElement -> element.text.unquote()
            element is ParadoxCsvColumn -> element.text.unquote()
            else -> element.text
        }
    }

    fun getExpressionTextRange(element: ParadoxExpressionElement): TextRange {
        return when (element) {
            is ParadoxScriptBlock -> TextRange.create(0, 1) // "{"
            is ParadoxScriptInlineMath -> element.firstChild.textRangeInParent // "@[" or "@\["
            is ParadoxScriptStringExpressionElement -> TextRange.create(0, element.text.length).unquote(element.text)
            is ParadoxCsvColumn -> TextRange.create(0, element.text.length).unquote(element.text)
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
        return CachedValuesManager.getCachedValue(element, Keys.cachedParameterRanges) {
            val value = doGetParameterRangesInExpression(element)
            value.withDependencyItems(element)
        }
    }

    private fun doGetParameterRangesInExpression(element: ParadoxExpressionElement): List<TextRange> {
        var parameterRanges: MutableList<TextRange>? = null
        element.processChild { e ->
            if (isParameterElementInExpression(e)) {
                if (parameterRanges == null) parameterRanges = mutableListOf()
                parameterRanges.add(e.textRange)
            }
            true
        }
        return parameterRanges.orEmpty()
    }

    fun isParameterElementInExpression(element: PsiElement): Boolean {
        return element is ParadoxParameter || element is ParadoxScriptInlineParameterCondition
            || element is ParadoxLocalisationParameter
    }

    fun isUnaryOperatorAwareParameter(text: String, parameterRanges: List<TextRange>): Boolean {
        return text.firstOrNull()?.let { it == '+' || it == '-' } == true
            && parameterRanges.singleOrNull()?.let { it.startOffset == 1 && it.endOffset == text.length } == true
    }

    // endregion

    // region Core Methods

    fun getConfigContext(element: PsiElement): CwtConfigContext? {
        ProgressManager.checkCanceled()
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return null
        return doGetConfigContextFromCache(memberElement)
    }

    private fun doGetConfigContextFromCache(element: ParadoxScriptMember): CwtConfigContext? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigContext) {
            val value = doGetConfigContext(element)
            // also depends on localisation files (for loc references)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetConfigContext(element: ParadoxScriptMember): CwtConfigContext? {
        return CwtConfigContextProvider.getContext(element)
    }

    fun getConfigsForConfigContext(
        element: ParadoxScriptMember,
        rootConfigs: List<CwtMemberConfig<*>>,
        elementPathFromRoot: ParadoxElementPath,
        configGroup: CwtConfigGroup,
        matchOptions: Int = Options.Default
    ): List<CwtMemberConfig<*>> {
        val result = doGetConfigsForConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        return result.sortedByPriority({ it.configExpression }, { it.configGroup })
    }

    private fun doGetConfigsForConfigContext(
        element: ParadoxScriptMember,
        rootConfigs: List<CwtMemberConfig<*>>,
        elementPathFromRoot: ParadoxElementPath,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): List<CwtMemberConfig<*>> {
        val isPropertyValue = element is ParadoxScriptValue && element.isPropertyValue()

        var result: List<CwtMemberConfig<*>> = rootConfigs

        val subPaths = elementPathFromRoot.subPaths
        subPaths.forEachIndexed f1@{ i, subPath ->
            ProgressManager.checkCanceled()

            // 如果整个过程中得到的某个propertyConfig的valueExpressionType是single_alias_right或alias_matches_left，则需要内联子规则
            // 如果整个过程中的某个key匹配内联规则的名字（如，inline_script），则需要内联此内联规则

            val isParameterized = subPath.isParameterized()
            val isFullParameterized = subPath.isParameterized(full = true)
            val shift = subPaths.lastIndex - i
            val matchesKey = isPropertyValue || shift > 0
            val expression = ParadoxScriptExpression.resolve(subPath, quoted = false, isKey = true)
            val nextResult = mutableListOf<CwtMemberConfig<*>>()

            val memberElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: element
            val pathToMatch = ParadoxElementPath.resolve(subPaths.drop(i).dropLast(1))
            val elementToMatch = memberElement.findParentByPath(pathToMatch.path)?.castOrNull<ParadoxScriptMember>() ?: return emptyList()

            val parameterizedKeyConfigs by lazy {
                if (!isParameterized) return@lazy null
                if (!isFullParameterized) return@lazy emptyList() // must be full parameterized yet
                ParadoxParameterManager.getParameterizedKeyConfigs(elementToMatch)
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
                                val matchResult = ParadoxScriptExpressionMatcher.matches(elementToMatch, expression, config.keyExpression, config, configGroup, matchOptions)
                                if (!matchResult.get(matchOptions)) return@f3
                            }
                            val inlinedConfigs = doInlineConfigForConfigContext(elementToMatch, subPath, config, matchOptions)
                            if (inlinedConfigs == null) {
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
                if (subPath == "-") return@r1 // #196
                if (!matchesKey) return@r1
                ProgressManager.checkCanceled()
                val resultValuesMatchKey = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
                result.forEach f@{ config ->
                    val matchResult = ParadoxScriptExpressionMatcher.matches(elementToMatch, expression, config.configExpression, config, configGroup, matchOptions)
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

        return result.optimized()
    }

    private fun doInlineConfigForConfigContext(
        element: ParadoxScriptMember,
        key: String,
        config: CwtPropertyConfig,
        matchOptions: Int
    ): List<CwtMemberConfig<*>>? {
        val configGroup = config.configGroup
        val valueExpression = config.valueExpression
        val result = when (valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> {
                val inlined = CwtConfigManipulator.inlineSingleAlias(config)
                inlined?.singleton?.list()
            }
            CwtDataTypes.AliasMatchLeft -> {
                val inlined = CwtConfigManipulator.inlineAlias(config) { aliasName -> getAliasSubNames(element, key, false, aliasName, configGroup, matchOptions) }
                inlined
            }
            else -> null
        }
        return result
    }

    fun getConfigs(
        element: PsiElement,
        orDefault: Boolean = true,
        matchOptions: Int = Options.Default
    ): List<CwtMemberConfig<*>> {

        ProgressManager.checkCanceled()
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        val configsMap = doGetConfigsCacheFromCache(memberElement)
        val cacheKey = buildString {
            append('#').append(orDefault.toInt())
            append('#').append(matchOptions)
        }
        return configsMap.getOrPut(cacheKey) {
            val result = doGetConfigs(memberElement, orDefault, matchOptions).optimized()
            result.sortedByPriority({ it.configExpression }, { it.configGroup })
        }
    }

    private fun doGetConfigsCacheFromCache(element: PsiElement): MutableMap<String, List<CwtMemberConfig<*>>> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigsCache) {
            val value = doGetConfigsCache()
            // also depends on localisation files (for loc references)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetConfigsCache(): MutableMap<String, List<CwtMemberConfig<*>>> {
        // use soft values to optimize memory
        return ContainerUtil.createConcurrentSoftValueMap()
    }

    private fun doGetConfigs(element: PsiElement, orDefault: Boolean, matchOptions: Int): List<CwtMemberConfig<*>> {
        // 未填写属性的值 - 匹配所有
        val keyExpression = when (element) {
            is ParadoxScriptFile -> null
            is ParadoxScriptProperty -> element.propertyKey.let { ParadoxScriptExpression.resolve(it, matchOptions) }
            is ParadoxScriptValue -> null
            else -> return emptyList()
        }
        val valueExpression = when (element) {
            is ParadoxScriptFile -> ParadoxScriptExpression.resolveBlock()
            is ParadoxScriptProperty -> element.propertyValue?.let { ParadoxScriptExpression.resolve(it, matchOptions) }
            is ParadoxScriptValue -> ParadoxScriptExpression.resolve(element, matchOptions)
            else -> return emptyList()
        }

        val configContext = getConfigContext(element) ?: return emptyList()
        val configGroup = configContext.configGroup

        ProgressManager.checkCanceled()
        val contextConfigs = configContext.getConfigs(matchOptions)
        if (contextConfigs.isEmpty()) return emptyList()

        if (element is ParadoxScriptDefinitionElement && configContext.isDefinition()) {
            // 直接返回 contextConfigs
            if (BitUtil.isSet(matchOptions, Options.AcceptDefinition)) return contextConfigs
        }

        // 匹配键
        val resultMatchKey = when {
            keyExpression != null -> {
                val resultValuesMatchKey = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
                contextConfigs.forEach f@{ config ->
                    if (config !is CwtPropertyConfig) return@f
                    val matchResult = ParadoxScriptExpressionMatcher.matches(element, keyExpression, config.keyExpression, config, configGroup, matchOptions)
                    if (matchResult == ParadoxExpressionMatcher.Result.NotMatch) return@f
                    resultValuesMatchKey += ResultValue(config, matchResult)
                }
                optimizeMatchedConfigs(element, keyExpression, resultValuesMatchKey, true, matchOptions)
            }
            else -> contextConfigs.filterIsInstance<CwtValueConfig>()
        }
        if (resultMatchKey.isEmpty()) return emptyList()

        // 如果无法获取valueExpression，则返回所有匹配键的规则
        if (valueExpression == null) return resultMatchKey

        // 得到所有可能匹配的结果
        ProgressManager.checkCanceled()
        val resultValues = mutableListOf<ResultValue<CwtMemberConfig<*>>>()
        resultMatchKey.forEach f@{ config ->
            val matchResult = ParadoxScriptExpressionMatcher.matches(element, valueExpression, config.valueExpression, config, configGroup, matchOptions)
            if (matchResult == ParadoxExpressionMatcher.Result.NotMatch) return@f
            resultValues += ResultValue(config, matchResult)
        }
        // 如果无结果且需要使用默认值，则返回所有可能匹配的规则
        if (resultValues.isEmpty() && orDefault) return resultMatchKey

        // 优化匹配结果
        ProgressManager.checkCanceled()
        val optimizedResult = optimizeMatchedConfigs(element, valueExpression, resultValues, false, matchOptions)
        // 如果仍然无结果且需要使用默认值，则返回所有可能匹配的规则
        if (optimizedResult.isEmpty() && orDefault) return resultMatchKey
        return optimizedResult
    }

    private fun doMatchParameterizedKeyConfigs(pkConfigs: List<CwtValueConfig>?, configExpression: CwtDataExpression): Boolean? {
        // 如果作为参数的键的规则类型可以（从扩展的CWT规则）推断出来且是匹配的，则需要继续向下匹配
        // 目前要求推断结果必须是唯一的
        // 目前不支持从参数的使用处推断 - 这可能会导致规则上下文的递归解析

        if (pkConfigs == null) return null // 不是作为参数的键，不作特殊处理
        if (pkConfigs.size != 1) return false // 推断结果不是唯一的，要求后续宽松匹配的结果是唯一的，否则认为没有最终匹配的结果
        return CwtConfigManipulator.mergeAndMatchValueConfig(pkConfigs, configExpression)
    }

    fun optimizeMatchedConfigs(
        element: PsiElement,
        expression: ParadoxScriptExpression,
        resultValues: List<ResultValue<CwtMemberConfig<*>>>,
        postHandle: Boolean,
        matchOptions: Int = Options.Default
    ): List<CwtMemberConfig<*>> {
        if (resultValues.isEmpty()) return emptyList()

        val configGroup = resultValues.first().value.configGroup

        // 首先尝试直接的精确匹配，如果有结果，则直接返回
        // 然后，尝试需要检测子句的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        // 然后，尝试需要检测作用域上下文的匹配，如果存在匹配项，则保留所有匹配的结果或者第一个匹配项
        // 然后，尝试非回退的匹配，如果有结果，则直接返回
        // 然后，尝试复杂表达式的回退的匹配（可以解析，但存在错误），如果有结果，则直接返回
        // 然后，尝试回退的匹配，如果有结果，则直接返回
        // 如果到这里仍然无法匹配，则直接返回空列表

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
                if (it.result is ParadoxExpressionMatcher.Result.LazyBlockAwareMatch) return@p false // 已经匹配过
                if (it.result is ParadoxExpressionMatcher.Result.LazyScopeAwareMatch) return@p false // 已经匹配过
                if (it.result is ParadoxExpressionMatcher.Result.LazySimpleMatch) return@p true // 直接认为是匹配的
                if (it.result is ParadoxExpressionMatcher.Result.PartialMatch) return@p false // 之后再匹配
                if (it.result is ParadoxExpressionMatcher.Result.FallbackMatch) return@p false // 之后再匹配
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

        // 后续处理

        // 如果要匹配的是字符串，且匹配结果中存在作为常量匹配的规则，则仅保留这些规则
        run r1@{
            if (newResult.size <= 1) return@r1
            if (expression.type != ParadoxType.String) return@r1
            val result1 = newResult.filter { isConstantMatch(expression, it.configExpression, configGroup) }
            if (result1.isEmpty()) return@r1
            newResult = result1
        }

        // 如果匹配结果中存在键相同的规则，且其值是子句，则尝试根据子句进行进一步的匹配
        run r1@{
            if (newResult.isEmpty()) return@r1
            val blockElement = element.castOrNull<ParadoxScriptProperty>()?.block ?: return@r1
            val blockExpression = ParadoxScriptExpression.resolveBlock()
            val configsToRemove = mutableSetOf<CwtPropertyConfig>()
            val group: Collection<List<CwtPropertyConfig>> = newResult.filterIsInstance<CwtPropertyConfig>().groupBy { it.key }.values
            group.forEach f1@{ configs ->
                if (configs.size <= 1) return@f1
                val configs1 = configs.filter { it.valueType == CwtType.Block }
                if (configs1.size <= 1) return@r1
                configs.forEach f2@{ config ->
                    val valueConfig = config.valueConfig ?: return@f2
                    val matchResult = ParadoxScriptExpressionMatcher.matches(blockElement, blockExpression, valueConfig.configExpression, valueConfig, configGroup, matchOptions)
                    if (matchResult.get(matchOptions)) return@f2
                    configsToRemove += config
                }
            }
            if (configsToRemove.isEmpty()) return@r1
            val result1 = newResult.filter { it !in configsToRemove }
            newResult = result1
        }

        // 如果结果不为空且结果中存在需要重载的规则，则全部替换成重载后的规则
        run r1@{
            if (newResult.isEmpty()) return@r1
            val result1 = mutableListOf<CwtMemberConfig<*>>()
            newResult.forEach f1@{ config ->
                val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(element, config)
                if (overriddenConfigs.isNullOrEmpty()) {
                    result1 += config
                    return@f1
                }
                // 这里需要再次进行匹配
                overriddenConfigs.forEach { c ->
                    val matchResult = ParadoxScriptExpressionMatcher.matches(element, expression, c.configExpression, c, configGroup, matchOptions)
                    if (matchResult.get(matchOptions)) {
                        result1 += c
                    }
                }
            }
            newResult = result1
        }

        return newResult.optimized()
    }

    // 兼容需要考虑内联的情况（如内联脚本）
    // 这里需要兼容匹配key的子句规则有多个的情况 - 匹配任意则使用匹配的首个规则，空子句或者都不匹配则使用合并的规则

    /**
     * 得到指定的[element]的作为值的子句中的子属性/值的出现次数信息。（先合并子规则）
     */
    fun getChildOccurrenceMap(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if (configs.isEmpty()) return emptyMap()
        val childConfigs = configs.flatMap { it.configs.orEmpty() }
        if (childConfigs.isEmpty()) return emptyMap()

        ProgressManager.checkCanceled()
        val childOccurrenceMap = doGetChildOccurrenceMapCacheFromCache(element) ?: return emptyMap()
        // NOTE cacheKey基于childConfigs即可，key相同而value不同的规则，上面的cardinality应当保证是一样的
        val cacheKey = childConfigs.joinToString(" ")
        return childOccurrenceMap.getOrPut(cacheKey) { doGetChildOccurrenceMap(element, configs).optimized() }
    }

    private fun doGetChildOccurrenceMapCacheFromCache(element: ParadoxScriptMember): MutableMap<String, Map<CwtDataExpression, Occurrence>>? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedChildOccurrenceMapCache) {
            val value = doGetChildOccurrenceMapCache()
            // also depends on localisation files (for loc references)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetChildOccurrenceMapCache(): MutableMap<String, Map<CwtDataExpression, Occurrence>> {
        // use soft values to optimize memory
        return ContainerUtil.createConcurrentSoftValueMap()
    }

    private fun doGetChildOccurrenceMap(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if (configs.isEmpty()) return emptyMap()
        val configGroup = configs.first().configGroup
        // 这里需要先按优先级排序
        val childConfigs = configs.flatMap { it.configs.orEmpty() }.sortedByPriority({ it.configExpression }, { configGroup })
        if (childConfigs.isEmpty()) return emptyMap()
        val project = configGroup.project
        val blockElement = when (element) {
            is ParadoxScriptDefinitionElement -> element.block
            is ParadoxScriptBlockElement -> element
            else -> null
        }
        if (blockElement == null) return emptyMap()
        val occurrenceMap = mutableMapOf<CwtDataExpression, Occurrence>()
        for (childConfig in childConfigs) {
            occurrenceMap[childConfig.configExpression] = childConfig.toOccurrence(element, project)
        }
        ProgressManager.checkCanceled()
        // 注意这里需要考虑内联和可选的情况
        blockElement.members().options(conditional = true, inline = true).forEach f@{ data ->
            val expression = when (data) {
                is ParadoxScriptProperty -> ParadoxScriptExpression.resolve(data.propertyKey)
                is ParadoxScriptValue -> ParadoxScriptExpression.resolve(data)
                else -> return@f
            }
            val isParameterized = expression.type == ParadoxType.String && expression.value.isParameterized()
            // may contain parameter -> can't and should not get occurrences
            if (isParameterized) {
                occurrenceMap.clear()
                return@f
            }
            val matched = childConfigs.find { childConfig ->
                if (childConfig is CwtPropertyConfig && data !is ParadoxScriptProperty) return@find false
                if (childConfig is CwtValueConfig && data !is ParadoxScriptValue) return@find false
                ParadoxScriptExpressionMatcher.matches(data, expression, childConfig.configExpression, childConfig, configGroup).get()
            }
            if (matched == null) return@f
            val occurrence = occurrenceMap[matched.configExpression]
            if (occurrence == null) return@f
            occurrence.actual += 1
        }
        return occurrenceMap
    }

    // endregion

    // region Annotate Methods

    fun annotateScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder, config: CwtConfig<*>) {
        val expressionText = getExpressionText(element, rangeInElement)
        ParadoxScriptExpressionSupport.annotate(element, rangeInElement, expressionText, holder, config)
    }

    fun annotateLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder) {
        val expressionText = getExpressionText(element, rangeInElement)
        ParadoxLocalisationExpressionSupport.annotate(element, rangeInElement, expressionText, holder)
    }

    fun annotateCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder, config: CwtValueConfig) {
        if (element is ParadoxCsvColumn && element.isHeaderColumn()) return
        val expressionText = getExpressionText(element, rangeInElement)
        ParadoxCsvExpressionSupport.annotate(element, rangeInElement, expressionText, holder, config)
    }

    fun annotateExpressionByAttributesKey(element: ParadoxExpressionElement, range: TextRange, attributesKey: TextAttributesKey, holder: AnnotationHolder) {
        if (range.isEmpty) return
        // skip parameter ranges
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

    fun annotateExpressionAsHighlightedReference(range: TextRange, holder: AnnotationHolder) {
        holder.newSilentAnnotation(HighlightInfoType.HIGHLIGHTED_REFERENCE_SEVERITY).range(range).textAttributes(DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE).create()
    }

    fun annotateComplexExpression(element: ParadoxExpressionElement, expression: ParadoxComplexExpression, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        annotateComplexExpressionNode(element, expression, holder, config)
    }

    private fun annotateComplexExpressionNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        val attributesKey = node.getAttributesKey(element)

        run {
            val mustUseAttributesKey = attributesKey != ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY && attributesKey != ParadoxScriptAttributesKeys.STRING_KEY
            if (attributesKey != null && mustUseAttributesKey) {
                annotateNodeByAttributesKey(element, node, attributesKey, holder)
                return@run
            }
            val attributesKeyConfig = node.getAttributesKeyConfig(element)
            if (attributesKeyConfig != null) {
                val rangeInElement = node.rangeInExpression.shiftRight(if (element.text.isLeftQuoted()) 1 else 0)
                annotateScriptExpression(element, rangeInElement, holder, attributesKeyConfig)
                return@run
            }
            if (attributesKey != null) {
                annotateNodeByAttributesKey(element, node, attributesKey, holder)
            }
        }

        if (node.nodes.isNotEmpty()) {
            for (node in node.nodes) {
                annotateComplexExpressionNode(element, node, holder, config)
            }
        }
    }

    private fun annotateNodeByAttributesKey(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, attributesKey: TextAttributesKey, holder: AnnotationHolder) {
        val startOffest = element.startOffset + getExpressionOffset(element)
        val rangeToAnnotate = node.rangeInExpression.shiftRight(startOffest)

        // merge text attributes from HighlighterColors.TEXT and attributesKey for token nodes (in case foreground is not set)
        if (node is ParadoxTokenNode) {
            val editorColorsManager = EditorColorsManager.getInstance()
            val schema = editorColorsManager.activeVisibleScheme ?: editorColorsManager.schemeForCurrentUITheme
            val textAttributes1 = schema.getAttributes(HighlighterColors.TEXT)
            val textAttributes2 = schema.getAttributes(attributesKey)
            val textAttributes = TextAttributes.merge(textAttributes1, textAttributes2)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(rangeToAnnotate).enforcedTextAttributes(textAttributes).create()
            return
        }

        annotateExpressionByAttributesKey(element, rangeToAnnotate, attributesKey, holder)
    }

    private val markerPairs = "()<>{}[]".chunked(2).flatMap { listOf(it, it.reversed()) }.associate { it.take(1) to it.takeLast(1) }

    // endregion

    // region Reference Methods

    fun getExpressionReferences(element: ParadoxExpressionElement): Array<out PsiReference> {
        ProgressManager.checkCanceled()
        return when (element) {
            is ParadoxScriptExpressionElement -> doGetExpressionReferencesFromCache(element)
            is ParadoxLocalisationExpressionElement -> doGetExpressionReferencesFromCache(element)
            is ParadoxCsvExpressionElement -> doGetExpressionReferencesFromCache(element)
            else -> PsiReference.EMPTY_ARRAY
        }
    }

    private fun doGetExpressionReferencesFromCache(element: ParadoxScriptExpressionElement): Array<out PsiReference> {
        if (!element.isExpression()) return PsiReference.EMPTY_ARRAY

        // 尝试兼容可能包含参数的情况
        // if(element.text.isParameterized()) return PsiReference.EMPTY_ARRAY

        val processMergedIndex = PlsCoreManager.processMergedIndex.get() == true
        val key = if (processMergedIndex) Keys.cachedExpressionReferencesForMergedIndex else Keys.cachedExpressionReferences
        return CachedValuesManager.getCachedValue(element, key) {
            val value = doGetExpressionReferences(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetExpressionReferences(element: ParadoxScriptExpressionElement): Array<out PsiReference> {
        // 尝试基于CWT规则进行解析
        val isKey = element is ParadoxScriptPropertyKey
        val processMergedIndex = PlsCoreManager.processMergedIndex.get() == true
        val matchOptions = if (processMergedIndex) Options.SkipIndex or Options.SkipScope else Options.Default
        val configs = getConfigs(element, orDefault = isKey, matchOptions = matchOptions)
        val config = configs.firstOrNull() ?: return PsiReference.EMPTY_ARRAY
        val textRange = getExpressionTextRange(element) // unquoted text
        val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
        return reference.collectReferences()
    }

    private fun doGetExpressionReferencesFromCache(element: ParadoxLocalisationExpressionElement): Array<out PsiReference> {
        if (!element.isComplexExpression()) return PsiReference.EMPTY_ARRAY

        // 尝试兼容可能包含参数的情况
        // if(text.isParameterized()) return PsiReference.EMPTY_ARRAY

        val processMergedIndex = PlsCoreManager.processMergedIndex.get() == true
        val key = if (processMergedIndex) Keys.cachedExpressionReferencesForMergedIndex else Keys.cachedExpressionReferences
        return CachedValuesManager.getCachedValue(element, key) {
            val value = doGetExpressionReferences(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetExpressionReferences(element: ParadoxLocalisationExpressionElement): Array<out PsiReference> {
        // 尝试解析为复杂表达式
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        val reference = ParadoxLocalisationExpressionPsiReference(element, textRange)
        return reference.collectReferences()
    }

    private fun doGetExpressionReferencesFromCache(element: ParadoxCsvExpressionElement): Array<out PsiReference> {
        val key = Keys.cachedExpressionReferences
        return CachedValuesManager.getCachedValue(element, key) {
            val value = doGetExpressionReferences(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetExpressionReferences(element: ParadoxCsvExpressionElement): Array<out PsiReference> {
        val columnConfig = when (element) {
            is ParadoxCsvColumn -> ParadoxCsvFileManager.getColumnConfig(element)
            else -> null
        }
        if (columnConfig == null) return PsiReference.EMPTY_ARRAY
        val textRange = getExpressionTextRange(element) // unquoted text
        val reference = ParadoxCsvExpressionPsiReference(element, textRange, columnConfig)
        return arrayOf(reference)
    }

    // endregion

    // region Resolve Methods

    fun resolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        ProgressManager.checkCanceled()

        if (configExpression == null) return null
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return null // 排除引用文本带参数的情况

        val result = ParadoxScriptExpressionSupport.resolve(element, rangeInElement, expressionText, config, isKey, exact)
        if (result != null) return result

        if (configExpression.isKey) return getResolvedConfigElement(element, config, config.configGroup)

        return null
    }

    fun multiResolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        if (configExpression == null) return emptySet()
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return emptySet() // 排除引用文本带参数的情况

        val result = ParadoxScriptExpressionSupport.multiResolve(element, rangeInElement, expressionText, config, isKey)
        if (result.isNotEmpty()) return result

        if (configExpression.isKey) return getResolvedConfigElement(element, config, config.configGroup).singleton.setOrEmpty()

        return emptySet()
    }

    private fun getResolvedConfigElement(element: ParadoxExpressionElement, config: CwtConfig<*>, configGroup: CwtConfigGroup): PsiElement? {
        val resolvedConfig = config.resolved()
        if (resolvedConfig is CwtMemberConfig<*> && resolvedConfig.pointer.isEmpty()) {
            // 特殊处理合成的CWT规则
            val gameType = configGroup.gameType
            val project = configGroup.project
            return CwtMemberConfigElement(element, resolvedConfig, gameType, project)
        }

        return resolvedConfig.pointer.element
    }

    fun resolveLocalisationExpression(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?): PsiElement? {
        ProgressManager.checkCanceled()
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return null // 排除引用文本带参数的情况

        val result = ParadoxLocalisationExpressionSupport.resolve(element, rangeInElement, expressionText)
        return result
    }

    fun multiResolveLocalisationExpression(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return emptySet() // 排除引用文本带参数的情况

        val result = ParadoxLocalisationExpressionSupport.multiResolve(element, rangeInElement, expressionText)
        return result
    }

    fun resolveCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, config: CwtValueConfig): PsiElement? {
        ProgressManager.checkCanceled()
        if (element is ParadoxCsvColumn && element.isHeaderColumn()) return null
        val expressionText = getExpressionText(element, rangeInElement)

        val result = ParadoxCsvExpressionSupport.resolve(element, rangeInElement, expressionText, config)
        return result
    }

    fun multiResolveCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, config: CwtValueConfig): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        if (element is ParadoxCsvColumn && element.isHeaderColumn()) return emptySet()
        val expressionText = getExpressionText(element, rangeInElement)

        val result = ParadoxCsvExpressionSupport.multiResolve(element, rangeInElement, expressionText, config)
        return result
    }

    fun resolveModifier(element: ParadoxExpressionElement, name: String, configGroup: CwtConfigGroup): PsiElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null // NOTE 1.4.0 - unnecessary to support yet
        return ParadoxModifierManager.resolveModifier(name, element, configGroup)
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

    // endregion

    // region Misc Methods

    fun isConstantMatch(expression: ParadoxScriptExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        // 注意这里可能需要在同一循环中同时检查keyExpression和valueExpression，因此这里需要特殊处理
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
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) // 不区分大小写
        if (constKey != null) return constKey
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return null
        val expression = ParadoxScriptExpression.resolve(key, quoted, true)
        return keys.find { ParadoxScriptExpressionMatcher.matches(element, expression, CwtDataExpression.resolve(it, true), null, configGroup, matchOptions).get(matchOptions) }
    }

    fun getAliasSubNames(element: PsiElement, key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, matchOptions: Int = Options.Default): Set<String> {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) // 不区分大小写
        if (constKey != null) return setOf(constKey)
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return emptySet()
        val expression = ParadoxScriptExpression.resolve(key, quoted, true)
        return keys.filterTo(mutableSetOf()) { ParadoxScriptExpressionMatcher.matches(element, expression, CwtDataExpression.resolve(it, true), null, configGroup, matchOptions).get(matchOptions) }
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
        return when (config) {
            is CwtPropertyConfig -> {
                config.inlineConfig?.let { return getEntryConfigs(it) }
                config.aliasConfig?.let { return getEntryConfigs(it) }
                config.singleAliasConfig?.let { return getEntryConfigs(it) }
                config.parentConfig?.configs?.filter { it is CwtPropertyConfig && it.key == config.key }?.let { return it }
                config.singleton.list()
            }
            is CwtValueConfig -> {
                config.propertyConfig?.let { return getEntryConfigs(it) }
                config.parentConfig?.configs?.filterIsInstance<CwtValueConfig>()?.let { return it }
                config.singleton.list()
            }
            is CwtSingleAliasConfig -> {
                config.config.singleton.listOrEmpty()
            }
            is CwtAliasConfig -> {
                configGroup.aliasGroups.get(config.name)?.get(config.subName)?.map { it.config }.orEmpty()
            }
            is CwtInlineConfig -> {
                config.config.singleton.listOrEmpty()
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
            when (config) {
                is CwtPropertyConfig -> {
                    val propertyConfig = config
                    propertyConfig.parentConfig?.configs?.forEach { c ->
                        if (c is CwtPropertyConfig && c.key.equals(propertyConfig.key, true) && c.pointer != propertyConfig.pointer) {
                            c.configs?.forEach { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                        }
                    }
                }
                is CwtValueConfig -> {
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
        if (config.key.isInlineScriptUsage()) return false // 排除是内联脚本调用的情况
        if (config.keyExpression.type != CwtDataTypes.Constant) return false
        if (config.optionData { cardinality }?.isRequired() == false) return false
        return true
    }

    fun getFullNamesFromSuffixAware(name: String, config: CwtConfig<*>): List<String> {
        val suffixes = config.configExpression?.suffixes
        if (suffixes.isNullOrEmpty()) return listOf(name)
        return suffixes.map { name + it }
    }

    // endregion
}
