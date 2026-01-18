@file:Optimized

package icu.windea.pls.config.config.impl

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.util.elementType
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.model.forCwtSeparatorType
import icu.windea.pls.model.forCwtType
import java.util.*

internal class CwtOptionConfigResolverImpl : CwtOptionConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()
    private val cache = CacheBuilder().build<String, CwtOptionConfig>()

    override fun resolve(element: CwtOption): CwtOptionConfig? {
        // - use `EmptyPointer` since visit PSI is not needed
        // - 2.1.1 reduce PSI iterations to optimize performance

        var keyElement: CwtOptionKey? = null
        var valueElement: CwtValue? = null
        var separatorType = CwtSeparatorType.EQUAL
        element.forEachChild { e ->
            when {
                e is CwtOptionKey -> keyElement = e
                e is CwtValue -> valueElement = e
                e.elementType == CwtElementTypes.NOT_EQUAL_SIGN -> separatorType = CwtSeparatorType.NOT_EQUAL
            }
        }

        if (keyElement == null) {
            logger.warn("Missing option key, skipped.".withLocationPrefix(element))
            return null
        }
        if (valueElement == null) {
            logger.warn("Missing option value, skipped.".withLocationPrefix(element))
            return null
        }

        val key = keyElement.value
        val value = valueElement.value
        val valueType = valueElement.type
        val optionConfigs = CwtConfigResolverManager.getOptionConfigsInOption(valueElement)
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

private const val blockValue = PlsStrings.blockFolder
private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

private abstract class CwtOptionConfigBase : CwtOptionConfig {
    override fun equals(other: Any?) = this === other || other is CwtOptionConfig
        && key == other.key && value == other.value
        && separatorType == other.separatorType && optionConfigs == other.optionConfigs

    override fun hashCode() = Objects.hash(key, value, separatorType, optionConfigs)
    override fun toString() = "(option) $key $separatorType $value"
}

// 12 + 1 * 1 + 1 * 4 = 17 -> 24
private abstract class CwtOptionConfigImplBase(
    key: String,
    separatorType: CwtSeparatorType,
) : CwtOptionConfigBase() {
    private val separatorTypeId = separatorType.optimized(OptimizerRegistry.forCwtSeparatorType()) // optimized to optimize memory

    override val key: String = key.optimized() // optimized to optimize memory
    override val separatorType: CwtSeparatorType get() = separatorTypeId.deoptimized(OptimizerRegistry.forCwtSeparatorType())
}

// 12 + 2 * 1 + 2 * 4 = 22 -> 24
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

// 12 + 1 * 1 + 2 * 4 = 21 -> 24
private class CwtOptionConfigImplNested(
    key: String,
    separatorType: CwtSeparatorType,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>?,
) : CwtOptionConfigImplBase(key, separatorType) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block
}
