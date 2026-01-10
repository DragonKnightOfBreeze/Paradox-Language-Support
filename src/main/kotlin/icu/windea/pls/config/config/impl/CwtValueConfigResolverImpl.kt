@file:Optimized

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
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.CwtConfigService
import icu.windea.pls.config.util.option.CwtOptionConfigsOptimizer
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.filterIsInstanceFast
import icu.windea.pls.core.collections.forEachFast
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
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.model.forCwtType

class CwtValueConfigResolverImpl : CwtValueConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun create(
        pointer: SmartPsiElementPointer<out CwtValue>,
        configGroup: CwtConfigGroup,
        value: String,
        valueType: CwtType,
        configs: List<CwtMemberConfig<*>>?,
        optionConfigs: List<CwtOptionMemberConfig<*>>,
        propertyConfig: CwtPropertyConfig?,
        injectable: Boolean,
    ): CwtValueConfig {
        val optionConfigs = optionConfigs.optimized() // optimized to optimize memory
        val withConfigs = configs != null && (injectable || configs.isNotEmpty()) // 2.0.6 NOTE configs may be injectable
        val config = when (withConfigs) {
            true -> CwtValueConfigImplWithConfigs(pointer, configGroup, propertyConfig, optionConfigs)
            else -> CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig, optionConfigs)
        }
        if (withConfigs) withConfigs(config, configs)
        return config
    }

    override fun withConfigs(config: CwtValueConfig, configs: List<CwtMemberConfig<*>>): Boolean {
        if (config is CwtValueConfigImplWithConfigs) {
            config.configs = configs.optimized() // optimized to optimize memory
            config.memberType = CwtConfigResolverManager.checkMemberType(configs)
            return true
        }
        return false
    }

    override fun postProcess(config: CwtValueConfig) {
        // bind parent config
        config.configs?.forEachFast { it.parentConfig = config }
        // run post processors
        CwtConfigService.postProcess(config)
        // collect information
        CwtConfigResolverManager.collectFromConfigExpression(config, config.valueExpression)
    }

    override fun postOptimize(config: CwtValueConfig) {
        // optimize child configs
        when (config) {
            is CwtValueConfigImplWithConfigs -> config.configs = config.configs.optimized()
            is CwtValueConfigDelegateWithConfigs -> config.configs = config.configs?.optimized()
        }
        // bind parent config
        config.configs?.forEachFast { it.parentConfig = config }
    }

    override fun resolve(element: CwtValue, file: CwtFile, configGroup: CwtConfigGroup): CwtValueConfig {
        // - use `EmptyPointer` for default project to optimize memory

        val pointer = if (configGroup.project.isDefault) emptyPointer() else element.createPointer(file)
        val value = element.value
        val valueType = element.type
        val configs = CwtConfigResolverManager.getConfigs(element, file, configGroup)
        val optionConfigs = CwtConfigResolverManager.getOptionConfigs(element)
        val config = create(pointer, configGroup, value, valueType, configs, optionConfigs, injectable = true)
        logger.trace { "Resolved value config (value: ${config.value}).".withLocationPrefix(element) }
        return config
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
        val config = create(pointer, targetConfig.configGroup, value, valueType, configs, optionConfigs, propertyConfig, injectable = true)
        return config
    }

    override fun resolveFromPropertyConfig(
        pointer: SmartPsiElementPointer<out CwtValue>,
        propertyConfig: CwtPropertyConfig,
    ): CwtValueConfig {
        return CwtValueConfigFromPropertyConfig(pointer, propertyConfig)
    }

    override fun delegated(
        targetConfig: CwtValueConfig,
        configs: List<CwtMemberConfig<*>>?,
    ): CwtValueConfig {
        val noConfigs = configs == null  // 2.0.6 NOTE configs may be injectable
        return when (noConfigs) {
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

private const val blockValue = PlsStrings.blockFolder
private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

// 12 + 2 * 4 = 20 -> 24
private abstract class CwtValueConfigBase : UserDataHolderBase(), CwtValueConfig {
    override val properties: List<CwtPropertyConfig>? get() = configs?.filterIsInstanceFast<CwtPropertyConfig>()
    override val values: List<CwtValueConfig>? get() = configs?.filterIsInstanceFast<CwtValueConfig>()

    @Volatile
    override var parentConfig: CwtMemberConfig<*>? = null

    override val valueExpression: CwtDataExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)

    override fun toString() = "(value) $value"
}

// 12 + 1 * 1 + 5 * 4 = 33 -> 40
private abstract class CwtValueConfigImplBase(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val configGroup: CwtConfigGroup,
    override val propertyConfig: CwtPropertyConfig?,
    optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtValueConfigBase() {
    private val optionConfigsId = optionConfigs.optimized(CwtOptionConfigsOptimizer) // optimized to optimize memory

    override val optionConfigs: List<CwtOptionMemberConfig<*>> get() = optionConfigsId.deoptimized(CwtOptionConfigsOptimizer)
}

// 12 + 2 * 1 + 6 * 4 = 38 -> 40
private open class CwtValueConfigImpl(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType,
    propertyConfig: CwtPropertyConfig?,
    optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtValueConfigImplBase(pointer, configGroup, propertyConfig, optionConfigs) {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val value: String = value.optimized() // optimized to optimize memory
    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val configs: List<CwtMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null
}

// 12 + 1 * 1 + 7 * 4 = 41 -> 48
private open class CwtValueConfigImplWithConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    propertyConfig: CwtPropertyConfig?,
    optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtValueConfigImplBase(pointer, configGroup, propertyConfig, optionConfigs) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block
    override var configs: List<CwtMemberConfig<*>> = emptyList()
    var memberType: CwtMemberType = CwtMemberType.MIXED
    override val properties: List<CwtPropertyConfig>
        get() = when (memberType) {
            CwtMemberType.PROPERTY -> configs.cast()
            CwtMemberType.MIXED -> configs.filterIsInstanceFast()
            else -> emptyList()
        }
    override val values: List<CwtValueConfig>
        get() = when (memberType) {
            CwtMemberType.VALUE -> configs.cast()
            CwtMemberType.MIXED -> configs.filterIsInstanceFast()
            else -> emptyList()
        }
}

// 12 + 3 * 4 = 24 -> 24
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

    override fun <T> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegateWithConfigs(
    delegate: CwtValueConfig,
    override var configs: List<CwtMemberConfig<*>>?,
) : CwtValueConfigDelegate(delegate) {
    override val valueType: CwtType get() = if (configs != null) CwtType.Block else super.valueType
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegateWithValue(
    delegate: CwtValueConfig,
    value: String,
) : CwtValueConfigDelegate(delegate) {
    override val value: String = value.optimized() // optimized to optimize memory
    override val configs: List<CwtMemberConfig<*>>? get() = null // should be always null here
}

// 12 + 4 * 4 = 28 -> 32
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
