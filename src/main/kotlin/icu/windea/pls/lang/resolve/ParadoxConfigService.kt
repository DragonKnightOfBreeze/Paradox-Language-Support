package icu.windea.pls.lang.resolve

import com.github.benmanes.caffeine.cache.Cache
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.declarationConfigCacheKey
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.originalConfig
import icu.windea.pls.config.config.overriddenProvider
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.cache.createNestedCache
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.createCachedValue
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.singleton
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.resolve.config.CwtConfigContextProvider
import icu.windea.pls.ep.resolve.config.CwtDeclarationConfigContextProvider
import icu.windea.pls.ep.resolve.config.CwtOverriddenConfigProvider
import icu.windea.pls.ep.resolve.config.CwtRelatedConfigProvider
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchOptionsUtil
import icu.windea.pls.lang.match.ParadoxMatchPipeline
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxMemberRole
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isPropertyValue

object ParadoxConfigService {
    @Optimized
    private val CwtConfigGroup.configsCache by registerKey(CwtConfigContext.Keys) {
        createCachedValue(project) {
            // rootFile -> cacheKey -> configs
            // use soft values to optimize memory
            createNestedCache<VirtualFile, _, _, Cache<String, List<CwtMemberConfig<*>>>> {
                CacheBuilder().softValues().build<String, List<CwtMemberConfig<*>>>().cancelable()
            }.withDependencyItems(ParadoxModificationTrackers.Match)
        }
    }

    @Optimized
    private val CwtConfigGroup.declarationConfigCache by registerKey(CwtDeclarationConfigContext.Keys) {
        createCachedValue(project) {
            // cacheKey -> declarationConfig
            // use soft values to optimize memory
            CacheBuilder().softValues().build<String, CwtPropertyConfig>().cancelable()
                .withDependencyItems(ModificationTracker.NEVER_CHANGED)
        }
    }

