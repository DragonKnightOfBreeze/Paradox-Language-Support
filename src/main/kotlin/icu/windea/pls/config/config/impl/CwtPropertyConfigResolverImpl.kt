package icu.windea.pls.config.config.impl

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtPropertyPointer
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.resolved
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.cast
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getUserDataOrDefault
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue

class CwtPropertyConfigResolverImpl : CwtPropertyConfig.Resolver {
    override fun resolve(
        pointer: SmartPsiElementPointer<out CwtProperty>,
        configGroup: CwtConfigGroup,
        key: String,
        value: String,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        configs: List<CwtMemberConfig<*>>?,
        optionConfigs: List<CwtOptionMemberConfig<*>>?
    ): CwtPropertyConfig {
        return if (configs != null) {
            if (optionConfigs != null) {
                CwtPropertyConfigImpl1(pointer, configGroup, key, value, valueType, separatorType, configs, optionConfigs)
            } else {
                CwtPropertyConfigImpl2(pointer, configGroup, key, value, valueType, separatorType, configs)
            }
        } else {
            if (optionConfigs != null) {
                CwtPropertyConfigImpl3(pointer, configGroup, key, value, valueType, separatorType, optionConfigs)
            } else {
                CwtPropertyConfigImpl4(pointer, configGroup, key, value, valueType, separatorType)
            }
        }
    }

    override fun delegated(
        targetConfig: CwtPropertyConfig,
        configs: List<CwtMemberConfig<*>>?,
        parentConfig: CwtMemberConfig<*>?
    ): CwtPropertyConfig {
        return if (configs != null) {
            CwtPropertyConfigDelegate1(targetConfig, configs).apply { targetConfig.parentConfig = parentConfig }
        } else {
            CwtPropertyConfigDelegate2(targetConfig).apply { targetConfig.parentConfig = parentConfig }
        }
    }

    override fun delegatedWith(
        targetConfig: CwtPropertyConfig,
        key: String,
        value: String
    ): CwtPropertyConfig {
        return CwtPropertyConfigDelegateWith(targetConfig, key, value)
    }

    override fun copy(
        targetConfig: CwtPropertyConfig,
        pointer: SmartPsiElementPointer<out CwtProperty>,
        key: String,
        value: String,
        valueType: CwtType,
        separatorType: CwtSeparatorType,
        configs: List<CwtMemberConfig<*>>?,
        optionConfigs: List<CwtOptionMemberConfig<*>>?
    ): CwtPropertyConfig {
        return resolve(pointer, targetConfig.configGroup, key, value, valueType, separatorType, configs, optionConfigs)
    }
}

private abstract class CwtPropertyConfigImpl(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL
) : UserDataHolderBase(), CwtPropertyConfig {
    override val key = key.intern() // intern to optimize memory
    override val value = value.intern() // intern to optimize memory

    private val valueTypeId = valueType.optimizeValue() // use enum id as field to optimize memory
    override val valueType get() = valueTypeId.deoptimizeValue<CwtType>()

    private val separatorTypeId = separatorType.optimizeValue() // use enum id as field to optimize memory
    override val separatorType get() = separatorTypeId.deoptimizeValue<CwtSeparatorType>()

    // use memory-optimized lazy property
    @Volatile
    private var _valueConfig: Any? = EMPTY_OBJECT
    override val valueConfig @Synchronized get() = if (_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }

    override var parentConfig: CwtMemberConfig<*>? = null

    // cached into user data to optimize performance and memory
    override val keyExpression get() = getUserDataOrDefault(CwtMemberConfig.Keys.keyExpression)
    override val valueExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else getUserDataOrDefault(CwtMemberConfig.Keys.valueExpression)

    override fun toString() = "$key $separatorType $value"
}

// 12 + 9 * 4 + 2 * 1 = 50 -> 56
private class CwtPropertyConfigImpl1(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    configs: List<CwtMemberConfig<*>>? = null,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs = configs
    override val optionConfigs = optionConfigs
}

// 12 + 8 * 4 + 2 * 1 = 46 -> 48
private class CwtPropertyConfigImpl2(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs = configs
    override val optionConfigs get() = null
}

// 12 + 8 * 4 + 2 * 1 = 46 -> 48
private class CwtPropertyConfigImpl3(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
    override val optionConfigs = optionConfigs
}

// 12 + 7 * 4 + 2 * 1 = 42 -> 48
private class CwtPropertyConfigImpl4(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
    override val optionConfigs get() = null
}

private abstract class CwtPropertyConfigDelegate(
    private val delegate: CwtPropertyConfig,
) : UserDataHolderBase(), CwtPropertyConfig by delegate {
    // use memory-optimized lazy property
    @Volatile
    private var _valueConfig: Any? = EMPTY_OBJECT
    override val valueConfig @Synchronized get() = if (_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }

    override var parentConfig: CwtMemberConfig<*>? = null

    // cached into user data to optimize performance and memory
    override val keyExpression get() = getUserDataOrDefault(CwtMemberConfig.Keys.keyExpression)
    override val valueExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else getUserDataOrDefault(CwtMemberConfig.Keys.valueExpression)

    override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)

    override fun toString() = "$key $separatorType $value"
}

// 12 + 5 * 4 = 32 -> 32
private class CwtPropertyConfigDelegate1(
    delegate: CwtPropertyConfig,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtPropertyConfigDelegate(delegate) {
    override val configs = configs
}

// 12 + 4 * 4 = 28 -> 32
private class CwtPropertyConfigDelegate2(
    delegate: CwtPropertyConfig,
) : CwtPropertyConfigDelegate(delegate) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
}

// 12 + 6 * 4 = 36 -> 40
private class CwtPropertyConfigDelegateWith(
    delegate: CwtPropertyConfig,
    key: String,
    value: String,
    // configs should be always null here
) : CwtPropertyConfigDelegate(delegate) {
    override val key = key.intern() // intern to optimize memory
    override val value = value.intern() // intern to optimize memory

    // do not use cache here, since key and value are overridden
    override val keyExpression: CwtDataExpression get() = CwtDataExpression.resolve(value, true)
    override val valueExpression: CwtDataExpression get() = if (configs != null) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)
}

private fun CwtPropertyConfig.getValueConfig(): CwtValueConfig? {
    // this function should be enough fast because there are no pointers to be created
    val resolvedPointer = this.resolved().pointer
    val valuePointer = when {
        resolvedPointer is CwtPropertyPointer -> resolvedPointer.valuePointer
        else -> resolvedPointer.element?.propertyValue?.createPointer()
    } ?: return null
    return CwtValueConfig.resolveFromPropertyConfig(valuePointer, this)
}

private val CwtMemberConfig.Keys.keyExpression by createKey<_, CwtPropertyConfig>(CwtMemberConfig.Keys) { CwtDataExpression.resolve(key, true) }
private val CwtMemberConfig.Keys.valueExpression by createKey<_, CwtMemberConfig<*>>(CwtMemberConfig.Keys) { CwtDataExpression.resolve(value, false) }
