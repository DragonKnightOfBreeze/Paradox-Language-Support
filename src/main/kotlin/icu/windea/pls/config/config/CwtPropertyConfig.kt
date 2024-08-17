package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

interface CwtPropertyConfig : CwtMemberConfig<CwtProperty> {
    val key: String
    val separatorTypeId: @EnumId(CwtSeparatorType::class) Byte //use enum id to optimize memory 
    val separatorType: CwtSeparatorType get() = CwtSeparatorType.resolve(separatorTypeId)
    
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
    valueTypeId: @EnumId(CwtType::class) Byte = CwtType.String.id,
    separatorTypeId: @EnumId(CwtSeparatorType::class) Byte = CwtSeparatorType.EQUAL.id,
    configs: List<CwtMemberConfig<*>>? = null,
    options: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null
): CwtPropertyConfig {
    return if(configs != null) {
        if(options != null || documentation != null) {
            CwtPropertyConfigImpl1(pointer, configGroup, key, value, valueTypeId, separatorTypeId, configs, options, documentation)
        } else {
            CwtPropertyConfigImpl2(pointer, configGroup, key, value, valueTypeId, separatorTypeId, configs)
        }
    } else {
        if(options != null || documentation != null) {
            CwtPropertyConfigImpl3(pointer, configGroup, key, value, valueTypeId, separatorTypeId, options, documentation)
        } else {
            CwtPropertyConfigImpl4(pointer, configGroup, key, value, valueTypeId, separatorTypeId)
        }
    }
}

fun CwtPropertyConfig.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtPropertyConfig {
    return if(configs != null) {
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
    valueTypeId: @EnumId(CwtType::class) Byte = this.valueTypeId,
    separatorTypeId: @EnumId(CwtSeparatorType::class) Byte = this.separatorTypeId,
    configs: List<CwtMemberConfig<*>>? = this.configs,
    options: List<CwtOptionMemberConfig<*>>? = this.optionConfigs,
    documentation: String? = this.documentation
): CwtPropertyConfig {
    return CwtPropertyConfig.resolve(pointer, this.configGroup, key, value, valueTypeId, separatorTypeId, configs, options, documentation)
}

//Implementations

private abstract class CwtPropertyConfigImpl(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val configGroup: CwtConfigGroup,
    override val key: String,
    override val value: String,
    override val valueTypeId: Byte = CwtType.String.id,
    override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
) : UserDataHolderBase(), CwtPropertyConfig {
    //use memory-optimized lazy property
    private var _valueConfig: Any? = EMPTY_OBJECT
    override val valueConfig @Synchronized get() = if(_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }
    
    override var parentConfig: CwtMemberConfig<*>? = null
    
    override fun toString(): String = "$key ${separatorType.text} $value"
}

//12 + 10 * 4 + 2 * 1 = 54 -> 56
private class CwtPropertyConfigImpl1(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueTypeId: Byte = CwtType.String.id,
    separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
    configs: List<CwtMemberConfig<*>>? = null,
    options: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueTypeId, separatorTypeId) {
    override val configs = configs?.toMutableIfNotEmptyInActual()
    override val optionConfigs = options?.toMutableIfNotEmptyInActual()
    override val documentation = documentation
}

//12 + 8 * 4 + 2 * 1 = 46 -> 48
private class CwtPropertyConfigImpl2(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueTypeId: Byte = CwtType.String.id,
    separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueTypeId, separatorTypeId) {
    override val configs = configs?.toMutableIfNotEmptyInActual()
    override val optionConfigs get() = null
    override val documentation get() = null
}

//12 + 9 * 4 + 2 * 1 = 50 -> 56
private class CwtPropertyConfigImpl3(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueTypeId: Byte = CwtType.String.id,
    separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
    options: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueTypeId, separatorTypeId) {
    override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    override val optionConfigs = options?.toMutableIfNotEmptyInActual()
    override val documentation = documentation
}

//12 + 7 * 4 + 2 * 1 = 42 -> 48
private class CwtPropertyConfigImpl4(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueTypeId: Byte = CwtType.String.id,
    separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueTypeId, separatorTypeId) {
    override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    override val optionConfigs get() = null
    override val documentation get() = null
}

private abstract class CwtPropertyConfigDelegate(
    private val delegate: CwtPropertyConfig,
) : UserDataHolderBase(), CwtPropertyConfig by delegate {
    //use memory-optimized lazy property
    private var _valueConfig: Any? = EMPTY_OBJECT
    override val valueConfig @Synchronized get() = if(_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }
    
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
    override val configs = configs?.toMutableIfNotEmptyInActual()
}

//12 + 4 * 4 = 28 -> 32
private class CwtPropertyConfigDelegate2(
    delegate: CwtPropertyConfig,
) : CwtPropertyConfigDelegate(delegate) {
    override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
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
    //this function should be enough fast because there is no pointers to be created
    val resolvedPointer = resolved().pointer
    val valuePointer = when {
        resolvedPointer is CwtPropertyPointer -> resolvedPointer.valuePointer
        else -> resolvedPointer.element?.propertyValue?.createPointer()
    } ?: return null
    return CwtValueConfig.resolveFromPropertyConfig(valuePointer, this)
}
