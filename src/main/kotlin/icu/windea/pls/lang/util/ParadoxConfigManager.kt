package icu.windea.pls.lang.util

import com.google.common.collect.ImmutableList
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberType
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.select.selectConfigScope
import icu.windea.pls.config.util.CwtConfigKeyManager
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.buildImmutableList
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.collections.flatMapFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.ComputedModificationTracker
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.values.SoftValue
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.ep.resolve.config.CwtOverriddenConfigProvider
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.match.ParadoxMatchOccurrence
import icu.windea.pls.lang.match.ParadoxMatchOccurrenceService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.match.toHashString
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.overriddenProvider
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptValue
import java.util.concurrent.ConcurrentMap

@Optimized
object ParadoxConfigManager {
    object Keys : KeyRegistry() {
        val cachedConfigContext by registerKey<CachedValue<CwtConfigContext>>(Keys)
        val cachedConfigsCache by registerKey<CachedValue<SoftValue<ConcurrentMap<String, List<CwtMemberConfig<*>>>>>>(Keys)
        val cachedChildOccurrencesCache by registerKey<CachedValue<SoftValue<ConcurrentMap<String, Map<CwtDataExpression, ParadoxMatchOccurrence>>>>>(Keys)
        val cachedRowConfig by registerKey<CachedValue<CwtRowConfig>>(Keys)
        val inBlockKeys by registerKey<Set<String>>(this)
    }

    /**
     * 得到 [element] 对应的脚本成员（[ParadoxScriptMember]）的规则上下文。
     */
    fun getConfigContext(element: PsiElement): CwtConfigContext? {
        if (element.language !== ParadoxScriptLanguage) return null
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return null
        return getConfigContextFromCache(memberElement)
    }

