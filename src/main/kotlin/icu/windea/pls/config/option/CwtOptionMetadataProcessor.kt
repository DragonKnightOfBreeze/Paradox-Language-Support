package icu.windea.pls.config.option

import icu.windea.pls.base.ChronicleCapacities
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.CwtConfigApiStatus
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.optimizedPathExtension
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.CaseInsensitiveStringSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.type.CwtSeparatorType

object CwtOptionMetadataProcessor {
    @Optimized
    fun process(optionMetadata: CwtOptionMetadata, optionConfigs: List<CwtOptionMemberConfig<*>>, configGroup: CwtConfigGroup) {
        if (optionMetadata !is CwtOptionMetadataBase) return
        if (optionConfigs.isEmpty()) return
        val skipProcessing = ChronicleThreadContext.skipProcessingOptionMetadata.get() == true
        val keepOptionConfigs = skipProcessing || ChronicleCapacities.keepOptionConfigs()
        if (keepOptionConfigs) {
            optionMetadata.optionConfigs = optionConfigs.optimized() // optimized to optimize memory
        }
        if (skipProcessing) {
            return
        }
        optionConfigs.forEachFast { config ->
            when (config) {
                is CwtOptionConfig -> processOptionConfig(optionMetadata, config, configGroup)
                is CwtOptionValueConfig -> processOptionValueConfig(optionMetadata, config, configGroup)
            }
        }
    }

