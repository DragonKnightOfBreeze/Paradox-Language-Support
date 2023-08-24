package icu.windea.pls.lang.cwt.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*

sealed interface CwtPropertyConfig : CwtMemberConfig<CwtProperty>, CwtKeyAware {
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
    return CwtPropertyConfigImpls.Impl(pointer, info, key, value, valueTypeId, separatorTypeId, configs, options, documentation)
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

fun CwtPropertyConfig.copyDelegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtPropertyConfig {
    return CwtPropertyConfigImpls.Delegate(this, configs).apply { this.parentConfig = parentConfig }
}

private object CwtPropertyConfigImpls {
    class Impl(
        override val pointer: SmartPsiElementPointer<out CwtProperty>,
        override val info: CwtConfigGroupInfo,
        override val key: String,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
        configs: List<CwtMemberConfig<*>>? = null,
        options: List<CwtOptionMemberConfig<*>>? = null,
        documentation: String? = null,
    ) : UserDataHolderBase(), CwtPropertyConfig {
        override val configs = configs
        override val options = options
        override val documentation = documentation
        
        //use memory-optimized lazy property
        private var _valueConfig: Any? = EMPTY_OBJECT
        override val valueConfig @Synchronized get() = if(_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }
        
        override var parentConfig: CwtMemberConfig<*>? = null
        override var inlineableConfig: CwtInlineableConfig<CwtProperty>? = null
        
        override val keyExpression: CwtKeyExpression get() = CwtKeyExpression.resolve(key)
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtKeyExpression get() = keyExpression
        
        override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
        override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
        
        override fun toString(): String = "$key ${separatorType.text} $value"
    }
    
    class Delegate(
        private val delegate: CwtPropertyConfig,
        configs: List<CwtMemberConfig<*>>? = null,
    ) : UserDataHolderBase(), CwtPropertyConfig by delegate {
        override val configs = configs
        
        //use memory-optimized lazy property
        private var _valueConfig: Any? = EMPTY_OBJECT
        override val valueConfig @Synchronized get() = if(_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }
        
        override var parentConfig: CwtMemberConfig<*>? = null
        override var inlineableConfig: CwtInlineableConfig<CwtProperty>? = null
        
        override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
        override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
        
        override fun <T : Any?> getUserData(key: Key<T>) = super.getUserData(key) ?: delegate.getUserData(key)
        override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
        
        override fun toString(): String = "$key ${separatorType.text} $value"
    }
}
