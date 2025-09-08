package icu.windea.pls.config.config.impl

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.isBlock
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue

class CwtValueConfigResolverImpl : CwtValueConfig.Resolver {
    override fun resolve(
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

    override fun resolveFromPropertyConfig(
        pointer: SmartPsiElementPointer<out CwtValue>,
        propertyConfig: CwtPropertyConfig
    ): CwtValueConfig {
        return CwtValueConfigFromPropertyConfig(pointer, propertyConfig)
    }

    override fun delegated(
        targetConfig: CwtValueConfig,
        configs: List<CwtMemberConfig<*>>?,
        parentConfig: CwtMemberConfig<*>?,
    ): CwtValueConfig {
        return if (configs != null) {
            CwtValueConfigDelegate1(targetConfig, configs).apply { targetConfig.parentConfig = parentConfig }
        } else {
            CwtValueConfigDelegate2(targetConfig).apply { targetConfig.parentConfig = parentConfig }
        }
    }

    override fun delegatedWith(
        targetConfig: CwtValueConfig,
        value: String
    ): CwtValueConfig {
        return CwtValueConfigDelegateWith(targetConfig, value)
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
        return resolve(pointer, targetConfig.configGroup, value, valueType, configs, optionConfigs, propertyConfig)
    }
}

private abstract class CwtValueConfigImpl(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    propertyConfig: CwtPropertyConfig? = null,
) : UserDataHolderBase(), CwtValueConfig {
    override val value = value.intern() // intern to optimize memory

    private val valueTypeId = valueType.optimizeValue() // use enum id to optimize memory
    override val valueType get() = valueTypeId.deoptimizeValue<CwtType>()

    override val propertyConfig = propertyConfig

    override var parentConfig: CwtMemberConfig<*>? = null

    // not cached to optimize memory
    override val valueExpression get() = if (isBlock) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)

    override fun toString(): String = value
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
    override val configs = configs
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
    override val configs = configs
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
    override var parentConfig: CwtMemberConfig<*>? = null

    // not cached to optimize memory
    override val valueExpression get() = if (isBlock) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)

    override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)

    override fun toString(): String = value
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegate1(
    delegate: CwtValueConfig,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtValueConfigDelegate(delegate) {
    override val configs = configs
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
    override val value = value.intern() // intern to optimize memory
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

    override var parentConfig: CwtMemberConfig<*>? = null

    // not cached to optimize memory
    override val valueExpression get() = if (isBlock) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)

    override fun toString(): String = value
}
