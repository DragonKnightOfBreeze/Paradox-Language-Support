package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

interface CwtValueConfig : CwtMemberConfig<CwtValue> {
    val propertyConfig: CwtPropertyConfig?

    override val expression: CwtDataExpression get() = valueExpression

    companion object
}

//Resolve Methods

fun CwtValueConfig.Companion.resolve(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    configs: List<CwtMemberConfig<*>>? = null,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
    propertyConfig: CwtPropertyConfig? = null
): CwtValueConfig {
    return if (configs != null) {
        if (optionConfigs != null || documentation != null) {
            CwtValueConfigImpl1(pointer, configGroup, value, valueType, configs, optionConfigs, documentation, propertyConfig)
        } else {
            CwtValueConfigImpl2(pointer, configGroup, value, valueType, configs, propertyConfig)
        }
    } else {
        if (optionConfigs != null || documentation != null) {
            CwtValueConfigImpl3(pointer, configGroup, value, valueType, optionConfigs, documentation, propertyConfig)
        } else {
            CwtValueConfigImpl4(pointer, configGroup, value, valueType, propertyConfig)
        }
    }
}

fun CwtValueConfig.Companion.resolveFromPropertyConfig(
    pointer: SmartPsiElementPointer<out CwtValue>,
    propertyConfig: CwtPropertyConfig
): CwtValueConfig {
    return CwtValueConfigFromPropertyConfig(pointer, propertyConfig)
}

fun CwtValueConfig.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig,
): CwtValueConfig {
    return if (configs != null) {
        CwtValueConfigDelegate1(this, configs).apply { this.parentConfig = parentConfig }
    } else {
        CwtValueConfigDelegate2(this).apply { this.parentConfig = parentConfig }
    }
}

fun CwtValueConfig.delegatedWith(value: String): CwtValueConfig {
    return CwtValueConfigDelegateWith(this, value)
}

fun CwtValueConfig.copy(
    pointer: SmartPsiElementPointer<out CwtValue> = this.pointer,
    value: String = this.value,
    valueType: CwtType = this.valueType,
    configs: List<CwtMemberConfig<*>>? = this.configs,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = this.optionConfigs,
    documentation: String? = this.documentation,
    propertyConfig: CwtPropertyConfig? = this.propertyConfig,
): CwtValueConfig {
    return CwtValueConfig.resolve(pointer, this.configGroup, value, valueType, configs, optionConfigs, documentation, propertyConfig)
}

class CwtPropertyPointer(
    private val delegate: SmartPsiElementPointer<CwtProperty>
) : SmartPsiElementPointer<CwtProperty> by delegate {
    val valuePointer: SmartPsiElementPointer<CwtValue>? = delegate.element?.propertyValue?.createPointer()
}

//Implementations

private abstract class CwtValueConfigImpl(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val configGroup: CwtConfigGroup,
    override val value: String,
    valueType: CwtType = CwtType.String,
    propertyConfig: CwtPropertyConfig? = null,
) : UserDataHolderBase(), CwtValueConfig {
    private val valueTypeId = valueType.optimizeValue() //use enum id to optimize memory
    override val valueType get() = valueTypeId.deoptimizeValue<CwtType>()

    override val propertyConfig = propertyConfig

    override var parentConfig: CwtMemberConfig<*>? = null

    //not cached to optimize memory
    override val valueExpression get() = if (isBlock) CwtDataExpression.BlockExpression else CwtDataExpression.resolve(value, false)

    override fun toString(): String = value
}

//12 + 9 * 4 + 1 * 1 = 49 -> 56
private class CwtValueConfigImpl1(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    configs: List<CwtMemberConfig<*>>? = null,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig) {
    override val configs = configs
    override val optionConfigs = optionConfigs
    override val documentation = documentation
}

//12 + 7 * 4 + 1 * 1 = 41 -> 48
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
    override val documentation get() = null
}

//12 + 8 * 4 + 1 * 1 = 45 -> 48
private class CwtValueConfigImpl3(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
    override val optionConfigs = optionConfigs
    override val documentation = documentation
}

//12 + 6 * 4 + 1 * 1 = 37 -> 40
private class CwtValueConfigImpl4(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueType: CwtType = CwtType.String,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueType, propertyConfig) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
    override val optionConfigs get() = null
    override val documentation get() = null
}

private abstract class CwtValueConfigDelegate(
    private val delegate: CwtValueConfig,
) : UserDataHolderBase(), CwtValueConfig by delegate {
    override var parentConfig: CwtMemberConfig<*>? = null

    //not cached to optimize memory
    override val valueExpression get() = if (isBlock) CwtDataExpression.BlockExpression else CwtDataExpression.resolve(value, false)

    override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)

    override fun toString(): String = value
}

//12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegate1(
    delegate: CwtValueConfig,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtValueConfigDelegate(delegate) {
    override val configs = configs
}

//12 + 3 * 4 = 24 -> 24
private class CwtValueConfigDelegate2(
    delegate: CwtValueConfig,
) : CwtValueConfigDelegate(delegate) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
}

//12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegateWith(
    delegate: CwtValueConfig,
    override val value: String,
    //configs should be always null here
) : CwtValueConfigDelegate(delegate)

//12 + 4 * 4 = 28 -> 32 
private class CwtValueConfigFromPropertyConfig(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val propertyConfig: CwtPropertyConfig,
) : UserDataHolderBase(), CwtValueConfig {
    override val configGroup get() = propertyConfig.configGroup
    override val value get() = propertyConfig.value
    override val valueType get() = propertyConfig.valueType
    override val configs get() = propertyConfig.configs
    override val optionConfigs get() = propertyConfig.optionConfigs
    override val documentation get() = propertyConfig.documentation

    override var parentConfig: CwtMemberConfig<*>? = null

    //not cached to optimize memory
    override val valueExpression get() = if (isBlock) CwtDataExpression.BlockExpression else CwtDataExpression.resolve(value, false)

    override fun toString(): String = value
}
