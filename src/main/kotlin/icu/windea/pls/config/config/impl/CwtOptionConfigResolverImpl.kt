package icu.windea.pls.config.config.impl

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.util.CwtConfigResolverUtil
import icu.windea.pls.config.util.CwtConfigResolverUtil.withLocationPrefix
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue
import java.util.concurrent.ConcurrentHashMap

internal class CwtOptionConfigResolverImpl : CwtOptionConfig.Resolver {
    private val logger = thisLogger()
    private val cache = ConcurrentHashMap<String, CwtOptionConfig>()

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
            return cache.getOrPut(cacheKey) {
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
    override val key = key.intern() // intern to optimize memory
    override val value = value.intern() // intern to optimize memory

    private val valueTypeId = valueType.optimizeValue() // use enum id as field to optimize memory
    override val valueType get() = valueTypeId.deoptimizeValue<CwtType>()

    private val separatorTypeId = separatorType.optimizeValue() // use enum id as field to optimize memory
    override val separatorType get() = separatorTypeId.deoptimizeValue<CwtSeparatorType>()

    override fun toString() = "(option) $key $separatorType $value"
}
