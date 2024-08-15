package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

interface CwtValueConfig : CwtMemberConfig<CwtValue> {
    val propertyConfig: CwtPropertyConfig?
    
    companion object
}

//Resolve Methods

fun CwtValueConfig.Companion.resolve(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueTypeId: @EnumId(CwtType::class) Byte = CwtType.String.id,
    configs: List<CwtMemberConfig<*>>? = null,
    options: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
    propertyConfig: CwtPropertyConfig? = null
): CwtValueConfig {
    return if(configs != null) {
        if(options != null || documentation != null) {
            CwtValueConfigImpl1(pointer, configGroup, value, valueTypeId, configs, options, documentation, propertyConfig)
        } else {
            CwtValueConfigImpl2(pointer, configGroup, value, valueTypeId, configs, propertyConfig)
        }
    } else {
        if(options != null || documentation != null) {
            CwtValueConfigImpl3(pointer, configGroup, value, valueTypeId, options, documentation, propertyConfig)
        } else {
            CwtValueConfigImpl4(pointer, configGroup, value, valueTypeId, propertyConfig)
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
    return if(configs != null) {
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
    valueTypeId: @EnumId(CwtType::class) Byte = this.valueTypeId,
    configs: List<CwtMemberConfig<*>>? = this.configs,
    options: List<CwtOptionMemberConfig<*>>? = this.optionConfigs,
    documentation: String? = this.documentation,
    propertyConfig: CwtPropertyConfig? = this.propertyConfig,
): CwtValueConfig {
    return CwtValueConfig.resolve(pointer, this.configGroup, value, valueTypeId, configs, options, documentation, propertyConfig)
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
    override val valueTypeId: Byte = CwtType.String.id,
    propertyConfig: CwtPropertyConfig? = null,
) : UserDataHolderBase(), CwtValueConfig {
    override val propertyConfig = propertyConfig
    
    override var parentConfig: CwtMemberConfig<*>? = null
    
    override fun toString(): String = value
}

//12 + 9 * 4 + 2 * 1 = 50 -> 56
private class CwtValueConfigImpl1(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueTypeId: Byte = CwtType.String.id,
    configs: List<CwtMemberConfig<*>>? = null,
    options: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueTypeId, propertyConfig) {
    override val configs = configs?.toMutableIfNotEmptyInActual()
    override val optionConfigs = options?.toMutableIfNotEmptyInActual()
    override val documentation = documentation
}

//12 + 7 * 4 + 2 * 1 = 42 -> 48
private class CwtValueConfigImpl2(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueTypeId: Byte = CwtType.String.id,
    configs: List<CwtMemberConfig<*>>? = null,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueTypeId, propertyConfig) {
    override val configs = configs?.toMutableIfNotEmptyInActual()
    override val optionConfigs get() = null
    override val documentation get() = null
}

//12 + 8 * 4 + 2 * 1 = 46 -> 48
private class CwtValueConfigImpl3(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueTypeId: Byte = CwtType.String.id,
    options: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueTypeId, propertyConfig) {
    override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    override val optionConfigs = options?.toMutableIfNotEmptyInActual()
    override val documentation = documentation
}

//12 + 6 * 4 + 2 * 1 = 38 -> 40
private class CwtValueConfigImpl4(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    value: String,
    valueTypeId: Byte = CwtType.String.id,
    propertyConfig: CwtPropertyConfig? = null,
) : CwtValueConfigImpl(pointer, configGroup, value, valueTypeId, propertyConfig) {
    override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    override val optionConfigs get() = null
    override val documentation get() = null
}

private abstract class CwtValueConfigDelegate(
    private val delegate: CwtValueConfig,
) : UserDataHolderBase(), CwtValueConfig by delegate {
    override var parentConfig: CwtMemberConfig<*>? = null
    
    override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
    
    override fun toString(): String = value
}

//12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegate1(
    delegate: CwtValueConfig,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtValueConfigDelegate(delegate) {
    override val configs = configs?.toMutableIfNotEmptyInActual()
}

//12 + 3 * 4 = 24 -> 24
private class CwtValueConfigDelegate2(
    delegate: CwtValueConfig,
) : CwtValueConfigDelegate(delegate) {
    override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
}

//12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegateWith(
    delegate: CwtValueConfig,
    override val value: String,
    //configs should be always null here
) : CwtValueConfigDelegate(delegate) {
    override fun toString(): String = value
}

//12 + 4 * 4 = 28 -> 32 
private class CwtValueConfigFromPropertyConfig(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val propertyConfig: CwtPropertyConfig,
) : UserDataHolderBase(), CwtValueConfig {
    override val configGroup: CwtConfigGroup get() = propertyConfig.configGroup
    override val value: String get() = propertyConfig.value
    override val valueTypeId: Byte get() = propertyConfig.valueTypeId
    override val documentation: String? get() = propertyConfig.documentation
    override val optionConfigs: List<CwtOptionMemberConfig<*>>? get() = propertyConfig.optionConfigs
    override val configs: List<CwtMemberConfig<*>>? get() = propertyConfig.configs
    
    override var parentConfig: CwtMemberConfig<*>? = null
    
    override fun <T : Any?> getUserData(key: Key<T>) = super.getUserData(key) ?: propertyConfig.getUserData(key)
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
    
    override fun toString(): String = value
}
