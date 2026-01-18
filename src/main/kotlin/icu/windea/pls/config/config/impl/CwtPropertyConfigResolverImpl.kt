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
import icu.windea.pls.config.resolved
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPropertyPointer
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtMembersType
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.model.forCwtSeparatorType
import icu.windea.pls.model.forCwtType

internal class CwtPropertyConfigResolverImpl : CwtPropertyConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(element: CwtProperty, file: CwtFile, configGroup: CwtConfigGroup): CwtPropertyConfig? {
        // - use `EmptyPointer` for default project to optimize memory
        // - use `CwtPropertyPointer` to optimize performance and memory
        // - 2.1.1 use `keyExpression` and `valueExpression` as constructor argument and field directly to optimize performance

        val valueElement = element.propertyValue
        if (valueElement == null) {
            logger.warn("Missing property value.".withLocationPrefix(element))
            return null
        }
        val pointer = if (configGroup.project.isDefault) emptyPointer() else CwtPropertyPointer(element.createPointer(file))
        val configs = CwtConfigResolverManager.getConfigs(valueElement, file, configGroup)
        val keyExpression = CwtDataExpression.resolveKey(element.name)
        val valueType = valueElement.type
        val separatorType = element.separatorType
        val valueExpression = if (configs == null) CwtDataExpression.resolveValue(valueElement.value) else CwtDataExpression.resolveBlock()
        val config = create(pointer, configGroup, keyExpression, valueExpression, valueType, separatorType, configs, injectable = true)
        val optionConfigs = CwtConfigResolverManager.getOptionConfigs(element)
        CwtOptionDataProvider.process(config.optionData, optionConfigs) // initialize option data
        logger.trace { "Resolved property config (key: ${config.key}, value: ${config.value}).".withLocationPrefix(element) }
        return config
    }

    override fun create(
        pointer: SmartPsiElementPointer<out CwtProperty>,
        configGroup: CwtConfigGroup,
        keyExpression: CwtDataExpression,
        valueExpression: CwtDataExpression,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        configs: List<CwtMemberConfig<*>>?,
        injectable: Boolean,
    ): CwtPropertyConfig {
        val withConfigs = configs != null && (injectable || configs.isNotEmpty()) // 2.0.6 NOTE configs may be injectable
        val config = when (withConfigs) {
            true -> CwtPropertyConfigImplWithConfigs(pointer, configGroup, keyExpression, separatorType)
                .also { it.configs = configs.optimized() } // optimized to optimize memory
            else -> CwtPropertyConfigImpl(pointer, configGroup, keyExpression, valueExpression, valueType, separatorType)
        }
        return config
    }

    override fun copy(
        sourceConfig: CwtPropertyConfig,
        pointer: SmartPsiElementPointer<out CwtProperty>,
        keyExpression: CwtDataExpression,
        valueExpression: CwtDataExpression,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        configs: List<CwtMemberConfig<*>>?,
    ): CwtPropertyConfig {
        val config = create(pointer, sourceConfig.configGroup, keyExpression, valueExpression, valueType, separatorType, configs, injectable = true)
        return config
    }
}

private const val blockValue = PlsStrings.blockFolder
private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

// 12 + 3 * 4 = 24 -> 24
private abstract class CwtPropertyConfigBase : CwtOptionDataHolderBase(), CwtPropertyConfig {
    override val optionData: CwtOptionDataHolder get() = this

    @Volatile override var parentConfig: CwtMemberConfig<*>? = null

    // use memory-optimized lazy property
    @Volatile private var _valueConfig: Any? = EMPTY_OBJECT
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

    override val configExpression: CwtDataExpression get() = keyExpression

    override fun postProcess() {
        // bind parent config
        this.configs?.forEachFast { it.parentConfig = this }
        // run post processors
        CwtConfigService.postProcess(this)
        // collect information
        CwtConfigResolverManager.collectFromConfigExpression(this, keyExpression)
        CwtConfigResolverManager.collectFromConfigExpression(this, valueExpression)
    }

    override fun postOptimize() {
        // bind parent config
        this.configs?.forEachFast { it.parentConfig = this }
    }

    override fun delegated(configs: List<CwtMemberConfig<*>>?): CwtPropertyConfig {
        val withConfigs = configs != null // 2.0.6 NOTE configs may be injectable
        val config = when (withConfigs) {
            true -> CwtPropertyConfigDelegateWithConfigs(this)
                .also { it.configs = configs } // do not do optimization here
            else -> CwtPropertyConfigDelegate(this)
        }
        return config
    }

    override fun delegatedWith(key: String, value: String): CwtPropertyConfig {
        return CwtPropertyConfigDelegateWithKeyAndValue(this, key, value)
    }