    /**
     * @see icu.windea.pls.ep.resolve.config.CwtOverriddenConfigProvider.getOverriddenConfigs
     */
    fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T> {
        val gameType = config.configGroup.gameType
        return CwtOverriddenConfigProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getOverriddenConfigs(contextElement, config).orNull()
                ?.onEach {
                    it.originalConfig = config
                    it.overriddenProvider = ep
                }
        }.orEmpty()
    }

    /**
     * @see icu.windea.pls.ep.resolve.config.CwtRelatedConfigProvider.getRelatedConfigs
     */
    fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
        val gameType = selectGameType(file) ?: return emptySet()
        val result = mutableSetOf<CwtConfig<*>>()
        CwtRelatedConfigProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            val r = ep.getRelatedConfigs(file, offset)
            result += r
        }
        return result
    }

    /**
     * @see icu.windea.pls.ep.resolve.config.CwtConfigContextProvider.getContext
     */
    fun getConfigContext(element: ParadoxScriptMember): CwtConfigContext? {
        val file = element.containingFile ?: return null
        val memberPathFromFile = ParadoxMemberService.getPath(element)?.normalize() ?: return null
        val memberRole = ParadoxMemberRole.resolve(element)
        val gameType = selectGameType(file)
        return CwtConfigContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getContext(element, file, memberPathFromFile, memberRole)?.also { it.provider = ep }
        }
    }

    /**
     * @see icu.windea.pls.ep.resolve.config.CwtDeclarationConfigContextProvider.getContext
     */
    fun getDeclarationConfigContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        val gameType = configGroup.gameType
        return CwtDeclarationConfigContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getContext(element, definitionName, definitionType, definitionSubtypes, configGroup)?.also { it.provider = ep }
        }
    }

    fun getConfigsForConfigContext(context: CwtConfigContext, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        val provider = context.provider
        val rootFile = selectRootFile(context.element) ?: return emptyList()
        val cache = context.configGroup.configsCache.value.get(rootFile)
        val cachedKey = provider.getCacheKey(context, options) ?: return emptyList()
        val cached = withRecursionGuard {
            withRecursionCheck(cachedKey) {
                try {
                    PlsStates.dynamicContextConfigs.set(false)
                    // use lock-freeze ConcurrentMap.getOrPut to prevent IDE freezing problems
                    cache.asMap().getOrPut(cachedKey) {
                        val result = provider.getConfigs(context, options)
                        result?.optimized().orEmpty()
                    }
                } finally {
                    // use uncached result if result context configs are dynamic (e.g., based on script context)
                    if (PlsStates.dynamicContextConfigs.get() == true) cache.invalidate(cachedKey)
                    PlsStates.dynamicContextConfigs.remove()
                }
            }
        } ?: emptyList() // unexpected recursion, return empty list
        return cached
    }

    fun getConfigForDeclarationConfigContext(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val provider = context.provider
        val cache = context.configGroup.declarationConfigCache.value
        val cacheKey = provider.getCacheKey(context, declarationConfig)
        val cached = cache.get(cacheKey) {
            val result = provider.getConfig(context, declarationConfig)
            result.apply { declarationConfigCacheKey = cacheKey }
        }
        return cached
    }

    // fun getContextConfigs(context: CwtConfigContext, parentConfigs: List<CwtMemberConfig<*>>, subPath: String, options: Int = 0): List<CwtMemberConfig<*>> {
    //     val result = doGetConfigsForConfigContext(element, parentConfigs, subPath, options)
    //     return result.sortedByPriority({ it.configExpression }, { it.configGroup }) // 按优先级排序
    // }
    //
    // private fun doGetConfigsForConfigContext(element: ParadoxScriptMember, parentConfigs: List<CwtMemberConfig<*>>, subPath: String, options: Int): List<CwtMemberConfig<*>> {
    //     ProgressManager.checkCanceled()
    //     if (parentConfigs.isEmpty()) return emptyList() // 忽略
    //     val memberRole = ParadoxMemberRole.resolve(element)
    //     if (memberRole == ParadoxMemberRole.Other) return emptyList() // 忽略
    //     var current = parentConfigs.asSequence()
    //
    //     // 根据 `subPath` 打平规则
    //     val expect = subPath
    //
    //
    //     // 如果 `element` 是属性值，则需要再次转换为属性值对应的规则
    //     if (memberRole == ParadoxMemberRole.PropertyValue) {
    //         current = current.mapNotNull { if (it is CwtPropertyConfig) it.valueConfig else null }
    //     }
    //
    //     return current.toList()
    // }

    fun getConfigsForConfigContext(
        element: ParadoxScriptMember,
        rootConfigs: List<CwtMemberConfig<*>>,
        memberPathFromRoot: ParadoxMemberPath,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
    ): List<CwtMemberConfig<*>> {
        val result = doGetConfigsForConfigContext(element, rootConfigs, memberPathFromRoot, configGroup, options)
        return result.sortedByPriority({ it.configExpression }, { it.configGroup }) // 按优先级排序
    }

    private fun doGetConfigsForConfigContext(
        element: ParadoxScriptMember,
        rootConfigs: List<CwtMemberConfig<*>>,
        memberPathFromRoot: ParadoxMemberPath,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions?,
    ): List<CwtMemberConfig<*>> {
        val isPropertyValue = element is ParadoxScriptValue && element.isPropertyValue()

        var result: List<CwtMemberConfig<*>> = rootConfigs

        val subPaths = memberPathFromRoot.subPaths
        subPaths.forEachIndexed f1@{ i, subPath ->
            val matchesKey = isPropertyValue || subPaths.lastIndex - i > 0
            val expression = ParadoxScriptExpression.resolve(subPath, quoted = false, isKey = true)

            val memberElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: element
            val pathToMatch = ParadoxMemberPath.resolve(subPaths.drop(i).dropLast(1))
            val elementToMatch = selectScope { memberElement.parentOfPath(pathToMatch.path)?.asMember() } ?: return emptyList()
            val parameterizedKeyConfigs by lazy { getParameterizedKeyConfigs(elementToMatch, expression) }

            val nextResult1 = mutableListOf<CwtMemberConfig<*>>()
            ProgressManager.checkCanceled()
            result.forEach f2@{ parentConfig ->
                val configs = parentConfig.configs
                if (configs.isNullOrEmpty()) return@f2

                val exactMatchedConfigs = mutableListOf<CwtMemberConfig<*>>()
                val relaxMatchedConfigs = mutableListOf<CwtMemberConfig<*>>()

                fun addToMatchedConfigs(config: CwtMemberConfig<*>) {
                    if (config is CwtPropertyConfig) {
                        val m = matchesParameterizedKeyConfigs(parameterizedKeyConfigs, config.keyExpression)
                        when (m) {
                            null -> nextResult1 += config
                            true -> exactMatchedConfigs += config
                            false -> relaxMatchedConfigs += config
                        }
                    } else if (config is CwtValueConfig) {
                        nextResult1 += config
                    }
                }

                fun collectMatchedConfigs() {
                    if (exactMatchedConfigs.isNotEmpty()) {
                        nextResult1 += exactMatchedConfigs
                    } else if (relaxMatchedConfigs.size == 1) {
                        nextResult1 += relaxMatchedConfigs
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
            val nextResult = nextResult1
            result = nextResult

            run r1@{
                if (subPath == "-") return@r1 // #196
                if (!matchesKey) return@r1
                ProgressManager.checkCanceled()
                val candidates = ParadoxMatchPipeline.collectCandidates(result) { config ->
                    ParadoxMatchService.matchScriptExpression(elementToMatch, expression, config.configExpression, config, configGroup, options)
                }
                val filteredResult = ParadoxMatchPipeline.filter(candidates, options)
                val optimizedResult = ParadoxMatchPipeline.optimize(elementToMatch, expression, filteredResult, options)
                result = optimizedResult
            }
        }

        if (isPropertyValue) {
            result = result.mapNotNull { if (it is CwtPropertyConfig) it.valueConfig else null }
        }

        return result
    }

    private fun getParameterizedKeyConfigs(element: ParadoxScriptMember, expression: ParadoxScriptExpression): List<CwtValueConfig>? {
        // 脚本表达式必须带参数（目前来说，如果不是整个作为参数，则直接返回空列表）

        if (!expression.isParameterized()) return null
        if (!expression.isFullParameterized()) return emptyList()
        return ParadoxParameterManager.getParameterizedKeyConfigs(element)
    }

    private fun matchesParameterizedKeyConfigs(configs: List<CwtValueConfig>?, configExpression: CwtDataExpression): Boolean? {
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

    fun getConfigs(element: ParadoxScriptMember, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        val result = doGetConfigs(element, options)
        return result.sortedByPriority({ it.configExpression }, { it.configGroup }) // 按优先级排序
    }

    private fun doGetConfigs(element: ParadoxScriptMember, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        ProgressManager.checkCanceled()
        val configContext = ParadoxConfigManager.getConfigContext(element)
        if (configContext == null) return emptyList()

        ProgressManager.checkCanceled()
        val contextConfigs = configContext.getConfigs(options)
        if (contextConfigs.isEmpty()) return emptyList()

        // 如果当前上下文是定义，且匹配选项接受定义，则直接返回所有上下文规则
        if (element is ParadoxScriptDefinitionElement && configContext.isRootForDefinition()) {
            if (ParadoxMatchOptionsUtil.acceptDefinition(options)) return contextConfigs
        }

        val configGroup = configContext.configGroup
        val fallback = ParadoxMatchOptionsUtil.fallback(options)
        when (element) {
            is ParadoxScriptProperty -> {
                // 匹配属性
                val result = contextConfigs.filterIsInstance<CwtPropertyConfig>()
                if (result.isEmpty()) return emptyList() // 如果无结果，则直接返回空列表

                ProgressManager.checkCanceled()
                val keyExpression = element.propertyKey.let { ParadoxScriptExpression.resolve(it, options) }
                val candidatesForKey = ParadoxMatchPipeline.collectCandidates(result) { config ->
                    ParadoxMatchService.matchScriptExpression(element, keyExpression, config.keyExpression, config, configGroup, options)
                }
                val filteredResultForKey = ParadoxMatchPipeline.filter(candidatesForKey, options)
                val optimizedResultForKey = ParadoxMatchPipeline.optimize(element, keyExpression, filteredResultForKey, options)
                val resultForKey = optimizedResultForKey
                if (resultForKey.isEmpty()) return emptyList() // 如果无结果，则直接返回空列表

                ProgressManager.checkCanceled()
                val valueExpression = element.propertyValue?.let { ParadoxScriptExpression.resolve(it, options) }
                if (valueExpression == null) return resultForKey // 如果无法得到值表达式，则返回所有匹配键的规则
                val candidates = ParadoxMatchPipeline.collectCandidates(resultForKey) { config ->
                    ParadoxMatchService.matchScriptExpression(element, valueExpression, config.valueExpression, config, configGroup, options)
                }
                if (candidates.isEmpty() && fallback) return resultForKey // 如果无结果，则需要考虑回退
                val finalResult = ParadoxMatchPipeline.filter(candidates, options)
                if (finalResult.isEmpty() && fallback) return candidates.map { it.value } // 如果无结果，则需要考虑回退
                return finalResult // 返回最终匹配的规则
            }
            else -> {
                // 匹配文件或单独的值
                val result = contextConfigs.filterIsInstance<CwtValueConfig>()
                if (result.isEmpty()) return emptyList() // 如果无结果，则直接返回空列表

                ProgressManager.checkCanceled()
                val valueExpression = when (element) {
                    is ParadoxScriptFile -> ParadoxScriptExpression.resolveBlock()
                    is ParadoxScriptValue -> ParadoxScriptExpression.resolve(element, options)
                    else -> null
                }
                if (valueExpression == null) return result // 如果无法得到值表达式，则返回所有上下文值规则
                val candidates = ParadoxMatchPipeline.collectCandidates(result) { config ->
                    ParadoxMatchService.matchScriptExpression(element, valueExpression, config.valueExpression, config, configGroup, options)
                }
                if (candidates.isEmpty() && fallback) return result // 如果无结果，则需要考虑回退
                val finalResult = ParadoxMatchPipeline.filter(candidates, options)
                    .let { ParadoxMatchPipeline.optimize(element, valueExpression, it, options) }
                if (finalResult.isEmpty() && fallback) return candidates.map { it.value } // 如果无结果，则需要考虑回退
                return finalResult // 返回最终匹配的规则
            }
        }
    }
}
