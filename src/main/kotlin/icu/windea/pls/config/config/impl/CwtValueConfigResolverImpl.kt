@file:Optimized

package icu.windea.pls.config.config.impl

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.util.Key
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.CwtConfigService
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.option.CwtOptionDataHolderBase
import icu.windea.pls.config.option.CwtOptionDataProvider
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtMembersType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.model.forCwtType

class CwtValueConfigResolverImpl : CwtValueConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(element: CwtValue, file: CwtFile, configGroup: CwtConfigGroup): CwtValueConfig {
        // - use `EmptyPointer` for default project to optimize memory

        val pointer = if (configGroup.project.isDefault) emptyPointer() else element.createPointer(file)
        val value = element.value
        val valueType = element.type
        val configs = CwtConfigResolverManager.getConfigs(element, file, configGroup)
        val config = create(pointer, configGroup, value, valueType, configs, injectable = true)
        val optionConfigs = CwtConfigResolverManager.getOptionConfigs(element)
        CwtOptionDataProvider.process(config.optionData, optionConfigs) // initialize option data
        logger.trace { "Resolved value config (value: ${config.value}).".withLocationPrefix(element) }
        return config
    }

    override fun resolveFromPropertyConfig(
        pointer: SmartPsiElementPointer<out CwtValue>,
        propertyConfig: CwtPropertyConfig,
    ): CwtValueConfig {
        val config = CwtValueConfigFromPropertyConfig(pointer, propertyConfig)
        propertyConfig.optionData.copyTo(config) // inherit option data from property config
        return config
    }

    override fun create(
        pointer: SmartPsiElementPointer<out CwtValue>,
        configGroup: CwtConfigGroup,
        value: String,
        valueType: CwtType,
        configs: List<CwtMemberConfig<*>>?,
        propertyConfig: CwtPropertyConfig?,
        injectable: Boolean,
    ): CwtValueConfig {
        val withConfigs = configs != null && (injectable || configs.isNotEmpty()) // 2.0.6 NOTE configs may be injectable
        val config = when (withConfigs) {
            true -> CwtValueConfigImplWithConfigs(pointer, configGroup, propertyConfig)
                .also { it.configs = configs.optimized() } // optimized to optimize memory
            else -> CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig)
        }
        return config
    }

    override fun copy(
        targetConfig: CwtValueConfig,
        pointer: SmartPsiElementPointer<out CwtValue>,
        value: String,
        valueType: CwtType,
        configs: List<CwtMemberConfig<*>>?,
        propertyConfig: CwtPropertyConfig?,
    ): CwtValueConfig {
        val config = create(pointer, targetConfig.configGroup, value, valueType, configs, propertyConfig, injectable = true)
        return config
    }

    override fun delegated(
        targetConfig: CwtValueConfig,
        configs: List<CwtMemberConfig<*>>?,
    ): CwtValueConfig {
        val withConfigs = configs != null  // 2.0.6 NOTE configs may be injectable
        val config = when (withConfigs) {
            true -> CwtValueConfigDelegateWithConfigs(targetConfig)
                .also { it.configs = configs } // do not do optimization here
            else -> CwtValueConfigDelegate(targetConfig)
        }
        return config
    }

    override fun delegatedWith(
        targetConfig: CwtValueConfig,
        value: String,
    ): CwtValueConfig {
        return CwtValueConfigDelegateWithValue(targetConfig, value)
    }

    override fun withConfigs(config: CwtValueConfig, configs: List<CwtMemberConfig<*>>): Boolean {
        return when (config) {
            is CwtValueConfigImplWithConfigs -> {
                config.configs = configs.optimized() // optimized to optimize memory
                true
            }
            is CwtValueConfigDelegateWithConfigs -> {
                config.configs = configs.optimized() // optimized to optimize memory
                true
            }
            else -> false
        }
    }

    override fun postProcess(config: CwtValueConfig) {
        // optimize child configs
        if (config is CwtValueConfigImplWithConfigs) {
            config.memberType = CwtMembersType.UNSET
        }
        // bind parent config
        config.configs?.forEachFast { it.parentConfig = config }
        // run post processors
        CwtConfigService.postProcess(config)
        // collect information
        CwtConfigResolverManager.collectFromConfigExpression(config, config.valueExpression)
    }

    override fun postOptimize(config: CwtValueConfig) {
        // optimize child configs
        if (config is CwtValueConfigImplWithConfigs) {
            config.configs = config.configs.optimized()
            config.memberType = CwtMembersType.UNSET
        } else if (config is CwtValueConfigDelegateWithConfigs) {
            config.configs = config.configs.optimized()
            config.memberType = CwtMembersType.UNSET
        }
        // bind parent config
        config.configs?.forEachFast { it.parentConfig = config }
    }
}