    private fun getConfigContextFromCache(element: ParadoxScriptMember): CwtConfigContext? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigContext) {
            ProgressManager.checkCanceled()
            val value = ParadoxConfigService.getConfigContext(element)
            value.withDependencyItems(element, ChronicleModificationTrackers.ConfigResolution)
        }
    }

    /**
     * 得到 [element] 对应的脚本成员（[ParadoxScriptMember]）的一组作为上下文的成员规则。如果当前位置不存在规则上下文，则返回空列表。
     */
    fun getContextConfigs(element: PsiElement, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        if (element.language !== ParadoxScriptLanguage) return emptyList()
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        val configContext = getConfigContextFromCache(memberElement) ?: return emptyList()
        return configContext.getConfigs(options)
    }

    /**
     * 得到 [element] 对应的脚本成员（[ParadoxScriptMember]）的一组匹配的成员规则。
     */
    fun getConfigs(element: PsiElement, options: ParadoxMatchOptions? = null): List<CwtMemberConfig<*>> {
        if (element.language !== ParadoxScriptLanguage) return emptyList()
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyList()
        ProgressManager.checkCanceled()
        val cacheKey = options.toHashString().optimized() // optimized to optimize memory
        val cache = getConfigsCacheFromCache(memberElement).dereference()
        return cache.getOrPut(cacheKey) { ParadoxConfigService.getConfigs(memberElement, options).optimized() }
    }

    private fun getConfigsCacheFromCache(element: ParadoxScriptMember): SoftValue<ConcurrentMap<String, List<CwtMemberConfig<*>>>> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedConfigsCache) {
            // use soft referenced concurrent map to optimize more memory
            val value = SoftValue.ofConcurrentMap<String, List<CwtMemberConfig<*>>>()
            value.withDependencyItems(element, ChronicleModificationTrackers.ConfigResolution)
        }
    }

    /**
     * 得到 [element] 对应的脚本成员（[ParadoxScriptMember]）的作为值的子句中的子成员的出现次数信息。
     */
    fun getChildOccurrences(element: PsiElement, configs: List<CwtMemberConfig<*>>): Map<CwtDataExpression, ParadoxMatchOccurrence> {
        if (element.language !== ParadoxScriptLanguage) return emptyMap()
        val memberElement = element.parentOfType<ParadoxScriptMember>(withSelf = true) ?: return emptyMap()
        if (configs.isEmpty()) return emptyMap()
        val childConfigs = configs.flatMapFast { it.configs.orEmpty() }
        if (childConfigs.isEmpty()) return emptyMap()
        ProgressManager.checkCanceled()
        val cacheKey = CwtConfigKeyManager.getIdentifierKey(childConfigs, "\u0000", 1).optimized() // optimized to optimize memory
        val cache = getChildOccurrencesCacheFromCache(memberElement).dereference()
        return cache.getOrPut(cacheKey) { ParadoxMatchOccurrenceService.getChildOccurrences(memberElement, configs).optimized() }
    }

    private fun getChildOccurrencesCacheFromCache(element: ParadoxScriptMember): SoftValue<ConcurrentMap<String, Map<CwtDataExpression, ParadoxMatchOccurrence>>> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedChildOccurrencesCache) {
            // use soft referenced concurrent map to optimize more memory
            val value = SoftValue.ofConcurrentMap<String, Map<CwtDataExpression, ParadoxMatchOccurrence>>()
            value.withDependencyItems(element, ChronicleModificationTrackers.ConfigResolution)
        }
    }

    /**
     * 得到 [element] 对应的 CSV 文件（[ParadoxCsvFile]）的行规则。
     */
    fun getRowConfig(element: PsiElement): CwtRowConfig? {
        if(element.language !== ParadoxCsvLanguage) return null
        val file = element.containingFile?.castOrNull<ParadoxCsvFile>() ?: return null
        // from cache
        return getRowConfigFromCache(file)
    }

    private fun getRowConfigFromCache(file: ParadoxCsvFile): CwtRowConfig? {
        // when the file content changes, the cache here does not need to be refreshed
        return CachedValuesManager.getCachedValue(file, Keys.cachedRowConfig) {
            val value = ParadoxConfigService.resolveRowConfig(file)
            value.withDependencyItems(ComputedModificationTracker { file.fileInfo })
        }
    }

    fun getColumnConfig(element: ParadoxCsvColumn, rowConfig: CwtRowConfig): CwtPropertyConfig? {
        return ParadoxConfigService.getColumnConfig(element, rowConfig)
    }

    fun getColumnConfig(element: ParadoxCsvColumn): CwtPropertyConfig? {
        val rowConfig = getRowConfig(element) ?: return null
        return getColumnConfig(element, rowConfig)
    }

    fun isMatchedColumnConfig(column: ParadoxCsvColumn, columnConfig: CwtPropertyConfig): Boolean {
        return ParadoxConfigService.isMatchedColumnConfig(column, columnConfig)
    }

    fun getExpectedConfigs(element: ParadoxScriptExpressionElement, configContext: CwtConfigContext, parentConfigContext: CwtConfigContext?): List<CwtMemberConfig<*>> {
        // 优先使用重载后的规则
        val result = mutableListOf<CwtMemberConfig<*>>()
        when (element) {
            is ParadoxScriptPropertyKey -> {
                if (parentConfigContext != null) {
                    // flatten and collect context configs from parent context configs
                    val parentContextConfigs = parentConfigContext.getConfigs()
                    parentContextConfigs.forEachFast { parentContextConfig ->
                        val contextConfigs = parentContextConfig.configs
                        collectConfigsWithOverridden(element, contextConfigs, result, CwtMemberType.PROPERTY)
                    }
                } else {
                    // collect from context configs
                    val contextConfigs = configContext.getConfigs()
                    collectConfigsWithOverridden(element, contextConfigs, result, CwtMemberType.PROPERTY)
                }
            }
            is ParadoxScriptValue -> {
                // collect from context configs
                val contextConfigs = configContext.getConfigs()
                collectConfigsWithOverridden(element, contextConfigs, result, CwtMemberType.VALUE)
            }
        }
        if (result.isEmpty()) return emptyList()
        return result
    }

    fun getExpectedConfigs(columnConfig: CwtPropertyConfig): List<CwtValueConfig> {
        val valueConfig = columnConfig.valueConfig ?: return emptyList()
        return listOf(valueConfig)
    }

    fun collectConfigsWithOverridden(element: PsiElement, configs: List<CwtMemberConfig<*>>?, result: MutableList<CwtMemberConfig<*>>, type: CwtMemberType? = null) {
        if (configs == null) return
        configs.forEachFast { collectConfigsWithOverridden(element, it, result, type) }
    }

    fun collectConfigsWithOverridden(element: PsiElement, config: CwtMemberConfig<*>?, result: MutableList<CwtMemberConfig<*>>, type: CwtMemberType? = null) {
        if (config == null) return
        if (type != null && type != config.memberType) return
        val overriddenConfigs = ParadoxConfigService.getOverriddenConfigs(element, config)
        if (overriddenConfigs.isNotEmpty()) {
            result.addAll(overriddenConfigs)
        } else {
            result.add(config)
        }
    }

    fun checkExtendedConfig(key: String, element: PsiElement, expectedConfig: CwtMemberConfig<*>): Boolean {
        val configGroup = expectedConfig.configGroup
        return ProcessorScope.anyFrom({ expectedConfig.expandConfigExpression { process(it) } }) { checkExtendedConfig(key, it, element, configGroup) }
    }

    fun checkExtendedConfig(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Boolean {
        if (expectedConfigs.isEmpty()) return false
        val value = element.value
        val configGroup = expectedConfigs.first().configGroup
        return ProcessorScope.anyFrom({ expectedConfigs.expandConfigExpression { process(it) } }) { checkExtendedConfig(value, it, element, configGroup) }
    }

    fun checkExtendedConfig(key: String, configExpression: CwtDataExpression, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        // NOTE 3.0.2 only for definition references and inline script expressions atm
        if (configExpression.type in CwtDataTypeSets.DefinitionAware) {
            val definitionType = configExpression.metadata.value ?: return false
            if (checkExtendedConfig(key, definitionType, element, configGroup)) return true
        }
        if (configExpression == ParadoxInlineScriptManager.inlineScriptPathExpression) {
            val config = configGroup.extendedInlineScripts.findByPattern(key, element, configGroup)
            if (config != null) return true
        }
        return false
    }

    fun checkExtendedConfig(key: String, definitionType: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
        val configs = configGroup.extendedDefinitions.findByPattern(key, element, configGroup).orEmpty()
        val config = configs.findFast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionType) }
        if (config != null) return true
        if (definitionType == ParadoxDefinitionTypes.gameRule) {
            val config = configGroup.extendedGameRules.findByPattern(key, element, configGroup)
            if (config != null) return true
        }
        if (definitionType == ParadoxDefinitionTypes.onAction) {
            val config = configGroup.extendedOnActions.findByPattern(key, element, configGroup)
            if (config != null) return true
        }
        return false
    }

    fun getOverriddenProvider(configs: List<CwtMemberConfig<*>>): CwtOverriddenConfigProvider? {
        configs.forEachFast { c1 ->
            c1.overriddenProvider?.let { return it }
            val pc1 = selectConfigScope { c1.asValue()?.propertyConfig }
            pc1?.overriddenProvider?.let { return it }
            val cs = selectConfigScope { (pc1 ?: c1).walkUp() }
            cs.forEach { c2 -> c2.overriddenProvider?.let { return it } }
        }
        return null
    }

    fun getSubtypes(subtypeConfigs: List<CwtSubtypeConfig>): List<String> {
        // optimize: build immutable list here
        val size = subtypeConfigs.size
        return buildImmutableList(size) {
            subtypeConfigs[it].name
        }
    }

    fun getTypes(type: String?, subtypeConfigs: List<CwtSubtypeConfig>): List<String> {
        // optimize: build immutable list here
        if (type == null) return ImmutableList.of()
        val size = subtypeConfigs.size
        return buildImmutableList(size + 1) {
            if (it == 0) type else subtypeConfigs[it - 1].name
        }
    }

    fun getTypeText(type: String?, subtypeConfigs: List<CwtSubtypeConfig>): String {
        if (type == null) return ""
        if (subtypeConfigs.isEmpty()) return type
        return buildString {
            append(type)
            subtypeConfigs.forEachFast { append(", ").append(it.name) }
        }
    }
}
