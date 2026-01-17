package icu.windea.pls.config.option

import icu.windea.pls.config.CwtApiStatus
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.FastMap
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.scope.ParadoxScopeContext

object CwtOptionDataProvider {
    // NOTE 2.1.1. 目前不作为 EP

    fun process(optionData: CwtOptionDataHolder, optionConfigs: List<CwtOptionMemberConfig<*>>) {
        if (optionData !is CwtOptionDataHolderBase) return
        if (optionConfigs.isEmpty()) return
        if (PlsStates.resolveForInternalConfigs.get() == true) {
            processForInternalConfigs(optionData, optionConfigs)
            return
        }
        for (config in optionConfigs) {
            when (config) {
                is CwtOptionConfig -> processOptionConfig(optionData, config)
                is CwtOptionValueConfig -> processOptionValueConfig(optionData, config)
            }
        }
    }

    private fun processForInternalConfigs(optionData: CwtOptionDataHolderBase, optionConfigs: List<CwtOptionMemberConfig<*>>) {
        // NOTE only reserved for internal configs
        optionData.optionConfigs = optionConfigs
    }

    private fun processOptionConfig(optionData: CwtOptionDataHolderBase, config: CwtOptionConfig) {
        val key = config.key
        when (key) {
            "api_status" -> {
                val v = config.getOptionValue()?.let { CwtApiStatus.get(it) } ?: return
                optionData.apiStatus = v
            }
            "cardinality" -> {
                val v = config.getOptionValue()?.let { CwtCardinalityExpression.resolve(it) } ?: return
                optionData.cardinality = v
            }
            "cardinality_min_define" -> {
                val v = config.getOptionValue() ?: return
                optionData.cardinalityMinDefine = v
            }
            "cardinality_max_define" -> {
                val v = config.getOptionValue() ?: return
                optionData.cardinalityMaxDefine = v
            }
            "predicate" -> {
                val v = resolvePredicate(config) ?: return
                optionData.predicate = v
            }
            "replace_scope", "replace_scopes" -> {
                val v = resolveReplaceScopes(config) ?: return
                optionData.replaceScopes = v
            }
            "push_scope" -> {
                val v = resolvePushScope(config) ?: return
                optionData.pushScope = v
            }
            "scope", "scopes" -> {
                val r = resolveSupportedScopes(config) ?: return
                optionData.supportedScopes = r
            }
            "type" -> {
                val v = config.getOptionValue() ?: return
                optionData.type = v
            }
            "hint" -> {
                val v = config.getOptionValue() ?: return
                optionData.hint = v
            }
            "event_type" -> {
                val v = config.getOptionValue() ?: return
                optionData.eventType = v
            }
            "context_key" -> {
                val v = config.getOptionValue() ?: return
                optionData.contextKey = v
            }
            "context_configs_type" -> {
                val v = config.getOptionValue() ?: return
                optionData.contextConfigsType = v
            }
            "group" -> {
                val v = config.getOptionValue() ?: return
                optionData.group = v
            }
            "type_key_filter" -> {
                val v = resolveTypeKeyFilter(config) ?: return
                optionData.typeKeyFilter = v
            }
            "type_key_regex" -> {
                val v = config.getOptionValue()?.toRegex(RegexOption.IGNORE_CASE) ?: return
                optionData.typeKeyRegex = v
            }
            "starts_with" -> {
                val v = config.getOptionValue() ?: return
                optionData.startsWith = v
            }
            "only_if_not" -> {
                val v = config.getOptionValueOrValues()?.optimized() ?: return
                optionData.onlyIfNot = v
            }
            "graph_related_types" -> {
                val v = config.getOptionValueOrValues()?.optimized() ?: return
                optionData.graphRelatedTypes = v
            }
            "severity" -> {
                val v = config.getOptionValue() ?: return
                optionData.severity = v
            }
            "file_extensions" -> {
                val v = config.getOptionValueOrValues()?.optimized() ?: return
                optionData.fileExtensions = v
            }
            "modifier_categories" -> {
                val v = config.getOptionValueOrValues()?.optimized() ?: return
                optionData.modifierCategories = v
            }
            "color_type" -> {
                val v = config.getOptionValue() ?: return
                optionData.colorType = v
            }
            "inject" -> {
                val v = config.getOptionValue() ?: return
                optionData.inject = v
            }
        }

        // 保存缺省的基数表达式
        run {
            if (optionData.cardinality != null || optionData !is CwtMemberConfig<*>) return@run
            val dataType = optionData.configExpression.type
            // 如果没有注明且类型是常量或枚举值，则推断为 `1..~1`
            if (dataType == CwtDataTypes.Constant || dataType == CwtDataTypes.EnumValue) {
                optionData.cardinality = CwtCardinalityExpression.resolve("1..~1")
            }
        }

        // 保存初始的作用域上下文
        run {
            val replaceScopes = optionData.replaceScopes
            val pushScope = optionData.pushScope
            val scopeContext = replaceScopes?.let { ParadoxScopeContext.get(it) }?.resolveNext(pushScope)
                ?: pushScope?.let { ParadoxScopeContext.get(it, it) }
            if (scopeContext == null) return@run
            optionData.scopeContext = scopeContext
        }
    }

