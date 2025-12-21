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
import icu.windea.pls.config.resolved
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.config.util.CwtConfigService
import icu.windea.pls.config.util.option.CwtOptionConfigsOptimizer
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.filterIsInstanceFast
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPropertyPointer
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtMemberType
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.model.forCwtSeparatorType
import icu.windea.pls.model.forCwtType

class CwtPropertyConfigResolverImpl : CwtPropertyConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun create(
        pointer: SmartPsiElementPointer<out CwtProperty>,
        configGroup: CwtConfigGroup,
        key: String,
        value: String,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        configs: List<CwtMemberConfig<*>>?,
        optionConfigs: List<CwtOptionMemberConfig<*>>,
        injectable: Boolean,
    ): CwtPropertyConfig {
        val optionConfigs = optionConfigs.optimized() // optimized to optimize memory
        val withConfigs = configs != null && (injectable || configs.isNotEmpty()) // 2.0.6 NOTE configs may be injectable
        val config = when (withConfigs) {
            true -> CwtPropertyConfigImplWithConfigs(pointer, configGroup, key, separatorType, optionConfigs)
            else -> CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType, optionConfigs)
        }
        if (withConfigs) withConfigs(config, configs)
        return config
    }

    override fun withConfigs(config: CwtPropertyConfig, configs: List<CwtMemberConfig<*>>) {
        if (config is CwtPropertyConfigImplWithConfigs) {
            config.configs = configs.optimized() // optimized to optimize memory
            config.memberType = CwtConfigResolverManager.checkMemberType(configs)
        }
    }

    override fun postProcess(config: CwtPropertyConfig) {
        // bind parent config
        config.configs?.forEach { it.parentConfig = config }
        // run post processors
        CwtConfigService.postProcess(config)
        // collect information
        CwtConfigResolverManager.collectFromConfigExpression(config, config.keyExpression)
        CwtConfigResolverManager.collectFromConfigExpression(config, config.valueExpression)
    }

    override fun postOptimize(config: CwtPropertyConfig) {
        // optimize child configs
        when (config) {
            is CwtPropertyConfigImplWithConfigs -> config.configs = config.configs.optimized()
            is CwtPropertyConfigDelegateWithConfigs -> config.configs = config.configs?.optimized()
        }
        // bind parent config
        config.configs?.forEach { it.parentConfig = config }
    }

    override fun resolve(element: CwtProperty, file: CwtFile, configGroup: CwtConfigGroup): CwtPropertyConfig? {
        // - use `EmptyPointer` for default project to optimize memory
        // - use `CwtPropertyPointer` to optimize performance and memory

        val valueElement = element.propertyValue
        if (valueElement == null) {
            logger.warn("Missing property value.".withLocationPrefix(element))
            return null
        }
        val pointer = if (configGroup.project.isDefault) emptyPointer() else CwtPropertyPointer(element.createPointer(file))
        val key = element.name
        val value: String = valueElement.value
        val valueType = valueElement.type
        val separatorType = element.separatorType
        val configs = CwtConfigResolverManager.getConfigs(valueElement, file, configGroup)
        val optionConfigs = CwtConfigResolverManager.getOptionConfigs(element)
        val config = create(pointer, configGroup, key, value, valueType, separatorType, configs, optionConfigs)
        logger.trace { "Resolved property config (key: ${config.key}, value: ${config.value}).".withLocationPrefix(element) }
        return config
    }

    override fun copy(
        targetConfig: CwtPropertyConfig,
        pointer: SmartPsiElementPointer<out CwtProperty>,
        key: String,
        value: String,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        configs: List<CwtMemberConfig<*>>?,
        optionConfigs: List<CwtOptionMemberConfig<*>>,
    ): CwtPropertyConfig {
        val config = create(pointer, targetConfig.configGroup, key, value, valueType, separatorType, configs, optionConfigs, injectable = true)
        return config
    }

    override fun delegated(
        targetConfig: CwtPropertyConfig,
        configs: List<CwtMemberConfig<*>>?,
    ): CwtPropertyConfig {
        val withConfig = configs != null // 2.0.6 NOTE configs may be injectable
        return when (withConfig) {
            true -> CwtPropertyConfigDelegateWithConfigs(targetConfig, configs)
            else -> CwtPropertyConfigDelegate(targetConfig)
        }
    }

    override fun delegatedWith(
        targetConfig: CwtPropertyConfig,
        key: String,
        value: String,
    ): CwtPropertyConfig {
        return CwtPropertyConfigDelegateWithKeyAndValue(targetConfig, key, value)
    }
}

private const val blockValue = PlsStringConstants.blockFolder
private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

