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
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.filterIsInstanceFast
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtMemberType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.forCwtType

class CwtValueConfigResolverImpl : CwtValueConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(element: CwtValue, file: CwtFile, configGroup: CwtConfigGroup): CwtValueConfig {
        // - use `EmptyPointer` for default project to optimize memory

        val pointer = if (configGroup.project.isDefault) emptyPointer() else element.createPointer(file)
        val value = element.value
        val valueType = element.type
        val configs = CwtConfigResolverUtil.getConfigs(element, file, configGroup)
        val optionConfigs = CwtConfigResolverUtil.getOptionConfigs(element)
        val config = create(pointer, configGroup, value, valueType, configs, optionConfigs)
        postProcess(config)
        logger.trace { "Resolved value config (value: ${config.value}).".withLocationPrefix(element) }
        return config
    }

    override fun postProcess(config: CwtValueConfig) {
        // bind parent config
        config.configs?.forEach { it.parentConfig = config }
        // apply special options
        CwtConfigResolverUtil.applyOptions(config)
        // collect information
        CwtConfigResolverUtil.collectFromConfigExpression(config, config.valueExpression)
    }

    override fun create(
        pointer: SmartPsiElementPointer<out CwtValue>,
        configGroup: CwtConfigGroup,
        value: String,
        valueType: CwtType,
        configs: List<CwtMemberConfig<*>>?,
        optionConfigs: List<CwtOptionMemberConfig<*>>,
        propertyConfig: CwtPropertyConfig?
    ): CwtValueConfig {
        val optionConfigs = optionConfigs.optimized() // optimized to optimize memory
        val noConfigs = configs == null // 2.0.6 NOTE configs may be injected during deep copy
        val noOptionConfigs = optionConfigs.isEmpty()
        if (noConfigs) {
            return when (noOptionConfigs) {
                true -> CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig)
                else -> CwtValueConfigImplWithOptionConfigs(pointer, configGroup, value, valueType, propertyConfig, optionConfigs)
            }
        }
        val configs = configs.optimized() // optimized to optimize memory
        val memberType = CwtConfigResolverUtil.checkMemberType(configs)
        return when (memberType) {
            null -> when (noOptionConfigs) {
                true -> CwtValueConfigImplWithConfigs(pointer, configGroup, value, propertyConfig, configs)
                else -> CwtValueConfigImplWithConfigsAndOptionConfigs(pointer, configGroup, value, propertyConfig, configs, optionConfigs)
            }
            CwtMemberType.PROPERTY -> when (noOptionConfigs) {
                true -> CwtValueConfigImplWithPropertyConfigs(pointer, configGroup, value, propertyConfig, configs)
                else -> CwtValueConfigImplWithPropertyConfigsAndOptionConfigs(pointer, configGroup, value, propertyConfig, configs, optionConfigs)
            }
            CwtMemberType.VALUE -> when (noOptionConfigs) {
                true -> CwtValueConfigImplWithValueConfigs(pointer, configGroup, value, propertyConfig, configs)
                else -> CwtValueConfigImplWithValueConfigsAndOptionConfigs(pointer, configGroup, value, propertyConfig, configs, optionConfigs)
            }
        }
    }

    override fun copy(
        targetConfig: CwtValueConfig,
        pointer: SmartPsiElementPointer<out CwtValue>,
        value: String,
        valueType: CwtType,
        configs: List<CwtMemberConfig<*>>?,
        optionConfigs: List<CwtOptionMemberConfig<*>>,
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
        return when (configs == null) {
            true -> CwtValueConfigDelegate(targetConfig)
            else -> CwtValueConfigDelegateWithConfigs(targetConfig, configs)
        }
    }

    override fun delegatedWith(
        targetConfig: CwtValueConfig,
        value: String,
    ): CwtValueConfig {
        return CwtValueConfigDelegateWithValue(targetConfig, value)
    }
}

private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

private abstract class CwtValueConfigBase : UserDataHolderBase(), CwtValueConfig {
    override val properties: List<CwtPropertyConfig>? get() = configs?.filterIsInstanceFast<CwtPropertyConfig>()
    override val values: List<CwtValueConfig>? get() = configs?.filterIsInstanceFast<CwtValueConfig>()

    @Volatile
    override var parentConfig: CwtMemberConfig<*>? = null

    override val valueExpression: CwtDataExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)

    override fun toString() = "(value) $value"
}

