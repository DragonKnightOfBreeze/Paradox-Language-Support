package icu.windea.pls.config.scopes

import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.CaseInsensitiveStringSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.type.CwtSeparatorType

@Suppress("unused")
@Optimized
interface CwtOptionMetadataResolutionScope : CwtFilePathResolutionScope {
    fun CwtOptionMemberConfig<*>.resolveValue(): String? {
        return stringValue
    }

    fun CwtOptionMemberConfig<*>.resolveValues(): Set<String>? {
        val optionConfigs = optionConfigs
        if (optionConfigs == null) return null
        if (optionConfigs.isEmpty()) return emptySet()
        val result = mutableSetOf<String>()
        optionConfigs.forEachFast { optionConfig ->
            optionConfig.castOrNull<CwtOptionValueConfig>()?.stringValue?.let { result += it }
        }
        return result.optimized()
    }

    fun CwtOptionMemberConfig<*>.resolveValueOrValues(): Set<String>? {
        val value = resolveValue()
        if (value != null) return setOf(value)
        return resolveValues().orEmpty()
    }

    fun CwtOptionConfig.resolvePredicate(): Map<String, ReversibleValue<String>>? {
        val optionConfigs = optionConfigs ?: return null
        if (optionConfigs.isEmpty()) return emptyMap()
        val r = mutableMapOf<String, ReversibleValue<String>>()
        optionConfigs.forEachFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            val k = optionConfig.key
            val o = optionConfig.separatorType == CwtSeparatorType.Equal
            val v = ReversibleValue(optionConfig.value, o)
            r[k] = v
        }
        return r.optimized() // ensure optimized
    }

    fun CwtOptionConfig.resolveReplaceScopes(): Map<String, String>? {
        val optionConfigs = optionConfigs ?: return null
        if (optionConfigs.isEmpty()) return emptyMap()
        val r = mutableMapOf<String, String>()
        optionConfigs.forEachFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            // ignore case for both system scopes and scopes (to lowercase)
            val k = optionConfig.key.lowercase()
            val v = optionConfig.resolveValue()?.let { ParadoxScope.getId(it) } ?: return@f
            r[k] = v
        }
        return r.optimized() // ensure optimized
    }

    fun CwtOptionConfig.resolvePushScope(): String? {
        return resolveValue()?.let { ParadoxScope.getId(it) }
    }

    fun CwtOptionConfig.resolveSupportedScopes(): Set<String>? {
        val values = resolveValueOrValues()?.orNull() ?: return null
        val r = values.mapTo(mutableSetOf()) { ParadoxScope.getId(it) }
        return r.optimized() // ensure optimized
    }

    fun CwtOptionConfig.resolveTypeKeyFilter(): ReversibleValue<Set<@CaseInsensitive String>>? {
        val values = resolveValueOrValues() ?: return null
        val value = CaseInsensitiveStringSet().apply { addAll(values) } // 忽略大小写
        val operator = separatorType == CwtSeparatorType.Equal
        return ReversibleValue(value.optimized(), operator) // ensure optimized
    }

    fun CwtOptionConfig.resolveTypeKeyRegex(): Regex? {
        return resolveValue()?.toRegex(RegexOption.IGNORE_CASE)
    }

    fun CwtOptionConfig.resolveFileExtensions(): Set<String>? {
        val values = resolveValueOrValues()?.orNull() ?: return null
        val r = values.mapNotNullTo(mutableSetOf()) { it.resolvePathExtension() }
        return r.optimized() // ensure optimized
    }

    companion object INSTANCE : CwtOptionMetadataResolutionScope
}