// 12 + 3 * 4 = 24 -> 24
private abstract class CwtPropertyConfigBase : UserDataHolderBase(), CwtPropertyConfig {
    override val properties: List<CwtPropertyConfig>? get() = configs?.filterIsInstanceFast<CwtPropertyConfig>()
    override val values: List<CwtValueConfig>? get() = configs?.filterIsInstanceFast<CwtValueConfig>()

    @Volatile
    override var parentConfig: CwtMemberConfig<*>? = null

    // use memory-optimized lazy property
    @Volatile
    private var _valueConfig: Any? = EMPTY_OBJECT
    override val valueConfig: CwtValueConfig? @Synchronized get() = resolveLazyValueConfig()

    private fun resolveLazyValueConfig(): CwtValueConfig? {
        return if (_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else resolveValueConfig().also { _valueConfig = it }
    }

    private fun resolveValueConfig(): CwtValueConfig? {
        // this function should be enough fast because there are no pointers to be created
        val resolvedPointer = this.resolved().pointer
        val valuePointer = when {
            resolvedPointer is CwtPropertyPointer -> resolvedPointer.valuePointer
            else -> resolvedPointer.element?.propertyValue?.createPointer()
        } ?: return null
        return CwtValueConfig.resolveFromPropertyConfig(valuePointer, this)
    }

    override val keyExpression: CwtDataExpression get() = CwtDataExpression.resolve(key, true)
    override val valueExpression: CwtDataExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)

    override fun toString() = "(property) $key $separatorType $value"
}

// 12 + 2 * 1 + 6 * 4 = 38 -> 40
private abstract class CwtPropertyConfigImplBase(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val configGroup: CwtConfigGroup,
    key: String,
    separatorType: CwtSeparatorType,
    optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtPropertyConfigBase() {
    private val separatorTypeId = separatorType.optimized(OptimizerRegistry.forCwtSeparatorType()) // optimized to optimize memory
    private val optionConfigsId = optionConfigs.optimized(CwtOptionConfigsOptimizer) // optimized to optimize memory

    override val key: String = key.optimized() // optimized to optimize memory
    override val separatorType: CwtSeparatorType get() = separatorTypeId.deoptimized(OptimizerRegistry.forCwtSeparatorType())
    override val optionConfigs: List<CwtOptionMemberConfig<*>> get() = optionConfigsId.deoptimized(CwtOptionConfigsOptimizer)
}

// 12 + 3 * 1 + 7 * 4 = 43 -> 48
private open class CwtPropertyConfigImpl(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType,
    separatorType: CwtSeparatorType,
    optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtPropertyConfigImplBase(pointer, configGroup, key, separatorType, optionConfigs) {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val value: String = value.optimized() // optimized to optimize memory
    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val configs: List<CwtMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null
}

// 12 + 2 * 1 + 8 * 4 = 46 -> 48
private open class CwtPropertyConfigImplWithConfigs(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    separatorType: CwtSeparatorType,
    optionConfigs: List<CwtOptionMemberConfig<*>>,
) : CwtPropertyConfigImplBase(pointer, configGroup, key, separatorType, optionConfigs) {
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

// 12 + 4 * 4 = 28 -> 32
private open class CwtPropertyConfigDelegate(
    private val delegate: CwtPropertyConfig
) : CwtPropertyConfigBase() {
    override val pointer: SmartPsiElementPointer<out CwtProperty> get() = delegate.pointer
    override val configGroup: CwtConfigGroup get() = delegate.configGroup
    override val key: String get() = delegate.key
    override val value: String get() = delegate.value
    override val valueType: CwtType get() = delegate.valueType
    override val separatorType: CwtSeparatorType get() = delegate.separatorType
    override val configs: List<CwtMemberConfig<*>>? get() = delegate.configs
    override val properties: List<CwtPropertyConfig>? get() = delegate.properties
    override val values: List<CwtValueConfig>? get() = delegate.values
    override val optionConfigs: List<CwtOptionMemberConfig<*>> get() = delegate.optionConfigs

    override fun <T> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
}

// 12 + 5 * 4 = 32 -> 32
private class CwtPropertyConfigDelegateWithConfigs(
    delegate: CwtPropertyConfig,
    override var configs: List<CwtMemberConfig<*>>?,
) : CwtPropertyConfigDelegate(delegate) {
    override val valueType: CwtType get() = if (configs != null) CwtType.Block else super.valueType
}

// 12 + 6 * 4 = 26 -> 40
private class CwtPropertyConfigDelegateWithKeyAndValue(
    delegate: CwtPropertyConfig,
    key: String,
    value: String,
) : CwtPropertyConfigDelegate(delegate) {
    override val key: String = key.optimized() // optimized to optimize memory
    override val value: String = value.optimized() // optimized to optimize memory
    override val configs: List<CwtMemberConfig<*>>? get() = null // should be always null here
}