private abstract class CwtValueConfigImplBase(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val configGroup: CwtConfigGroup,
    value: String,
    override val propertyConfig: CwtPropertyConfig?,
) : CwtValueConfigBase() {
    override val value: String = value.optimized() // optimized to optimize memory
    override val optionConfigs: List<CwtOptionMemberConfig<*>> get() = emptyList()
}

private open class CwtValueConfigImpl(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType,
    propertyConfig: CwtPropertyConfig?,
) : CwtValueConfigImplBase(pointer, configGroup, value, propertyConfig) {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val configs: List<CwtMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null
}

private open class CwtValueConfigImplWithConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    propertyConfig: CwtPropertyConfig?,
    override val configs: List<CwtMemberConfig<*>>,
) : CwtValueConfigImplBase(pointer, configGroup, value, propertyConfig) {
    override val valueType: CwtType get() = CwtType.Block
}

private open class CwtValueConfigImplWithPropertyConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    propertyConfig: CwtPropertyConfig?,
    configs: List<CwtMemberConfig<*>>,
) : CwtValueConfigImplWithConfigs(pointer, configGroup, value, propertyConfig, configs) {
    override val properties: List<CwtPropertyConfig> get() = configs.cast()
    override val values: List<CwtValueConfig> get() = emptyList()
}

private open class CwtValueConfigImplWithValueConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    propertyConfig: CwtPropertyConfig?,
    configs: List<CwtMemberConfig<*>>,
) : CwtValueConfigImplWithConfigs(pointer, configGroup, value, propertyConfig, configs) {
    override val properties: List<CwtPropertyConfig> get() = emptyList()
    override val values: List<CwtValueConfig> get() = configs.cast()
}

private class CwtValueConfigImplWithOptionConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType,
    propertyConfig: CwtPropertyConfig?,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig)

private class CwtValueConfigImplWithConfigsAndOptionConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    propertyConfig: CwtPropertyConfig?,
    configs: List<CwtMemberConfig<*>>,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtValueConfigImplWithConfigs(pointer, configGroup, value, propertyConfig, configs)

private class CwtValueConfigImplWithPropertyConfigsAndOptionConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    propertyConfig: CwtPropertyConfig?,
    configs: List<CwtMemberConfig<*>>,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtValueConfigImplWithPropertyConfigs(pointer, configGroup, value, propertyConfig, configs)

private class CwtValueConfigImplWithValueConfigsAndOptionConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    propertyConfig: CwtPropertyConfig?,
    configs: List<CwtMemberConfig<*>>,
    override val optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtValueConfigImplWithValueConfigs(pointer, configGroup, value, propertyConfig, configs)

private open class CwtValueConfigDelegate(
    private val delegate: CwtValueConfig
) : CwtValueConfigBase() {
    override val pointer: SmartPsiElementPointer<out CwtValue> get() = delegate.pointer
    override val configGroup: CwtConfigGroup get() = delegate.configGroup
    override val value: String get() = delegate.value
    override val valueType: CwtType get() = delegate.valueType
    override val configs: List<CwtMemberConfig<*>>? get() = delegate.configs
    override val properties: List<CwtPropertyConfig>? get() = delegate.properties
    override val values: List<CwtValueConfig>? get() = delegate.values
    override val optionConfigs: List<CwtOptionMemberConfig<*>> get() = delegate.optionConfigs
    override val propertyConfig: CwtPropertyConfig? get() = delegate.propertyConfig

    override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
}

private class CwtValueConfigDelegateWithConfigs(
    delegate: CwtValueConfig,
    override val configs: List<CwtMemberConfig<*>>?,
) : CwtValueConfigDelegate(delegate) {
    override val valueType: CwtType get() = if (configs != null) CwtType.Block else super.valueType
}

private class CwtValueConfigDelegateWithValue(
    delegate: CwtValueConfig,
    value: String,
) : CwtValueConfigDelegate(delegate) {
    override val value: String = value.optimized() // optimized to optimize memory
    override val configs: List<CwtMemberConfig<*>>? get() = null // should be always null here
}

private class CwtValueConfigFromPropertyConfig(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val propertyConfig: CwtPropertyConfig,
) : CwtValueConfigBase() {
    override val configGroup: CwtConfigGroup get() = propertyConfig.configGroup
    override val value: String get() = propertyConfig.value
    override val valueType: CwtType get() = propertyConfig.valueType
    override val configs: List<CwtMemberConfig<*>>? get() = propertyConfig.configs
    override val optionConfigs: List<CwtOptionMemberConfig<*>> get() = propertyConfig.optionConfigs
}
