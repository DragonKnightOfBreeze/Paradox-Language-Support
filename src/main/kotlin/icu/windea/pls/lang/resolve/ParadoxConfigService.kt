package icu.windea.pls.lang.resolve

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parents
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.isSamePointer
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.mockConfigModel
import icu.windea.pls.config.filterProperties
import icu.windea.pls.config.filterValues
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.config.match.CwtConfigExpressionMatchService
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.cache.createNestedCache
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.CaseInsensitiveStringSet
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.createCachedValue
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.registerKeyWithThis
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.ep.resolve.config.CwtConfigContextProvider
import icu.windea.pls.ep.resolve.config.CwtDeclarationConfigContextProvider
import icu.windea.pls.ep.resolve.config.CwtOverriddenConfigProvider
import icu.windea.pls.ep.resolve.config.CwtRelatedConfigProvider
import icu.windea.pls.lang.ParadoxThreadContext
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.match.CwtRowConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxCsvExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchContext
import icu.windea.pls.lang.match.toHashString
import icu.windea.pls.lang.select.*
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.orSpecific
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.model.type.ParadoxMemberRole
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.containingDirectMember
import icu.windea.pls.script.psi.isDirectMember
import java.util.*
import kotlin.concurrent.getOrSet

@Optimized
object ParadoxConfigService {
    private val CwtConfigGroup.configsCache by registerKeyWithThis(CwtConfigGroup.Keys) {
        createCachedValue(project) {
            // rootFile -> cacheKey -> configs
            // use soft values to optimize memory
            createNestedCache<VirtualFile, _, _> {
                CacheBuilder().softValues().build<String, List<CwtMemberConfig<*>>>().cancelable()
            }.withDependencyItems(ChronicleModificationTrackers.ConfigResolution)
        }
    }

    private val CwtConfigGroup.declarationConfigCache by registerKeyWithThis(CwtConfigGroup.Keys) {
        createCachedValue(project) {
            // cacheKey -> declarationConfig
            // use soft values to optimize memory
            CacheBuilder().softValues().build<String, CwtPropertyConfig>().cancelable()
                .withDependencyItems(ModificationTracker.NEVER_CHANGED)
        }
    }

    object Keys : KeyRegistry() {
        val inBlockKeys by registerKey<Set<String>>(this)
    }