private const val blockValue = PlsStrings.blockFolder
private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

// 12 + 2 * 4 = 20 -> 24
private abstract class CwtValueConfigBase : CwtOptionDataHolderBase(), CwtValueConfig {
    override val optionData: CwtOptionDataHolder get() = this

    @Volatile override var parentConfig: CwtMemberConfig<*>? = null

    override val valueExpression: CwtDataExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)
    override val configExpression: CwtDataExpression get() = valueExpression

    override fun toString() = "(value) $value"
}

// 12 + 5 * 4 = 32 -> 32
private abstract class CwtValueConfigImplBase(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val configGroup: CwtConfigGroup,
    override val propertyConfig: CwtPropertyConfig?,
) : CwtValueConfigBase()

// 12 + 1 * 1 + 6 * 4 = 37 -> 40
private open class CwtValueConfigImpl(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType,
    propertyConfig: CwtPropertyConfig?,
) : CwtValueConfigImplBase(pointer, configGroup, propertyConfig) {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val value: String get() = valueExpression.expressionString
    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val configs: List<CwtMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null

    override val valueExpression: CwtDataExpression = if (configs != null) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false) // as field directly
}

// 12 + 7 * 4 = 40 -> 40
private open class CwtValueConfigImplWithConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    propertyConfig: CwtPropertyConfig?,
) : CwtValueConfigImplBase(pointer, configGroup, propertyConfig) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block

    @Volatile override var configs: List<CwtMemberConfig<*>> = emptyList()
    @Volatile var memberType: CwtMembersType = CwtMembersType.MIXED

    override val properties: List<CwtPropertyConfig>
        get() {
            if (memberType == CwtMembersType.UNSET) memberType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getProperties(configs, memberType)
        }
    override val values: List<CwtValueConfig>
        get() {
            if (memberType == CwtMembersType.UNSET) memberType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getValues(configs, memberType)
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
    override val optionData: CwtOptionDataHolder get() = delegate.optionData
    override val propertyConfig: CwtPropertyConfig? get() = delegate.propertyConfig

    override val valueExpression: CwtDataExpression get() = delegate.valueExpression

    override fun <T> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
}

// 12 + 5 * 4 = 32 -> 32
private class CwtValueConfigDelegateWithConfigs(
    delegate: CwtValueConfig
) : CwtValueConfigDelegate(delegate) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block

    @Volatile override var configs: List<CwtMemberConfig<*>> = emptyList()
    @Volatile var memberType: CwtMembersType = CwtMembersType.MIXED

    override val properties: List<CwtPropertyConfig>
        get() {
            if (memberType == CwtMembersType.UNSET) memberType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getProperties(configs, memberType)
        }
    override val values: List<CwtValueConfig>
        get() {
            if (memberType == CwtMembersType.UNSET) memberType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getValues(configs, memberType)
        }
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegateWithValue(
    delegate: CwtValueConfig,
    value: String,
) : CwtValueConfigDelegate(delegate) {
    override val value: String get() = valueExpression.expressionString
    override val configs: List<CwtMemberConfig<*>>? get() = null // should be always null here

    override val valueExpression: CwtDataExpression = CwtDataExpression.resolve(value, false) // as field directly
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

    override val valueExpression: CwtDataExpression get() = propertyConfig.valueExpression
}
