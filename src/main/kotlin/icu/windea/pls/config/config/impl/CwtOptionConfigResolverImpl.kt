package icu.windea.pls.config.config.impl

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.config.util.CwtConfigResolverUtil
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.forCwtSeparatorType
import icu.windea.pls.model.forCwtType

internal class CwtOptionConfigResolverImpl : CwtOptionConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()
    private val cache = CacheBuilder().build<String, CwtOptionConfig>()

    override fun resolve(element: CwtOption): CwtOptionConfig? {
        val optionValueElement = element.optionValue
        if (optionValueElement == null) {
            logger.warn("Missing option value, skipped.".withLocationPrefix(element))
            return null
        }
        val key = element.name
        val value = optionValueElement.value
        val valueType: CwtType = optionValueElement.type
        val separatorType = element.separatorType
        val optionConfigs = CwtConfigResolverUtil.getOptionConfigsInOption(optionValueElement)
        return CwtOptionConfig.create(key, value, valueType, separatorType, optionConfigs)
    }

    override fun create(
        key: String,
        value: String,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        optionConfigs: List<CwtOptionMemberConfig<*>>?,
    ): CwtOptionConfig = doCreate(key, value, valueType, separatorType, optionConfigs)

    private fun doCreate(
        key: String,
        value: String,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        optionConfigs: List<CwtOptionMemberConfig<*>>?
    ): CwtOptionConfig {
        // use cache if possible to optimize memory
        if (optionConfigs.isNullOrEmpty()) {
            val cacheKey = "${valueType.ordinal}#${separatorType.ordinal}#${key}#${value}"
            return cache.get(cacheKey) {
                CwtOptionConfigImpl(key, value, valueType, separatorType, optionConfigs)
            }
        }
        return CwtOptionConfigImpl(key, value, valueType, separatorType, optionConfigs)
    }
}

// 12 + 3 * 4 + 2 * 1 = 26 -> 32
private class CwtOptionConfigImpl(
    key: String,
    value: String,
    valueType: CwtType,
    separatorType: CwtSeparatorType,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?,
) : CwtOptionConfig {
    override val key = key.optimized() // optimized to optimize memory
    override val value = value.optimized() // optimized to optimize memory

    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimize memory
    override val valueType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())

    private val separatorTypeId = separatorType.optimized(OptimizerRegistry.forCwtSeparatorType()) // optimize memory
    override val separatorType get() = separatorTypeId.deoptimized(OptimizerRegistry.forCwtSeparatorType())

    override fun toString() = "(option) $key $separatorType $value"
}