    /**
     * @see CwtRelatedConfigProvider.getRelatedConfigs
     */
    fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
        val gameType = selectGameType(file) ?: return emptySet()
        val result = mutableSetOf<CwtConfig<*>>()
        val eps = CwtRelatedConfigProvider.getAll()
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.getRelatedConfigs(file, offset)
            result += r
        }
        if (result.isEmpty()) return emptySet()
        return result
    }

    /**
     * @see CwtOverriddenConfigProvider.getOverriddenConfigs
     */
    fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T> {
        val gameType = config.configGroup.gameType
        val eps = CwtOverriddenConfigProvider.getAll()
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.getOverriddenConfigs(contextElement, config).orNull()
                ?.onEach {
                    it.originalConfig = config
                    it.overriddenProvider = ep
                }
            if (r != null) return r
        }
        return emptyList()
    }

    /**
     * @see CwtConfigContextProvider.getContext
     */
    fun getConfigContext(element: ParadoxScriptMember): CwtConfigContext? {
        val file = element.containingFile ?: return null
        val gameType = selectGameType(file) ?: return null
        val memberPathFromFile = ParadoxMemberService.getPath(element) ?: return null
        val memberRole = ParadoxTypeResolver.resolveMemberRole(element)
        val configGroup = ChronicleFacade.getConfigGroup(file.project, gameType)
        val eps = CwtConfigContextProvider.getAll()
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.getContext(configGroup, element, file, memberRole, memberPathFromFile)
            if (r != null) return r
        }
        return null
    }

    /**
     * @see CwtDeclarationConfigContextProvider.getContext
     */
    fun getDeclarationConfigContext(element: PsiElement, configGroup: CwtConfigGroup, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?): CwtDeclarationConfigContext? {
        val gameType = configGroup.gameType
        val eps = CwtDeclarationConfigContextProvider.getAll()
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.getContext(configGroup, element, definitionName, definitionType, definitionSubtypes)
            if (r != null) return r
        }
        return null
    }

    fun getConfigsForConfigContext(context: CwtConfigContext, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        if (context.dynamic) {
            // NOTE 2.1.1 prefix in-config-context cache if marked as dynamic
            val dynamicCacheKey = options.toHashString(forMatched = false).optimized() // optimized to optimize memory
            val cached = context.dynamicCache.getIfPresent(dynamicCacheKey)
            if (cached != null) return cached
        }
        val rootFile = context.rootFile ?: return emptyList() // 3.0.1 optimize: get root file from context object directly
        val provider = context.provider
        val cache = context.configGroup.configsCache.value.get(rootFile)
        val cacheKey = provider.getCacheKey(context, options) ?: return emptyList()
        val cached = withRecursionGuard("ParadoxConfigService.getConfigsForConfigContext") {
            withRecursionCheck(cacheKey) {
                val resolvingStack = ParadoxThreadContext.resolvingConfigContextStack.getOrSet { ArrayDeque() }
                resolvingStack.addLast(context)
                try {
                    // use lock-freeze `ConcurrentMap.getOrPut` to prevent IDE freezing problems (WARNING: or will cause deadlock!)
                    cache.asMap().getOrPut(cacheKey) {
                        val result = provider.getConfigs(context, options)
                        result.optimized()
                    }
                } finally {
                    resolvingStack.pollLast()
                    if (context.dynamic) {
                        // invalidate in-config-group cache if result context configs are dynamic (e.g., based on script context)
                        cache.invalidate(cacheKey)
                    }
                    if (resolvingStack.isEmpty()) ParadoxThreadContext.resolvingConfigContextStack.remove()
                }
            }
        } ?: return emptyList() // unexpected recursion, return empty list
        if (context.dynamic) {
            // NOTE 2.1.1 store dynamic result into in-config-context cache
            val dynamicCacheKey = options.toHashString(forMatched = false).optimized() // optimized to optimize memory
            context.dynamicCache.put(dynamicCacheKey, cached)
        }
        return cached
    }

    fun getConfigForDeclarationConfigContext(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val provider = context.provider
        val cacheKey = provider.getCacheKey(context, declarationConfig)
        val cache = context.configGroup.declarationConfigCache.value
        val cached = cache.get(cacheKey) {
            val result = provider.getConfig(context, declarationConfig)
            result.apply { declarationConfigCacheKey = cacheKey }
        }
        return cached
    }

    fun getTopConfigsForConfigContext(context: CwtConfigContext, rootConfigs: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        if (rootConfigs.isEmpty()) return emptyList()
        if (context.memberRole == ParadoxMemberRole.PropertyValue) {
            return rootConfigs.mapNotNullFast { if (it is CwtPropertyConfig) it.valueConfig else null }
        }
        return rootConfigs
    }

    fun getFlattenedConfigsForConfigContext(context: CwtConfigContext, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        val result = flattenConfigsForConfigContext(context, options)
        return result.sortedByPriority({ it.configExpression }, { it.configGroup }) // 按优先级排序
    }

    private fun flattenConfigsForConfigContext(context: CwtConfigContext, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        ProgressManager.checkCanceled()

        if (context.memberRole == ParadoxMemberRole.Other) return emptyList() // 忽略
        val memberPath = context.memberPath ?: return emptyList() // 忽略
        val subPaths = memberPath.subPaths
        if (subPaths.isEmpty()) return emptyList() // 忽略
        val subPath = subPaths.last()
        val expression = ParadoxExpression.resolve(subPath, quoted = false, role = ParadoxExpressionRole.Key)
        val parentSubPath = subPaths.getOrNull(subPaths.lastIndex - 1)
        val parentExpression = parentSubPath?.let { ParadoxExpression.resolve(it, quoted = false, role = ParadoxExpressionRole.Key) }

        val configGroup = context.configGroup
        val element = context.element ?: return emptyList() // null -> unexpected (should be bound first)
        val member = element.containingDirectMember
        val parentMember = member.parents(withSelf = false).findIsInstance<ParadoxScriptMember> { it is ParadoxScriptFile || it.isDirectMember() } ?: return emptyList()

        // 从存储于 PSI 的上级缓存中获取 `parentContext`（父上下文），然后再从存储于规则分组的缓存中获取 `parentConfigs`（父上下文规则）
        val parentContext = ParadoxConfigManager.getConfigContext(parentMember) ?: return emptyList()
        // NOTE 2.1.2 如果父上下文是动态的，也需要把子上下文标记为动态的
        if (parentContext.dynamic) context.markDynamic()

        // 得到父上下文的上下文规则
        val parentConfigs = parentContext.getConfigs(options)
        if (parentConfigs.isEmpty()) return emptyList() // 忽略

        // `parentConfigs` 是上下文规则，因此如果 `parentSubPath` 对应一个脚本属性，需要先进行一次匹配
        val matchedParentConfigs = when {
            parentExpression != null && parentMember is ParadoxScriptProperty -> matchConfigsForConfigContext(parentMember, parentExpression, parentConfigs, configGroup, options)
            else -> parentConfigs
        }
        if (matchedParentConfigs.isEmpty()) return emptyList() // 忽略

        // 按照 `subPath` 打平规则，并进行必要的处理
        val result = collectConfigsForConfigContext(expression, matchedParentConfigs, configGroup)
        if (result.isEmpty()) return emptyList()

        // 如果 `element` 是属性值，需要再次进行匹配，并接着转换为属性值对应的规则
        if (context.memberRole == ParadoxMemberRole.PropertyValue) {
            val matchedResult = matchConfigsForConfigContext(element, expression, result, configGroup, options)
            return matchedResult.mapNotNullFast { if (it is CwtPropertyConfig) it.valueConfig else null }
        }

        return result
    }

    private fun collectConfigsForConfigContext(expression: ParadoxExpression, parentConfigs: List<CwtMemberConfig<*>>, configGroup: CwtConfigGroup): List<CwtMemberConfig<*>> {
        val result = mutableListOf<CwtMemberConfig<*>>()
        if (expression.value == "-") {
            parentConfigs.forEachFast f1@{ parentConfig ->
                // NOTE #386 use `$any` only, if value expression of parent config is `$any`
                // NOTE 3.0.2 compatible with `wildcard_scalar`, which is for complex parameters, in case
                if (CwtConfigExpressionMatchService.matchesAnyDataType(parentConfig.valueExpression)) {
                    return listOf(configGroup.mockConfigModel.anyValue)
                }

                val configs = parentConfig.values
                if (configs.isNullOrEmpty()) return@f1

                configs.forEachFast { config ->
                    result.add(config)
                }
            }
        } else {
            parentConfigs.forEachFast f1@{ parentConfig ->
                // NOTE #386 use `$any = $any` only, if value expression of parent config is `$any`
                // NOTE 3.0.2 compatible with `wildcard_scalar`, which is for complex parameters, in case
                if (CwtConfigExpressionMatchService.matchesAnyDataType(parentConfig.valueExpression)) {
                    return listOf(configGroup.mockConfigModel.anyProperty)
                }

                val configs = parentConfig.properties
                if (configs.isNullOrEmpty()) return@f1

                configs.forEachFast { config ->
                    // it is necessary to perform inlining first after flatten
                    // ff the alias config, after inlining, involves a single alias config, it's necessary to continue inlining
                    val inlinedConfigs = CwtConfigManipulationService.inlineForConfigContext(config, expression.value)
                    if (inlinedConfigs != null) {
                        result.addAll(inlinedConfigs)
                    } else {
                        result.add(config)
                    }
                }
            }
        }
        return result
    }

    private fun matchConfigsForConfigContext(element: ParadoxScriptMember, expression: ParadoxExpression, configs: List<CwtMemberConfig<*>>, configGroup: CwtConfigGroup, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        ProgressManager.checkCanceled()
        val candidates = ParadoxMatchService.collectCandidates(configs) { config ->
            val context = ParadoxScriptExpressionMatchContext(element, expression, config.configExpression, config, configGroup, options)
            ParadoxExpressionMatchService.matchScriptExpression(context)
        }
        val processedCandidates = ParadoxMatchService.processCandidates(candidates, options)
        val processed = processedCandidates.mapFast { it.value }
        val optimized = ParadoxMatchService.optimize(processed, element, expression, options)
        val result = optimized
        return result
    }

    fun getConfigs(element: ParadoxScriptMember, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        val result = matchConfigs(element, options)
        return result.sortedByPriority({ it.configExpression }, { it.configGroup }) // 按优先级排序
    }

    private fun matchConfigs(element: ParadoxScriptMember, options: ParadoxMatchOptions?): List<CwtMemberConfig<*>> {
        ProgressManager.checkCanceled()
        val configContext = ParadoxConfigManager.getConfigContext(element)
        if (configContext == null) return emptyList()

        ProgressManager.checkCanceled()
        val contextConfigs = configContext.getConfigs(options)
        if (contextConfigs.isEmpty()) return emptyList()

        if (element is ParadoxScriptProperty && configContext.isDeclarationRoot()) {
            // 如果允许匹配声明的根对应的语法树节点，则这里直接返回所有作为上下文的规则，否则直接返回空列表
            if (ParadoxMatchService.forDeclarationRoot(options)) return contextConfigs
            return emptyList()
        }

        val configGroup = configContext.configGroup
        when (element) {
            // 匹配属性
            is ParadoxScriptProperty -> {
                val configs = contextConfigs.filterProperties()
                if (configs.isEmpty()) return emptyList() // 如果无结果，则直接返回空列表

                ProgressManager.checkCanceled()
                val keyExpression = element.propertyKey.let { ParadoxExpression.resolve(it, options) }
                val candidatesForKey = ParadoxMatchService.collectCandidates(configs) { config ->
                    val context = ParadoxScriptExpressionMatchContext(element, keyExpression, config.keyExpression, config, configGroup, options)
                    ParadoxExpressionMatchService.matchScriptExpression(context)
                }
                if (candidatesForKey.isEmpty()) {
                    // 如果无结果，则直接返回空列表
                    return emptyList()
                }
                val processedCandidatesForKey = ParadoxMatchService.processCandidates(candidatesForKey, options)
                if (processedCandidatesForKey.isEmpty()) {
                    // 如果无结果，则需要考虑回退（返回上一步已匹配得到的规则）
                    if (ParadoxMatchService.lenient(options)) return candidatesForKey.mapFast { it.value }
                    return emptyList()
                }
                val processedForKey = processedCandidatesForKey.mapFast { it.value }
                val optimizedForKey = ParadoxMatchService.optimize(processedForKey, element, keyExpression, options)
                if (optimizedForKey.isEmpty()) {
                    // 如果无结果，则需要考虑回退（返回上一步已匹配得到的规则）
                    if (ParadoxMatchService.lenient(options) || ParadoxMatchService.forExpression(options)) return processedForKey
                    return emptyList()
                }
                val resultForKey = optimizedForKey // 这里需要进行后续优化

                ProgressManager.checkCanceled()
                val valueExpression = element.propertyValue?.let { ParadoxExpression.resolve(it, options) }
                if (valueExpression == null) {
                    // 如果无法得到值表达式，则直接回退（返回上一步已匹配得到的规则）
                    return resultForKey
                }
                val candidates = ParadoxMatchService.collectCandidates(resultForKey) { config ->
                    val context = ParadoxScriptExpressionMatchContext(element, valueExpression, config.valueExpression, config, configGroup, options)
                    ParadoxExpressionMatchService.matchScriptExpression(context)
                }
                if (candidates.isEmpty()) {
                    // 如果无结果，则需要考虑回退（返回上一步已匹配得到的规则）
                    if (ParadoxMatchService.lenient(options) || ParadoxMatchService.forExpression(options)) return resultForKey
                    return emptyList()
                }
                val processedCandidates = ParadoxMatchService.processCandidates(candidates, options)
                if (processedCandidates.isEmpty()) {
                    // 如果无结果，则需要考虑回退（返回上一步已匹配得到的规则）
                    if (ParadoxMatchService.lenient(options) || ParadoxMatchService.forExpression(options)) return candidates.mapFast { it.value }
                    return emptyList()
                }
                val processed = processedCandidates.mapFast { it.value }
                val result = processed // 这里不需要也不应进行后续优化

                return result // 返回最终匹配的规则
            }
            // 匹配值（或者文件）
            else -> {
                val configs = contextConfigs.filterValues()
                if (configs.isEmpty()) return emptyList() // 如果无结果，则直接返回空列表

                ProgressManager.checkCanceled()
                val valueExpression = when (element) {
                    is ParadoxScriptFile -> ParadoxExpression.resolveBlock()
                    is ParadoxScriptValue -> ParadoxExpression.resolve(element, options)
                    else -> null
                }
                if (valueExpression == null) {
                    // 如果无法得到值表达式，则直接回退（返回上一步已匹配得到的规则）
                    return configs
                }
                val candidates = ParadoxMatchService.collectCandidates(configs) { config ->
                    val context = ParadoxScriptExpressionMatchContext(element, valueExpression, config.valueExpression, config, configGroup, options)
                    ParadoxExpressionMatchService.matchScriptExpression(context)
                }
                if (candidates.isEmpty()) {
                    // 如果无结果，则需要考虑回退（返回上一步已匹配得到的规则）
                    if (ParadoxMatchService.lenient(options)) return configs
                    return emptyList()
                }
                val processedCandidates = ParadoxMatchService.processCandidates(candidates, options)
                if (processedCandidates.isEmpty()) {
                    // 如果无结果，则需要考虑回退（返回上一步已匹配得到的规则）
                    if (ParadoxMatchService.lenient(options)) return candidates.mapFast { it.value }
                    return emptyList()
                }
                val processed = processedCandidates.mapFast { it.value }
                val optimized = ParadoxMatchService.optimize(processed, element, valueExpression, options)
                if (optimized.isEmpty()) {
                    // 如果无结果，则需要考虑回退（返回上一步已匹配得到的规则）
                    if (ParadoxMatchService.lenient(options)) return processed
                    return emptyList()
                }
                val result = optimized // 这里需要进行后续优化

                return result // 返回最终匹配的规则
            }
        }
    }

    fun resolveRowConfig(file: ParadoxCsvFile): CwtRowConfig? {
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.gameType
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val matchContext = CwtRowConfigMatchContext(configGroup, path)
        val rowConfig = ParadoxConfigMatchService.getMatchedRowConfig(matchContext)
        return rowConfig
    }

    fun getColumnConfig(element: ParadoxCsvColumn, rowConfig: CwtRowConfig): CwtPropertyConfig? {
        val rowElement = element.parent?.castOrNull<ParadoxCsvColumnContainer>() ?: return null
        if (rowConfig.skipLastRow && ParadoxCsvPsiService.isLastRow(rowElement)) return null // #314
        // if (rowConfig.skipLastColumn && ParadoxCsvPsiService.isLastColumn(element)) return null // #314 (not here, not such logic)
        val columnNames = ParadoxCsvPsiService.getColumnNames(rowElement).orNull() ?: return null
        val columnIndex = ParadoxCsvPsiService.getColumnIndex(element)
        return ParadoxConfigMatchService.getColumnConfig(rowConfig, columnNames, columnIndex)
    }

    fun isMatchedColumnConfig(column: ParadoxCsvColumn, columnConfig: CwtPropertyConfig): Boolean {
        if (ParadoxCsvPsiService.isHeaderColumn(column)) return true // header column -> always true

        val configExpression = columnConfig.valueConfig?.configExpression ?: return false
        val configGroup = columnConfig.configGroup
        val expression = ParadoxExpression.resolve(column)
        val context = ParadoxCsvExpressionMatchContext(column, expression, configExpression, configGroup)
        return ParadoxExpressionMatchService.matchCsvExpression(context).get()
    }

    fun getInBlockKeys(config: CwtMemberConfig<*>): Set<String> {
        return config.getOrPutUserData(Keys.inBlockKeys) { doGetInBlockKeys(config).optimized() }
    }

    private fun doGetInBlockKeys(config: CwtMemberConfig<*>): Set<@CaseInsensitive String> {
        val childConfigs = config.configs
        if (childConfigs.isNullOrEmpty()) return emptySet()
        val keys = CaseInsensitiveStringSet()
        childConfigs.forEachFast { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.add(it.key) }
        if (keys.isEmpty()) return emptySet()
        when (config) {
            is CwtPropertyConfig -> {
                val propertyConfig = config
                val configs1 = propertyConfig.parentConfig?.configs
                if (configs1.isNullOrEmpty()) return keys
                configs1.forEachFast f@{ c ->
                    val childConfigs1 = c.configs
                    if (childConfigs1.isNullOrEmpty()) return@f
                    if (c.isSamePointer(propertyConfig) || c !is CwtPropertyConfig || !c.key.equals(propertyConfig.key, true)) return@f
                    childConfigs1.forEachFast { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                }
            }
            is CwtValueConfig -> {
                val propertyConfig = config.propertyConfig
                val configs1 = propertyConfig?.parentConfig?.configs
                if (configs1.isNullOrEmpty()) return keys
                configs1.forEachFast f@{ c ->
                    val childConfigs1 = c.configs
                    if (childConfigs1.isNullOrEmpty()) return@f
                    if (c.isSamePointer(propertyConfig) || c !is CwtPropertyConfig || !c.key.equals(propertyConfig.key, true)) return@f
                    childConfigs1.forEachFast { if (it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                }
            }
        }
        return keys
    }

    private fun isInBlockKey(config: CwtPropertyConfig): Boolean {
        val gameType = config.configGroup.gameType
        if (config.keyExpression.type != CwtDataTypes.Constant) return false
        if (config.optionMetadata.cardinality?.isRequired() == false) return false
        if (ParadoxInlineScriptManager.isMatched(config.key, gameType)) return false // 排除是内联脚本用法的情况
        return true
    }

    fun getModifierCategories(value: String?, configGroup: CwtConfigGroup): Map<String, CwtModifierCategoryConfig> {
        if (value.isNullOrEmpty()) return emptyMap()
        val enumConfig = configGroup.enums["scripted_modifier_category"] ?: return emptyMap()
        return doGetModifierCategories(value, enumConfig)
    }

    private fun doGetModifierCategories(value: String, enumConfig: CwtEnumConfig): Map<String, CwtModifierCategoryConfig> {
        val keys = doGetModifierCategoriesOptionMetadata(value, enumConfig)
        if (keys.isNullOrEmpty()) return emptyMap()
        val modifierCategories = enumConfig.configGroup.modifierCategories
        val result = mutableMapOf<String, CwtModifierCategoryConfig>()
        for (key in keys) {
            val config = modifierCategories[key] ?: continue
            result[key] = config
        }
        return result
    }

    private fun doGetModifierCategoriesOptionMetadata(value: String, enumConfig: CwtEnumConfig): Set<String>? {
        val valueConfig = enumConfig.valueConfigMap[value] ?: return null
        return valueConfig.optionMetadata.modifierCategories
    }
}
