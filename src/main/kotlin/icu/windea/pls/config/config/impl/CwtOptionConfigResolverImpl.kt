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
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.model.forCwtSeparatorType
import icu.windea.pls.model.forCwtType
import java.util.*

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
    ): CwtOptionConfig {
        val noOptionConfigs = optionConfigs.isNullOrEmpty()
        if (noOptionConfigs) {
            // use (strong) cache if not nested to optimize memory
            val cacheKey = "${valueType.ordinal}#${separatorType.ordinal}#${key}#${value}"
            return cache.get(cacheKey) {
                CwtOptionConfigImpl(key, value, valueType, separatorType)
            }
        }
        return CwtOptionConfigImplNested(key, separatorType, optionConfigs)
    }
}

private const val blockValue = PlsStringConstants.blockFolder
private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

private abstract class CwtOptionConfigBase : CwtOptionConfig {
    override fun equals(other: Any?) = this === other || other is CwtOptionConfig
        && key == other.key && value == other.value
        && separatorType == other.separatorType && optionConfigs == other.optionConfigs

    override fun hashCode() = Objects.hash(key, value, separatorType, optionConfigs)
    override fun toString() = "(option) $key $separatorType $value"
}

private abstract class CwtOptionConfigImplBase(
    key: String,
    separatorType: CwtSeparatorType,
) : CwtOptionConfigBase() {
    private val separatorTypeId = separatorType.optimized(OptimizerRegistry.forCwtSeparatorType()) // optimized to optimize memory

    override val key: String = key.optimized() // optimized to optimize memory
    override val separatorType: CwtSeparatorType get() = separatorTypeId.deoptimized(OptimizerRegistry.forCwtSeparatorType())
}

private class CwtOptionConfigImpl(
    key: String,
    value: String,
    valueType: CwtType,
    separatorType: CwtSeparatorType,
) : CwtOptionConfigImplBase(key, separatorType) {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val value: String = value.optimized() // optimized to optimize memory
    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val optionConfigs: List<CwtOptionMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null
}

private class CwtOptionConfigImplNested(
    key: String,
    separatorType: CwtSeparatorType,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?,
) : CwtOptionConfigImplBase(key, separatorType) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block
}
