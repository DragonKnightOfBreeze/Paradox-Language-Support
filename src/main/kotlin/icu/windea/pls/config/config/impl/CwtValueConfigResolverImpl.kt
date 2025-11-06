package icu.windea.pls.config.config.impl

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.config.util.CwtConfigResolverUtil
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getUserDataOrDefault
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.forCwtType

class CwtValueConfigResolverImpl : CwtValueConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(element: CwtValue, file: CwtFile, configGroup: CwtConfigGroup): CwtValueConfig {
        // 1. use EmptyPointer for default project to optimize memory

        val pointer = when {
            configGroup.project.isDefault -> emptyPointer()
            else -> element.createPointer(file)
        }
        val value: String = element.value
        val valueType = element.type
        val configs = CwtConfigResolverUtil.getConfigs(element, file, configGroup)
        val optionConfigs = CwtConfigResolverUtil.getOptionConfigs(element)
        val config = create(pointer, configGroup, value, valueType, configs, optionConfigs)
        CwtConfigResolverUtil.applyOptions(config)
        CwtConfigResolverUtil.collectFromConfigExpression(config, config.valueExpression)
        configs?.forEach { it.parentConfig = config }
        logger.trace { "Resolved value config (value: ${config.value}).".withLocationPrefix(element) }
        return config
    }

    override fun create(
        pointer: SmartPsiElementPointer<out CwtValue>,
        configGroup: CwtConfigGroup,
        value: String,
        valueType: CwtType,
        configs: List<CwtMemberConfig<*>>?,
        optionConfigs: List<CwtOptionMemberConfig<*>>?,
        propertyConfig: CwtPropertyConfig?
    ): CwtValueConfig {
        return if (configs != null) {
            if (optionConfigs != null) {
                CwtValueConfigImpl1(pointer, configGroup, value, valueType, configs, optionConfigs, propertyConfig)
            } else {
                CwtValueConfigImpl2(pointer, configGroup, value, valueType, configs, propertyConfig)
            }
        } else {
            if (optionConfigs != null) {
                CwtValueConfigImpl3(pointer, configGroup, value, valueType, optionConfigs, propertyConfig)
            } else {
                CwtValueConfigImpl4(pointer, configGroup, value, valueType, propertyConfig)
            }
        }
    }

    override fun copy(
        targetConfig: CwtValueConfig,
        pointer: SmartPsiElementPointer<out CwtValue>,
        value: String,
        valueType: CwtType,
        configs: List<CwtMemberConfig<*>>?,
        optionConfigs: List<CwtOptionMemberConfig<*>>?,
        propertyConfig: CwtPropertyConfig?,
    ): CwtValueConfig {
        return create(pointer, targetConfig.configGroup, value, valueType, configs, optionConfigs, propertyConfig)
    }

    override fun resolveFromPropertyConfig(
        pointer: SmartPsiElementPointer<out CwtValue>,
        propertyConfig: CwtPropertyConfig
    ): CwtValueConfig {
        return CwtValueConfigFromPropertyConfig(pointer, propertyConfig)
    }

    override fun delegated(
        targetConfig: CwtValueConfig,
        configs: List<CwtMemberConfig<*>>?,
    ): CwtValueConfig {
        return if (configs != null) {
            CwtValueConfigDelegate1(targetConfig, configs)
        } else {
            CwtValueConfigDelegate2(targetConfig)
        }
    }

    override fun delegatedWith(
        targetConfig: CwtValueConfig,
        value: String
    ): CwtValueConfig {
        return CwtValueConfigDelegateWith(targetConfig, value)
    }
}

private abstract class CwtValueConfigImpl(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    propertyConfig: CwtPropertyConfig? = null,
) : UserDataHolderBase(), CwtValueConfig {
    override val value = value.optimized() // optimized to optimize memory

    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // use enum id to optimize memory
    override val valueType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())

    override val propertyConfig = propertyConfig

    @Volatile
    override var parentConfig: CwtMemberConfig<*>? = null

    // cached into user data to optimize performance and memory
    override val valueExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else getUserDataOrDefault(CwtMemberConfig.Keys.valueExpression)

    override fun toString() = "(value) $value"
}

// 12 + 8 * 4 + 1 * 1 = 45 -> 48
private class CwtValueConfigImpl1(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    configs: List<CwtMemberConfig<*>>? = null,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig) {
    override var configs = configs
    override val optionConfigs = optionConfigs
}

// 12 + 7 * 4 + 1 * 1 = 41 -> 48
private class CwtValueConfigImpl2(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    configs: List<CwtMemberConfig<*>>? = null,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig) {
    override var configs = configs
    override val optionConfigs get() = null
}

// 12 + 7 * 4 + 1 * 1 = 41 -> 48
private class CwtValueConfigImpl3(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
    override val optionConfigs = optionConfigs
}

// 12 + 6 * 4 + 1 * 1 = 37 -> 40
private class CwtValueConfigImpl4(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
    override val optionConfigs get() = null
}

private abstract class CwtValueConfigDelegate(
    private val delegate: CwtValueConfig,
) : UserDataHolderBase(), CwtValueConfig by delegate {
    @Volatile
    override var parentConfig: CwtMemberConfig<*>? = null

    // cached into user data to optimize performance and memory
    override val valueExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else getUserDataOrDefault(CwtMemberConfig.Keys.valueExpression)

    override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)

    override fun toString() = "(value) $value"
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegate1(
    delegate: CwtValueConfig,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtValueConfigDelegate(delegate) {
    override var configs = configs
}

// 12 + 3 * 4 = 24 -> 24
private class CwtValueConfigDelegate2(
    delegate: CwtValueConfig,
) : CwtValueConfigDelegate(delegate) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegateWith(
    delegate: CwtValueConfig,
    value: String,
    // configs should be always null here
) : CwtValueConfigDelegate(delegate) {
    override val value = value.optimized() // optimized to optimize memory

    // must override all following expression related properties, since value is overridden
    override val valueExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)
    override val configExpression: CwtDataExpression get() = valueExpression
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigFromPropertyConfig(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val propertyConfig: CwtPropertyConfig,
) : UserDataHolderBase(), CwtValueConfig {
    override val configGroup get() = propertyConfig.configGroup
    override val value get() = propertyConfig.value
    override val valueType get() = propertyConfig.valueType
    override val configs get() = propertyConfig.configs
    override val optionConfigs get() = propertyConfig.optionConfigs

    @Volatile
    override var parentConfig: CwtMemberConfig<*>? = null

    // cached into user data to optimize performance and memory
    override val valueExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else getUserDataOrDefault(CwtMemberConfig.Keys.valueExpression)

    override fun toString() = value
}

private val CwtMemberConfig.Keys.valueExpression by createKey<_, CwtMemberConfig<*>>(CwtMemberConfig.Keys) { CwtDataExpression.resolve(value, false) }
