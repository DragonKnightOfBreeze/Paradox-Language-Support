package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

interface CwtPropertyConfig : CwtMemberConfig<CwtProperty>, CwtKeyAware {
    val keyExpression: CwtKeyExpression
    
    val valueConfig: CwtValueConfig?
    
    companion object {
        val EmptyConfig: CwtPropertyConfig by lazy { resolve(emptyPointer(), CwtConfigGroupInfo(""), "", "") }
    }
}

fun CwtPropertyConfig.Companion.resolve(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    info: CwtConfigGroupInfo,
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
            CwtPropertyConfigImpls.Impl1(pointer, info, key, value, valueTypeId, separatorTypeId, configs, options, documentation)
        } else {
            CwtPropertyConfigImpls.Impl2(pointer, info, key, value, valueTypeId, separatorTypeId, configs)
        }
    } else {
        if(options != null || documentation != null) {
            CwtPropertyConfigImpls.Impl3(pointer, info, key, value, valueTypeId, separatorTypeId, options, documentation)
        } else {
            CwtPropertyConfigImpls.Impl4(pointer, info, key, value, valueTypeId, separatorTypeId)
        }
    }
}

fun CwtPropertyConfig.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtPropertyConfig {
    return if(configs != null) {
        CwtPropertyConfigImpls.Delegate1(this, configs).apply { this.parentConfig = parentConfig }
    } else {
        CwtPropertyConfigImpls.Delegate2(this).apply { this.parentConfig = parentConfig }
    }
}

fun CwtPropertyConfig.delegatedWith(key: String, value: String): CwtPropertyConfig {
    return CwtPropertyConfigImpls.DelegateWith(this, key, value)
}

fun CwtPropertyConfig.copy(
    pointer: SmartPsiElementPointer<out CwtProperty> = this.pointer,
    info: CwtConfigGroupInfo = this.info,
    key: String = this.key,
    value: String = this.value,
    valueTypeId: @EnumId(CwtType::class) Byte = this.valueTypeId,
    separatorTypeId: @EnumId(CwtSeparatorType::class) Byte = this.separatorTypeId,
    configs: List<CwtMemberConfig<*>>? = this.configs,
    options: List<CwtOptionMemberConfig<*>>? = this.options,
    documentation: String? = this.documentation
): CwtPropertyConfig {
    return CwtPropertyConfig.resolve(pointer, info, key, value, valueTypeId, separatorTypeId, configs, options, documentation)
}

//Implementations

private object CwtPropertyConfigImpls {
    abstract class Impl(
        override val pointer: SmartPsiElementPointer<out CwtProperty>,
        override val info: CwtConfigGroupInfo,
        override val key: String,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
    ) : UserDataHolderBase(), CwtPropertyConfig {
        //use memory-optimized lazy property
        private var _valueConfig: Any? = EMPTY_OBJECT
        override val valueConfig @Synchronized get() = if(_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }
        
        override var parentConfig: CwtMemberConfig<*>? = null
        override var inlineableConfig: CwtInlineableConfig<CwtProperty, CwtMemberConfig<CwtProperty>>? = null
        
        override val keyExpression: CwtKeyExpression get() = CwtKeyExpression.resolve(key)
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtKeyExpression get() = keyExpression
        
        override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
        override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
        
        override fun toString(): String = "$key ${separatorType.text} $value"
    }
    
    //12 + 11 * 4 + 2 * 1 = 58 => 64
    class Impl1(
        pointer: SmartPsiElementPointer<out CwtProperty>,
        info: CwtConfigGroupInfo,
        key: String,
        value: String,
        valueTypeId: Byte = CwtType.String.id,
        separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
        configs: List<CwtMemberConfig<*>>? = null,
        options: List<CwtOptionMemberConfig<*>>? = null,
        documentation: String? = null,
    ) : Impl(pointer, info, key, value, valueTypeId, separatorTypeId) {
        override val configs = configs
        override val options = options
        override val documentation = documentation
    }
    
    //12 + 9 * 4 + 2 * 1 = 50 => 56
    class Impl2(
        pointer: SmartPsiElementPointer<out CwtProperty>,
        info: CwtConfigGroupInfo,
        key: String,
        value: String,
        valueTypeId: Byte = CwtType.String.id,
        separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
        configs: List<CwtMemberConfig<*>>? = null,
    ) : Impl(pointer, info, key, value, valueTypeId, separatorTypeId) {
        override val configs = configs
        override val options get() = null
        override val documentation get() = null
    }
    
    //12 + 10 * 4 + 2 * 1 = 54 => 66
    class Impl3(
        pointer: SmartPsiElementPointer<out CwtProperty>,
        info: CwtConfigGroupInfo,
        key: String,
        value: String,
        valueTypeId: Byte = CwtType.String.id,
        separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
        options: List<CwtOptionMemberConfig<*>>? = null,
        documentation: String? = null,
    ) : Impl(pointer, info, key, value, valueTypeId, separatorTypeId) {
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val options = options
        override val documentation = documentation
    }
    
    //12 + 8 * 4 + 2 * 1 = 46 => 48
    class Impl4(
        pointer: SmartPsiElementPointer<out CwtProperty>,
        info: CwtConfigGroupInfo,
        key: String,
        value: String,
        valueTypeId: Byte = CwtType.String.id,
        separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
    ) : Impl(pointer, info, key, value, valueTypeId, separatorTypeId) {
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val options get() = null
        override val documentation get() = null
    }
    
    abstract class Delegate(
        private val delegate: CwtPropertyConfig,
    ) : UserDataHolderBase(), CwtPropertyConfig by delegate {
        //use memory-optimized lazy property
        private var _valueConfig: Any? = EMPTY_OBJECT
        override val valueConfig @Synchronized get() = if(_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }
        
        override var parentConfig: CwtMemberConfig<*>? = null
        override var inlineableConfig: CwtInlineableConfig<CwtProperty, CwtMemberConfig<CwtProperty>>? = null
        
        override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
        override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
        
        override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
        override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
        
        override fun toString(): String = "$key ${separatorType.text} $value"
    }
    
    //12 + 6 * 4 = 36 => 40
    class Delegate1(
        delegate: CwtPropertyConfig,
        configs: List<CwtMemberConfig<*>>? = null,
    ) : Delegate(delegate) {
        override val configs = configs
    }
    
    //12 + 5 * 4 = 32 => 32
    class Delegate2(
        delegate: CwtPropertyConfig,
    ) : Delegate(delegate) {
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    }
    
    //12 + 6 * 4 = 28 -> 40
    class DelegateWith(
        delegate: CwtPropertyConfig,
        override val key: String,
        override val value: String,
        //configs should be always null here
    ) : Delegate(delegate) {
        override val keyExpression: CwtKeyExpression get() = CwtKeyExpression.resolve(key)
        override val valueExpression: CwtValueExpression get() = CwtValueExpression.resolve(value)
        override val expression: CwtDataExpression get() = keyExpression
        
        override fun toString(): String = "$key ${separatorType.text} $value"
    }
}
