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
    
    object Keys: KeyAware
}

val CwtPropertyConfig.Keys.configs by createKey<List<CwtMemberConfig<*>>?>("cwt.propertyConfig.configs")
val CwtPropertyConfig.Keys.options by createKey<List<CwtOptionMemberConfig<*>>?>("cwt.propertyConfig.options")
val CwtPropertyConfig.Keys.documentation by createKey<String?>("cwt.propertyConfig.documentation")
val CwtPropertyConfig.Keys.valueConfig by createKey<CwtValueConfig?>("cwt.propertyConfig.valueConfig")
val CwtPropertyConfig.Keys.parentConfig by createKey<CwtMemberConfig<*>?>("cwt.propertyConfig.parentConfig")
val CwtPropertyConfig.Keys.inlineableConfig by createKey<CwtInlineableConfig<CwtProperty>?>("cwt.propertyConfig.inlineableConfig")

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
    return CwtPropertyConfigImpls.Impl(pointer, info, key, value, valueTypeId, separatorTypeId, configs, options, documentation)
}

fun CwtPropertyConfig.copyDelegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtPropertyConfig {
    return CwtPropertyConfigImpls.Delegate(this, configs, parentConfig)
}

private object CwtPropertyConfigImpls {
    // 12 + 5 * 4 + 2 * 1 = 34 => 40b
    
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
        override val configs by CwtPropertyConfig.Keys.configs
        override val options by CwtPropertyConfig.Keys.options
        override val documentation by CwtPropertyConfig.Keys.documentation
        override val valueConfig by CwtPropertyConfig.Keys.valueConfig
        override var parentConfig by CwtPropertyConfig.Keys.parentConfig
        override var inlineableConfig by CwtPropertyConfig.Keys.inlineableConfig
        
        override val keyExpression: CwtKeyExpression get() = CwtKeyExpression.resolve(key)
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtKeyExpression get() = keyExpression
        
        override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
        override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
        
        override fun toString(): String = "$key ${separatorType.text} $value"
        
        //should put after delegate properties
        
        init {
            putUserData(CwtPropertyConfig.Keys.configs, configs)
            putUserData(CwtPropertyConfig.Keys.options, options)
            putUserData(CwtPropertyConfig.Keys.documentation, documentation)
            putUserData(CwtPropertyConfig.Keys.valueConfig, getValueConfig())
        }
    }
    
    // 12 + 2 * 4 = 20 => 24b
    
    class Delegate(
        delegate: CwtPropertyConfig,
        configs: List<CwtMemberConfig<*>>? = null,
        parentConfig: CwtMemberConfig<*>? = null,
    ) : UserDataHolderBase(), CwtPropertyConfig by delegate {
        override val configs by CwtPropertyConfig.Keys.configs
        override val valueConfig by CwtPropertyConfig.Keys.valueConfig
        override var parentConfig by CwtPropertyConfig.Keys.parentConfig
        override var inlineableConfig by CwtPropertyConfig.Keys.inlineableConfig
        
        override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
        override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
        
        override fun <T : Any?> getUserData(key: Key<T>) = super.getUserData(key)
        override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
        
        override fun toString(): String = "$key ${separatorType.text} $value"
        
        //should put after delegate properties
        
        init {
            putUserData(CwtPropertyConfig.Keys.configs, configs)
            putUserData(CwtPropertyConfig.Keys.valueConfig, getValueConfig())
            putUserData(CwtPropertyConfig.Keys.parentConfig, parentConfig)
        }
    }
}
