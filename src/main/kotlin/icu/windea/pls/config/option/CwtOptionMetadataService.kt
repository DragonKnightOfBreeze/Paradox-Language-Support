package icu.windea.pls.config.option

import icu.windea.pls.base.ChronicleCapacities
import icu.windea.pls.config.CwtConfigApiStatus
import icu.windea.pls.config.CwtConfigThreadContext
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.scopes.CwtOptionMetadataResolutionScope
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.hasState
import icu.windea.pls.core.optimized
import icu.windea.pls.model.scope.ParadoxScopeContext

@Optimized
object CwtOptionMetadataService : CwtOptionMetadataResolutionScope {
    fun process(optionMetadata: CwtOptionMetadata, optionConfigs: List<CwtOptionMemberConfig<*>>, configGroup: CwtConfigGroup) {
        if (optionMetadata !is CwtOptionMetadataBase) return
        processMain(optionMetadata, optionConfigs, configGroup)
        processFinal(optionMetadata)
    }

    private fun processMain(optionMetadata: CwtOptionMetadataBase, optionConfigs: List<CwtOptionMemberConfig<*>>, configGroup: CwtConfigGroup) {
        if (optionConfigs.isEmpty()) return
        val skipProcessing = CwtConfigThreadContext.skipProcessingOptionMetadata.hasState()
        val keepOptionConfigs = skipProcessing || ChronicleCapacities.keepOptionConfigs()
        if (keepOptionConfigs) {
            optionMetadata.optionConfigs = optionConfigs.optimized() // ensure optimized
        }
        if (skipProcessing) {
            return
        }
        optionConfigs.forEachFast { config ->
            when (config) {
                is CwtOptionConfig -> processOptionConfig(optionMetadata, config, configGroup)
                is CwtOptionValueConfig -> processOptionValueConfig(optionMetadata, config)
            }
        }
    }

    private fun processOptionConfig(optionMetadata: CwtOptionMetadataBase, config: CwtOptionConfig, configGroup: CwtConfigGroup) {
        val key = config.key
        when (key) {
            "api_status" -> {
                val v = config.resolveValue()?.let { CwtConfigApiStatus.get(it) } ?: return
                optionMetadata.apiStatus = v
            }
            "cardinality" -> {
                val v = config.resolveValue()?.let { CwtCardinalityExpression.resolve(it) } ?: return
                optionMetadata.cardinality = v
            }
            "cardinality_min_define" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.cardinalityMinDefine = v
            }
            "cardinality_max_define" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.cardinalityMaxDefine = v
            }
            "predicate" -> {
                val v = config.resolvePredicate() ?: return
                configGroup.initializer.attributes.usePredicateBasedMatch = true // set attribute
                optionMetadata.predicate = v
            }
            "push_scope" -> {
                val v = config.resolvePushScope() ?: return
                optionMetadata.pushScope = v
            }
            "replace_scope", "replace_scopes" -> {
                val v = config.resolveReplaceScopes() ?: return
                optionMetadata.replaceScopes = v
            }
            "scope", "scopes" -> {
                val r = config.resolveSupportedScopes() ?: return
                optionMetadata.supportedScopes = r
            }
            "type" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.type = v
            }
            "hint" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.hint = v
            }
            "event_type" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.eventType = v
            }
            "context_key" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.contextKey = v
            }
            "context_configs_type" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.contextConfigsType = v
            }
            "group" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.group = v
            }
            "type_key_filter" -> {
                val v = config.resolveTypeKeyFilter() ?: return
                optionMetadata.typeKeyFilter = v
            }
            "type_key_regex" -> {
                val v = config.resolveTypeKeyRegex() ?: return
                optionMetadata.typeKeyRegex = v
            }
            "starts_with" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.startsWith = v
            }
            "only_if_not" -> {
                val v = config.resolveValueOrValues() ?: return
                optionMetadata.onlyIfNot = v
            }
            "graph_related_types" -> {
                val v = config.resolveValueOrValues() ?: return
                optionMetadata.graphRelatedTypes = v
            }
            "declare_complex_enum" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.declareComplexEnum = v
            }
            "severity" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.severity = v
            }
            "file_extensions" -> {
                val v = config.resolveFileExtensions() ?: return
                optionMetadata.fileExtensions = v
            }
            "modifier_categories" -> {
                val v = config.resolveValueOrValues() ?: return
                optionMetadata.modifierCategories = v
            }
            "color_type" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.colorType = v
            }
            "inject" -> {
                val v = config.resolveValue() ?: return
                optionMetadata.inject = v
            }
        }
    }

    private fun processOptionValueConfig(optionMetadata: CwtOptionMetadataBase, config: CwtOptionValueConfig) {
        // NOTE 2.1.1 移除 `optional` 标志：CWTools 指引文档中并未提及，同时也是不必要的（默认即为可选）
        val flag = config.resolveValue() ?: return
        when (flag) {
            "required" -> optionMetadata.required = true
            "primary" -> optionMetadata.primary = true
            "inherit" -> optionMetadata.primary = true
            "tag" -> optionMetadata.tag = true
            "case_insensitive" -> optionMetadata.caseInsensitive = true
            "per_definition" -> optionMetadata.perDefinition = true
        }
    }

    private fun processFinal(optionMetadata: CwtOptionMetadataBase) {
        // 保存缺省的基数表达式
        processFinalForCardinality(optionMetadata)
        // 保存初始的作用域上下文
        processFinalForScopeContext(optionMetadata)
    }

    private fun processFinalForCardinality(optionMetadata: CwtOptionMetadataBase) {
        if (optionMetadata.cardinality != null || optionMetadata !is CwtMemberConfig<*>) return
        val dataType = optionMetadata.configExpression.type
        // 如果没有注明且类型是常量或枚举值，则推断为 `1..~1`
        if (dataType == CwtDataTypes.Constant || dataType == CwtDataTypes.EnumValue) {
            optionMetadata.cardinality = CwtCardinalityExpression.resolve("1..~1")
        }
    }

    private fun processFinalForScopeContext(optionMetadata: CwtOptionMetadataBase) {
        val replaceScopes = optionMetadata.replaceScopes
        val pushScope = optionMetadata.pushScope
        val scopeContext = replaceScopes?.let { ParadoxScopeContext.resolve(it) }?.resolveNext(pushScope)
            ?: pushScope?.let { ParadoxScopeContext.resolve(it, it) }
        if (scopeContext == null) return
        optionMetadata.scopeContext = scopeContext
    }
}
