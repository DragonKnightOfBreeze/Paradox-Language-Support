package icu.windea.pls.lang.resolve

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.BitUtil
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configContext.isRootForDefinition
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOccurrence
import icu.windea.pls.lang.match.ParadoxMatchOccurrenceService.evaluate
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchPipeline
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager.getConfigContext
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isPropertyValue

object ParadoxExpressionService {
    fun getConfigsForConfigContext(
        element: ParadoxScriptMember,
        rootConfigs: List<CwtMemberConfig<*>>,
        memberPathFromRoot: ParadoxMemberPath,
        configGroup: CwtConfigGroup,
        matchOptions: Int = ParadoxMatchOptions.Default
    ): List<CwtMemberConfig<*>> {
        val result = doGetConfigsForConfigContext(element, rootConfigs, memberPathFromRoot, configGroup, matchOptions)
        return result.sortedByPriority({ it.configExpression }, { it.configGroup }) // 按优先级排序
    }

    private fun doGetConfigsForConfigContext(
        element: ParadoxScriptMember,
        rootConfigs: List<CwtMemberConfig<*>>,
        memberPathFromRoot: ParadoxMemberPath,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): List<CwtMemberConfig<*>> {
        val isPropertyValue = element is ParadoxScriptValue && element.isPropertyValue()

        var result: List<CwtMemberConfig<*>> = rootConfigs

        val subPaths = memberPathFromRoot.subPaths
        subPaths.forEachIndexed f1@{ i, subPath ->
            // 如果整个过程中得到的某个 propertyConfig 的 valueExpressionType 是 `single_alias_right` 或 `alias_matches_left` ，则需要内联子规则
            // 如果整个过程中的某个 key 匹配内联规则的名字（如，`inline_script`），则需要内联此内联规则

            val isParameterized = subPath.isParameterized()
            val isFullParameterized = subPath.isParameterized(full = true)
            val matchesKey = isPropertyValue || subPaths.lastIndex - i > 0
            val expression = ParadoxScriptExpression.resolve(subPath, quoted = false, isKey = true)
            val nextResult = mutableListOf<CwtMemberConfig<*>>()

            val memberElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: element
            val pathToMatch = ParadoxMemberPath.resolve(subPaths.drop(i).dropLast(1))
            val elementToMatch = selectScope { memberElement.parentOfPath(pathToMatch.path).asMember() } ?: return emptyList()

            val parameterizedKeyConfigs by lazy {
                if (!isParameterized) return@lazy null
                if (!isFullParameterized) return@lazy emptyList() // must be full parameterized yet
                ParadoxParameterManager.getParameterizedKeyConfigs(elementToMatch)
            }

            run r1@{
                ProgressManager.checkCanceled()
                result.forEach f2@{ parentConfig ->
                    val configs = parentConfig.configs
                    if (configs.isNullOrEmpty()) return@f2

                    val exactMatchedConfigs = mutableListOf<CwtMemberConfig<*>>()
                    val relaxMatchedConfigs = mutableListOf<CwtMemberConfig<*>>()

                    fun addToMatchedConfigs(config: CwtMemberConfig<*>) {
                        if (config is CwtPropertyConfig) {
                            val m = matchParameterizedKeyConfigs(parameterizedKeyConfigs, config.keyExpression)
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
                            val inlinedConfigs = inlineConfigForConfigContext(config, subPath)
                            if (inlinedConfigs == null) { // null (cannot or failed)
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
                val candidates = ParadoxMatchPipeline.collectCandidates(result) { config ->
                    ParadoxMatchService.matchScriptExpression(elementToMatch, expression, config.configExpression, config, configGroup, matchOptions)
                }
                val filteredResult = ParadoxMatchPipeline.filter(candidates, matchOptions)
                val optimizedResult = ParadoxMatchPipeline.optimize(elementToMatch, expression, filteredResult, matchOptions)
                result = optimizedResult
            }
        }

        if (isPropertyValue) {
            result = result.mapNotNull { if (it is CwtPropertyConfig) it.valueConfig else null }
        }

        return result
    }

    private fun matchParameterizedKeyConfigs(configs: List<CwtValueConfig>?, configExpression: CwtDataExpression): Boolean? {
        // 如果作为参数的键的规则类型可以（从扩展的规则）推断出来且是匹配的，则需要继续向下匹配
        // 目前要求推断结果必须是唯一的
        // 目前不支持从参数的使用处推断 - 这可能会导致规则上下文的递归解析

        if (configs == null) return null // 不是作为参数的键，不作特殊处理
        if (configs.size != 1) return false // 推断结果不是唯一的，要求后续宽松匹配的结果是唯一的，否则认为没有最终匹配的结果
        return CwtConfigManipulator.mergeAndMatchValueConfig(configs, configExpression)
    }

    private fun inlineConfigForConfigContext(config: CwtPropertyConfig, key: String): List<CwtMemberConfig<*>>? {
        // 内联单别名规则和别名规则（如果别名规则内联后涉及单别名规则，会继续内联）

        val valueExpression = config.valueExpression
        val result = when (valueExpression.type) {
            CwtDataTypes.SingleAliasRight -> {
                val inlined = CwtConfigManipulator.inlineSingleAlias(config)
                inlined?.singleton?.list()
            }
            CwtDataTypes.AliasMatchLeft -> {
                val inlined = CwtConfigManipulator.inlineAlias(config, key)
                inlined
            }
            else -> null
        }
        return result
    }

    fun getConfigs(element: ParadoxScriptMember, orDefault: Boolean = true, matchOptions: Int = ParadoxMatchOptions.Default): List<CwtMemberConfig<*>> {
        val result = doGetConfigs(element, orDefault, matchOptions)
        return result.sortedByPriority({ it.configExpression }, { it.configGroup }) // 按优先级排序
    }

    private fun doGetConfigs(element: ParadoxScriptMember, orDefault: Boolean, matchOptions: Int): List<CwtMemberConfig<*>> {
        ProgressManager.checkCanceled()
        val configContext = getConfigContext(element)
        if (configContext == null) return emptyList()
        ProgressManager.checkCanceled()
        val contextConfigs = configContext.getConfigs(matchOptions)
        if (contextConfigs.isEmpty()) return emptyList()

        // 如果当前上下文是定义，且匹配选项接受定义，则直接返回所有上下文规则
        if (element is ParadoxScriptDefinitionElement && configContext.isRootForDefinition()) {
            if (BitUtil.isSet(matchOptions, ParadoxMatchOptions.AcceptDefinition)) return contextConfigs
        }

        val configGroup = configContext.configGroup
        when (element) {
            is ParadoxScriptProperty -> {
                // 匹配属性
                val result = contextConfigs.filterIsInstance<CwtPropertyConfig>()
                if (result.isEmpty()) return emptyList() // 如果无结果，则直接返回空列表

                ProgressManager.checkCanceled()
                val keyExpression = element.propertyKey.let { ParadoxScriptExpression.resolve(it, matchOptions) }
                val candidatesForKey = ParadoxMatchPipeline.collectCandidates(result) { config ->
                    ParadoxMatchService.matchScriptExpression(element, keyExpression, config.keyExpression, config, configGroup, matchOptions)
                }
                val filteredResultForKey = ParadoxMatchPipeline.filter(candidatesForKey, matchOptions)
                val optimizedResultForKey = ParadoxMatchPipeline.optimize(element, keyExpression, filteredResultForKey, matchOptions)
                val resultForKey = optimizedResultForKey
                if (resultForKey.isEmpty()) return emptyList() // 如果无结果，则直接返回空列表

                ProgressManager.checkCanceled()
                val valueExpression = element.propertyValue?.let { ParadoxScriptExpression.resolve(it, matchOptions) }
                if (valueExpression == null) return resultForKey // 如果无法得到值表达式，则返回所有匹配键的规则
                val candidates = ParadoxMatchPipeline.collectCandidates(resultForKey) { config ->
                    ParadoxMatchService.matchScriptExpression(element, valueExpression, config.valueExpression, config, configGroup, matchOptions)
                }
                if (candidates.isEmpty() && orDefault) return resultForKey // 如果无结果，则需要考虑回退
                val finalResult = ParadoxMatchPipeline.filter(candidates, matchOptions)
                if (finalResult.isEmpty() && orDefault) return candidates.map { it.value } // 如果无结果，则需要考虑回退
                return finalResult // 返回最终匹配的规则
            }
            else -> {
                // 匹配文件或单独的值
                val result = contextConfigs.filterIsInstance<CwtValueConfig>()
                if (result.isEmpty()) return emptyList() // 如果无结果，则直接返回空列表

                ProgressManager.checkCanceled()
                val valueExpression = when (element) {
                    is ParadoxScriptFile -> ParadoxScriptExpression.resolveBlock()
                    is ParadoxScriptValue -> ParadoxScriptExpression.resolve(element, matchOptions)
                    else -> null
                }
                if (valueExpression == null) return result // 如果无法得到值表达式，则返回所有上下文值规则
                val candidates = ParadoxMatchPipeline.collectCandidates(result) { config ->
                    ParadoxMatchService.matchScriptExpression(element, valueExpression, config.valueExpression, config, configGroup, matchOptions)
                }
                if (candidates.isEmpty() && orDefault) return result // 如果无结果，则需要考虑回退
                val finalResult = ParadoxMatchPipeline.filter(candidates, matchOptions)
                    .let { ParadoxMatchPipeline.optimize(element, valueExpression, it, matchOptions) }
                if (finalResult.isEmpty() && orDefault) return candidates.map { it.value } // 如果无结果，则需要考虑回退
                return finalResult // 返回最终匹配的规则
            }
        }
    }

    fun getChildOccurrences(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, ParadoxMatchOccurrence> {
        if (configs.isEmpty()) return emptyMap()
        val result = doGetChildOccurrences(element, configs)
        return result
    }

    private fun doGetChildOccurrences(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, ParadoxMatchOccurrence> {
        // 兼容需要考虑内联的情况（如内联脚本）
        // 这里需要兼容匹配键的子句规则有多个的情况 - 匹配任意则使用匹配的首个规则，空子句或者都不匹配则使用合并的规则
        ProgressManager.checkCanceled()
        val configGroup = configs.first().configGroup
        // 这里需要先按优先级排序
        val childConfigs = configs.flatMap { it.configs.orEmpty() }.sortedByPriority({ it.configExpression }, { configGroup })
        if (childConfigs.isEmpty()) return emptyMap()
        val blockElement = when (element) {
            is ParadoxScriptDefinitionElement -> element.block
            is ParadoxScriptBlockElement -> element
            else -> null
        }
        if (blockElement == null) return emptyMap()
        val occurrences = mutableMapOf<CwtDataExpression, ParadoxMatchOccurrence>()
        for (childConfig in childConfigs) {
            occurrences[childConfig.configExpression] = evaluate(element, childConfig)
        }
        ProgressManager.checkCanceled()
        // 注意这里需要考虑内联和可选的情况
        blockElement.members(conditional = true, inline = true).forEach f@{ data ->
            val expression = when (data) {
                is ParadoxScriptProperty -> ParadoxScriptExpression.resolve(data.propertyKey)
                is ParadoxScriptValue -> ParadoxScriptExpression.resolve(data)
                else -> return@f
            }
            val isParameterized = expression.type == ParadoxType.String && expression.value.isParameterized()
            // may contain parameter -> can't and should not get occurrences
            if (isParameterized) {
                occurrences.clear()
                return@f
            }
            val matched = childConfigs.find { childConfig ->
                if (childConfig is CwtPropertyConfig && data !is ParadoxScriptProperty) return@find false
                if (childConfig is CwtValueConfig && data !is ParadoxScriptValue) return@find false
                ParadoxMatchService.matchScriptExpression(data, expression, childConfig.configExpression, childConfig, configGroup).get()
            }
            if (matched == null) return@f
            val occurrence = occurrences[matched.configExpression]
            if (occurrence == null) return@f
            occurrence.actual += 1
        }
        return occurrences
    }
}