    private fun processOptionConfig(optionMetadata: CwtOptionMetadataBase, config: CwtOptionConfig, configGroup: CwtConfigGroup) {
        val key = config.key
        when (key) {
            "api_status" -> {
                val v = config.getOptionValue()?.let { CwtConfigApiStatus.get(it) } ?: return
                optionMetadata.apiStatus = v
            }
            "cardinality" -> {
                val v = config.getOptionValue()?.let { CwtCardinalityExpression.resolve(it) } ?: return
                optionMetadata.cardinality = v
            }
            "cardinality_min_define" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.cardinalityMinDefine = v
            }
            "cardinality_max_define" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.cardinalityMaxDefine = v
            }
            "predicate" -> {
                val v = resolvePredicate(config) ?: return
                configGroup.initializer.attribute.usePredicateBasedMatch = true // set attribute
                optionMetadata.predicate = v
            }
            "push_scope" -> {
                val v = resolvePushScope(config) ?: return
                optionMetadata.pushScope = v
            }
            "replace_scope", "replace_scopes" -> {
                val v = resolveReplaceScopes(config) ?: return
                optionMetadata.replaceScopes = v
            }
            "scope", "scopes" -> {
                val r = resolveSupportedScopes(config) ?: return
                optionMetadata.supportedScopes = r
            }
            "type" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.type = v
            }
            "hint" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.hint = v
            }
            "event_type" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.eventType = v
            }
            "context_key" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.contextKey = v
            }
            "context_configs_type" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.contextConfigsType = v
            }
            "group" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.group = v
            }
            "type_key_filter" -> {
                val v = resolveTypeKeyFilter(config) ?: return
                optionMetadata.typeKeyFilter = v
            }
            "type_key_regex" -> {
                val v = config.getOptionValue()?.toRegex(RegexOption.IGNORE_CASE) ?: return
                optionMetadata.typeKeyRegex = v
            }
            "starts_with" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.startsWith = v
            }
            "only_if_not" -> {
                val v = config.getOptionValueOrValues()?.optimized() ?: return
                optionMetadata.onlyIfNot = v
            }
            "graph_related_types" -> {
                val v = config.getOptionValueOrValues()?.optimized() ?: return
                optionMetadata.graphRelatedTypes = v
            }
            "declare_complex_enum" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.declareComplexEnum = v
            }
            "severity" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.severity = v
            }
            "file_extensions" -> {
                val v = config.getOptionValueOrValues()?.mapTo(mutableSetOf()) { it.optimizedPathExtension() }?.optimized() ?: return
                optionMetadata.fileExtensions = v
            }
            "modifier_categories" -> {
                val v = config.getOptionValueOrValues()?.optimized() ?: return
                optionMetadata.modifierCategories = v
            }
            "color_type" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.colorType = v
            }
            "inject" -> {
                val v = config.getOptionValue() ?: return
                optionMetadata.inject = v
            }
        }

        // 保存缺省的基数表达式
        run {
            if (optionMetadata.cardinality != null || optionMetadata !is CwtMemberConfig<*>) return@run
            val dataType = optionMetadata.configExpression.type
            // 如果没有注明且类型是常量或枚举值，则推断为 `1..~1`
            if (dataType == CwtDataTypes.Constant || dataType == CwtDataTypes.EnumValue) {
                optionMetadata.cardinality = CwtCardinalityExpression.resolve("1..~1")
            }
        }

        // 保存初始的作用域上下文
        run {
            val replaceScopes = optionMetadata.replaceScopes
            val pushScope = optionMetadata.pushScope
            val scopeContext = replaceScopes?.let { ParadoxScopeContext.resolve(it) }?.resolveNext(pushScope)
                ?: pushScope?.let { ParadoxScopeContext.resolve(it, it) }
            if (scopeContext == null) return@run
            optionMetadata.scopeContext = scopeContext
        }
    }

    @Suppress("unused")
    private fun processOptionValueConfig(optionMetadata: CwtOptionMetadataBase, config: CwtOptionValueConfig, configGroup: CwtConfigGroup) {
        // NOTE 2.1.1 移除 `optional` 标志：CWTools 指引文档中并未提及，同时也是不必要的（默认即为可选）
        val flag = config.getOptionValue() ?: return
        when (flag) {
            "required" -> optionMetadata.required = true
            "primary" -> optionMetadata.primary = true
            "inherit" -> optionMetadata.primary = true
            "tag" -> optionMetadata.tag = true
            "case_insensitive" -> optionMetadata.caseInsensitive = true
            "per_definition" -> optionMetadata.perDefinition = true
        }
    }

    private fun resolvePredicate(config: CwtOptionConfig): Map<String, ReversibleValue<String>>? {
        val optionConfigs = config.optionConfigs ?: return null
        if (optionConfigs.isEmpty()) return emptyMap()
        val r = mutableMapOf<String, ReversibleValue<String>>()
        optionConfigs.forEachFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            val k = optionConfig.key
            val o = optionConfig.separatorType == CwtSeparatorType.Equal
            val v = ReversibleValue(optionConfig.value, o)
            r[k] = v
        }
        return r.optimized()
    }

    private fun resolveReplaceScopes(config: CwtOptionConfig): Map<String, String>? {
        val optionConfigs = config.optionConfigs ?: return null
        if (optionConfigs.isEmpty()) return emptyMap()
        val r = mutableMapOf<String, String>()
        optionConfigs.forEachFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            // ignore case for both system scopes and scopes (to lowercase)
            val k = optionConfig.key.lowercase()
            val v = optionConfig.getOptionValue()?.let { ParadoxScope.getId(it) } ?: return@f
            r[k] = v
        }
        return r.optimized()
    }

    private fun resolvePushScope(config: CwtOptionConfig): String? {
        return config.getOptionValue()?.let { ParadoxScope.getId(it) }
    }

    private fun resolveSupportedScopes(config: CwtOptionConfig): Set<String>? {
        val values = config.getOptionValueOrValues()?.orNull() ?: return null
        val r = values.mapTo(mutableSetOf()) { ParadoxScope.getId(it) }
        return r.optimized()
    }

    private fun resolveTypeKeyFilter(config: CwtOptionConfig): ReversibleValue<Set<@CaseInsensitive String>>? {
        val values = config.getOptionValueOrValues() ?: return null
        val value = CaseInsensitiveStringSet().apply { addAll(values) } // 忽略大小写
        val operator = config.separatorType == CwtSeparatorType.Equal
        val r = ReversibleValue(value.optimized(), operator)
        return r
    }
}