    private fun processOptionValueConfig(optionData: CwtOptionDataHolderBase, config: CwtOptionValueConfig) {
        // NOTE 2.1.1 移除 `optional` 标志：CWTools 指引文档中并未提及，同时也是不必要的（默认即为可选）
        val flag = config.getOptionValue() ?: return
        when (flag) {
            "required" -> optionData.required = true
            "primary" -> optionData.primary = true
            "inherit" -> optionData.primary = true
            "tag" -> optionData.tag = true
            "case_insensitive" -> optionData.caseInsensitive = true
            "per_definition" -> optionData.perDefinition = true
        }
    }

    private fun resolvePredicate(config: CwtOptionConfig): Map<String, ReversibleValue<String>>? {
        val optionConfigs = config.optionConfigs ?: return null
        if (optionConfigs.isEmpty()) return emptyMap()
        val r = FastMap<String, ReversibleValue<String>>()
        for ((_, optionConfig) in optionConfigs.withIndex()) {
            if (optionConfig !is CwtOptionConfig) continue
            val k = optionConfig.key
            val o = optionConfig.separatorType == CwtSeparatorType.EQUAL
            val v = ReversibleValue(o, optionConfig.value)
            r[k] = v
        }
        return r.optimized()
    }

    private fun resolveReplaceScopes(config: CwtOptionConfig): Map<String, String>? {
        val optionConfigs = config.optionConfigs ?: return null
        if (optionConfigs.isEmpty()) return emptyMap()
        val r = FastMap<String, String>()
        for ((_, optionConfig) in optionConfigs.withIndex()) {
            if (optionConfig !is CwtOptionConfig) continue
            // ignore case for both system scopes and scopes (to lowercase)
            val k = optionConfig.key.lowercase()
            val v = optionConfig.getOptionValue()?.let { ParadoxScopeManager.getScopeId(it) } ?: continue
            r[k] = v
        }
        return r.optimized()
    }

    private fun resolvePushScope(config: CwtOptionConfig): String? {
        return config.getOptionValue()?.let { ParadoxScopeManager.getScopeId(it) }
    }

    private fun resolveSupportedScopes(config: CwtOptionConfig): Set<String>? {
        val values = config.getOptionValueOrValues()?.orNull() ?: return null
        val r = values.mapTo(FastSet()) { ParadoxScopeManager.getScopeId(it) }
        return r.optimized()
    }

    private fun resolveTypeKeyFilter(config: CwtOptionConfig): ReversibleValue<Set<@CaseInsensitive String>>? {
        val values = config.getOptionValueOrValues() ?: return null
        val operator = config.separatorType == CwtSeparatorType.EQUAL
        val set = caseInsensitiveStringSet().apply { addAll(values) } // 忽略大小写
        val r = ReversibleValue(operator, set.optimized())
        return r
    }
}
