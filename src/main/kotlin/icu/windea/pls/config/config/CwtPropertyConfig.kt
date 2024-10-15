package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

interface CwtPropertyConfig : CwtMemberConfig<CwtProperty> {
    val key: String
    val separatorType: CwtSeparatorType

    val valueConfig: CwtValueConfig?

    val keyExpression: CwtDataExpression get() = CwtDataExpression.resolve(key, true)
    override val expression: CwtDataExpression get() = keyExpression

    companion object
}

//Resolve Methods

fun CwtPropertyConfig.Companion.resolve(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    configs: List<CwtMemberConfig<*>>? = null,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null
): CwtPropertyConfig {
    return if (configs != null) {
        if (optionConfigs != null || documentation != null) {
            CwtPropertyConfigImpl1(pointer, configGroup, key, value, valueType, separatorType, configs, optionConfigs, documentation)
        } else {
            CwtPropertyConfigImpl2(pointer, configGroup, key, value, valueType, separatorType, configs)
        }
    } else {
        if (optionConfigs != null || documentation != null) {
            CwtPropertyConfigImpl3(pointer, configGroup, key, value, valueType, separatorType, optionConfigs, documentation)
        } else {
            CwtPropertyConfigImpl4(pointer, configGroup, key, value, valueType, separatorType)
        }
    }
}

fun CwtPropertyConfig.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtPropertyConfig {
    return if (configs != null) {
        CwtPropertyConfigDelegate1(this, configs).apply { this.parentConfig = parentConfig }
    } else {
        CwtPropertyConfigDelegate2(this).apply { this.parentConfig = parentConfig }
    }
}

fun CwtPropertyConfig.delegatedWith(key: String, value: String): CwtPropertyConfig {
    return CwtPropertyConfigDelegateWith(this, key, value)
}

fun CwtPropertyConfig.copy(
    pointer: SmartPsiElementPointer<out CwtProperty> = this.pointer,
    key: String = this.key,
    value: String = this.value,
    valueType: CwtType = this.valueType,
    separatorType: CwtSeparatorType = this.separatorType,
    configs: List<CwtMemberConfig<*>>? = this.configs,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = this.optionConfigs,
    documentation: String? = this.documentation
): CwtPropertyConfig {
    return CwtPropertyConfig.resolve(pointer, this.configGroup, key, value, valueType, separatorType, configs, optionConfigs, documentation)
}

//Implementations

private abstract class CwtPropertyConfigImpl(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val configGroup: CwtConfigGroup,
    override val key: String,
    override val value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
) : UserDataHolderBase(), CwtPropertyConfig {
    private val valueTypeId: Byte = valueType.optimizeValue() //use enum id as field to optimize memory 
    override val valueType: CwtType get() = valueTypeId.deoptimizeValue()

    private val separatorTypeId: Byte = separatorType.optimizeValue() //use enum id as field to optimize memory
    override val separatorType: CwtSeparatorType get() = separatorTypeId.deoptimizeValue()

    //use memory-optimized lazy property
    @Volatile
    private var _valueConfig: Any? = EMPTY_OBJECT
    override val valueConfig @Synchronized get() = if (_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }

    override var parentConfig: CwtMemberConfig<*>? = null

    override fun toString(): String = "$key ${separatorType.text} $value"
}

//12 + 10 * 4 + 2 * 1 = 54 -> 56
private class CwtPropertyConfigImpl1(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    configs: List<CwtMemberConfig<*>>? = null,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs = configs
    override val optionConfigs = optionConfigs
    override val documentation = documentation
}

//12 + 8 * 4 + 2 * 1 = 46 -> 48
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
    override val documentation get() = null
}

//12 + 9 * 4 + 2 * 1 = 50 -> 56
private class CwtPropertyConfigImpl3(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs: List<CwtMemberConfig<*>>? get() = if (valueType == CwtType.Block) emptyList() else null
    override val optionConfigs = optionConfigs
    override val documentation = documentation
}

//12 + 7 * 4 + 2 * 1 = 42 -> 48
private class CwtPropertyConfigImpl4(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs: List<CwtMemberConfig<*>>? get() = if (valueType == CwtType.Block) emptyList() else null
    override val optionConfigs get() = null
    override val documentation get() = null
}

private abstract class CwtPropertyConfigDelegate(
    private val delegate: CwtPropertyConfig,
) : UserDataHolderBase(), CwtPropertyConfig by delegate {
    //use memory-optimized lazy property
    @Volatile
    private var _valueConfig: Any? = EMPTY_OBJECT
    override val valueConfig @Synchronized get() = if (_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }

    override var parentConfig: CwtMemberConfig<*>? = null

    override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)

    override fun toString(): String = "$key ${separatorType.text} $value"
}

//12 + 5 * 4 = 32 -> 32
private class CwtPropertyConfigDelegate1(
    delegate: CwtPropertyConfig,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtPropertyConfigDelegate(delegate) {
    override val configs = configs
}

//12 + 4 * 4 = 28 -> 32
private class CwtPropertyConfigDelegate2(
    delegate: CwtPropertyConfig,
) : CwtPropertyConfigDelegate(delegate) {
    override val configs: List<CwtMemberConfig<*>>? get() = if (valueType == CwtType.Block) emptyList() else null
}

//12 + 6 * 4 = 36 -> 40
private class CwtPropertyConfigDelegateWith(
    delegate: CwtPropertyConfig,
    override val key: String,
    override val value: String,
    //configs should be always null here
) : CwtPropertyConfigDelegate(delegate) {
    override fun toString(): String = "$key ${separatorType.text} $value"
}

private fun CwtPropertyConfig.getValueConfig(): CwtValueConfig? {
    //this function should be enough fast because there are no pointers to be created
    val resolvedPointer = this.resolved().pointer
    val valuePointer = when {
        resolvedPointer is CwtPropertyPointer -> resolvedPointer.valuePointer
        else -> resolvedPointer.element?.propertyValue?.createPointer()
    } ?: return null
    return CwtValueConfig.resolveFromPropertyConfig(valuePointer, this)
}