    override fun toString() = "(property) $key $separatorType $value"
}

// 12 + 1 * 1 + 6 * 4 = 37 -> 40
private abstract class CwtPropertyConfigImplBase(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val configGroup: CwtConfigGroup,
    override val keyExpression: CwtDataExpression, // as constructor argument and field directly
    separatorType: CwtSeparatorType,
) : CwtPropertyConfigBase() {
    private val separatorTypeId = separatorType.optimized(OptimizerRegistry.forCwtSeparatorType()) // optimized to optimize memory

    override val key: String get() = keyExpression.expressionString
    override val separatorType: CwtSeparatorType get() = separatorTypeId.deoptimized(OptimizerRegistry.forCwtSeparatorType())
}

// 12 + 2 * 1 + 7 * 4 = 42 -> 48
private open class CwtPropertyConfigImpl(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    keyExpression: CwtDataExpression,
    override val valueExpression: CwtDataExpression, // as constructor argument and field directly
    valueType: CwtType,
    separatorType: CwtSeparatorType,
) : CwtPropertyConfigImplBase(pointer, configGroup, keyExpression, separatorType) {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val value: String get() = valueExpression.expressionString
    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val configs: List<CwtMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null
}

// 12 + 1 * 1 + 8 * 4 = 45 -> 48
private open class CwtPropertyConfigImplWithConfigs(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    keyExpression: CwtDataExpression,
    separatorType: CwtSeparatorType,
) : CwtPropertyConfigImplBase(pointer, configGroup, keyExpression, separatorType) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block

    @Volatile override var configs: List<CwtMemberConfig<*>> = emptyList()
    @Volatile private var membersType: CwtMembersType = CwtMembersType.MIXED

    override val properties: List<CwtPropertyConfig>
        get() {
            if (membersType == CwtMembersType.UNSET) membersType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getProperties(configs, membersType)
        }
    override val values: List<CwtValueConfig>
        get() {
            if (membersType == CwtMembersType.UNSET) membersType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getValues(configs, membersType)
        }

    override val valueExpression: CwtDataExpression get() = CwtDataExpression.resolveBlock()

    override fun withConfigs(configs: List<CwtMemberConfig<*>>): Boolean {
        this.configs = configs.optimized() // optimized to optimize memory
        return true
    }

    override fun postProcess() {
        // optimize child configs
        this.membersType = CwtMembersType.UNSET
        // call super
        super.postProcess()
    }

    override fun postOptimize() {
        // optimize child configs
        this.configs = this.configs.optimized()
        this.membersType = CwtMembersType.UNSET
        // call super
        super.postOptimize()
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
    override val optionData: CwtOptionDataHolder get() = delegate.optionData

    override val keyExpression: CwtDataExpression get() = delegate.keyExpression
    override val valueExpression: CwtDataExpression get() = delegate.valueExpression

    override fun <T> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
}

// 12 + 6 * 4 = 36 -> 40
private class CwtPropertyConfigDelegateWithConfigs(
    delegate: CwtPropertyConfig
) : CwtPropertyConfigDelegate(delegate) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block

    @Volatile override var configs: List<CwtMemberConfig<*>> = emptyList()
    @Volatile private var membersType: CwtMembersType = CwtMembersType.MIXED

    override val properties: List<CwtPropertyConfig>
        get() {
            if (membersType == CwtMembersType.UNSET) membersType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getProperties(configs, membersType)
        }
    override val values: List<CwtValueConfig>
        get() {
            if (membersType == CwtMembersType.UNSET) membersType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getValues(configs, membersType)
        }

    override fun withConfigs(configs: List<CwtMemberConfig<*>>): Boolean {
        this.configs = configs.optimized() // optimized to optimize memory
        return true
    }

    override fun postOptimize() {
        // optimize child configs
        this.configs = this.configs.optimized()
        this.membersType = CwtMembersType.UNSET
        // call super
        super.postOptimize()
    }
}

// 12 + 6 * 4 = 26 -> 40
private class CwtPropertyConfigDelegateWithKeyAndValue(
    delegate: CwtPropertyConfig,
    key: String,
    value: String,
) : CwtPropertyConfigDelegate(delegate) {
    override val key: String get() = keyExpression.expressionString
    override val value: String get() = valueExpression.expressionString
    override val configs: List<CwtMemberConfig<*>>? get() = null // should be always null here

    override val keyExpression: CwtDataExpression = CwtDataExpression.resolveKey(key) // as field directly
    override val valueExpression: CwtDataExpression = CwtDataExpression.resolveValue(value) // as field directly
}
