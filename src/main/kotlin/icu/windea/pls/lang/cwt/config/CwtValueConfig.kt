package icu.windea.pls.lang.cwt.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*

sealed interface CwtValueConfig : CwtMemberConfig<CwtValue>, CwtValueAware {
    val propertyConfig: CwtPropertyConfig?
    
    companion object {
        val EmptyConfig: CwtValueConfig = resolve(emptyPointer(), CwtConfigGroupInfo(""), "")
    }
    
    object Keys: KeyAware
}

val CwtValueConfig.Keys.configs by createKey<List<CwtMemberConfig<*>>?>("cwt.valueConfig.configs")
val CwtValueConfig.Keys.options by createKey<List<CwtOptionMemberConfig<*>>?>("cwt.valueConfig.options")
val CwtValueConfig.Keys.documentation by createKey<String?>("cwt.valueConfig.documentation")
val CwtValueConfig.Keys.propertyConfig by createKey<CwtPropertyConfig?>("cwt.valueConfig.propertyConfig")
val CwtValueConfig.Keys.parentConfig by createKey<CwtMemberConfig<*>?>("cwt.valueConfig.parentConfig")
val CwtValueConfig.Keys.inlineableConfig by createKey<CwtInlineableConfig<CwtValue>?>("cwt.valueConfig.inlineableConfig")

fun CwtValueConfig.Companion.resolve(
    pointer: SmartPsiElementPointer<out CwtValue>,
    info: CwtConfigGroupInfo,
    value: String,
    valueTypeId: @EnumId(CwtType::class) Byte = CwtType.String.id,
    configs: List<CwtMemberConfig<*>>? = null,
    options: List<CwtOptionMemberConfig<*>>? = null,
    documentation: String? = null,
    propertyConfig: CwtPropertyConfig? = null
): CwtValueConfig {
    return CwtValueConfigImpls.Impl(pointer, info, value, valueTypeId, configs, options, documentation, propertyConfig)
}

fun CwtValueConfig.copy(
    pointer: SmartPsiElementPointer<out CwtValue> = this.pointer,
    info: CwtConfigGroupInfo = this.info,
    value: String = this.value,
    valueTypeId: @EnumId(CwtType::class) Byte = this.valueTypeId,
    configs: List<CwtMemberConfig<*>>? = this.configs,
    options: List<CwtOptionMemberConfig<*>>? = this.options,
    documentation: String? = this.documentation,
    propertyConfig: CwtPropertyConfig? = this.propertyConfig,
): CwtValueConfig {
    return CwtValueConfigImpls.Impl(pointer, info, value, valueTypeId, configs, options, documentation, propertyConfig)
}

fun CwtValueConfig.copyDelegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig,
): CwtValueConfig {
    return CwtValueConfigImpls.Delegate(this, configs, parentConfig)
}

fun CwtPropertyConfig.getValueConfig(): CwtValueConfig? {
    //this function should be enough fast because there is no pointers to be created
    val resolvedPointer = resolved().pointer
    val valuePointer = when {
        resolvedPointer is CwtPropertyPointer -> resolvedPointer.valuePointer
        else -> resolvedPointer.element?.propertyValue?.createPointer()
    } ?: return null
    return CwtValueConfigImpls.FromPropertyConfig(valuePointer, this)
}

class CwtPropertyPointer(
    private val delegate: SmartPsiElementPointer<CwtProperty>
) : SmartPsiElementPointer<CwtProperty> by delegate {
    val valuePointer: SmartPsiElementPointer<CwtValue>? = delegate.element?.propertyValue?.createPointer()
}

private object CwtValueConfigImpls {
    // 12 + 4 * 4 + 1 * 1 = 29 => 32b
    
    class Impl(
        override val pointer: SmartPsiElementPointer<out CwtValue>,
        override val info: CwtConfigGroupInfo,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        configs: List<CwtMemberConfig<*>>? = null,
        options: List<CwtOptionMemberConfig<*>>? = null,
        documentation: String? = null,
        propertyConfig: CwtPropertyConfig? = null,
    ) : UserDataHolderBase(), CwtValueConfig {
        init {
            putUserData(CwtValueConfig.Keys.configs, configs)
            putUserData(CwtValueConfig.Keys.options, options)
            putUserData(CwtValueConfig.Keys.documentation, documentation)
            putUserData(CwtValueConfig.Keys.propertyConfig, propertyConfig)
        }
        
        override val configs by CwtValueConfig.Keys.configs
        override val options by CwtValueConfig.Keys.options
        override val documentation by CwtValueConfig.Keys.documentation
        override val propertyConfig by CwtValueConfig.Keys.propertyConfig
        override var parentConfig by CwtValueConfig.Keys.parentConfig
        override var inlineableConfig by CwtValueConfig.Keys.inlineableConfig
        
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtValueExpression get() = valueExpression
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun toString(): String = value
    }
    
    // 12 + 2 * 4 = 20 => 24b
    
    class Delegate(
        delegate: CwtValueConfig,
        configs: List<CwtMemberConfig<*>>? = null,
        parentConfig: CwtMemberConfig<*>?,
    ) : UserDataHolderBase(), CwtValueConfig by delegate {
        init {
            putUserData(CwtValueConfig.Keys.configs, configs)
            putUserData(CwtValueConfig.Keys.parentConfig, parentConfig)
        }
        
        override val configs by CwtValueConfig.Keys.configs
        override var parentConfig by CwtValueConfig.Keys.parentConfig
        override var inlineableConfig by CwtValueConfig.Keys.inlineableConfig
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun <T : Any?> getUserData(key: Key<T>) = super.getUserData(key)
        override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
        
        override fun toString(): String = value
    }
    
    // 12 + 3 * 4 = 24 => 24b
    
    class FromPropertyConfig(
        override val pointer: SmartPsiElementPointer<out CwtValue>,
        override val propertyConfig: CwtPropertyConfig,
    ) : UserDataHolderBase(), CwtValueConfig {
        override val info: CwtConfigGroupInfo get() = propertyConfig.info
        override val value: String get() = propertyConfig.value
        override val valueTypeId: Byte get() = propertyConfig.valueTypeId
        override val documentation: String? get() = propertyConfig.documentation
        override val options: List<CwtOptionMemberConfig<*>>? get() = propertyConfig.options
        override val configs: List<CwtMemberConfig<*>>? get() = propertyConfig.configs
        
        override var parentConfig by CwtValueConfig.Keys.parentConfig
        override var inlineableConfig by CwtValueConfig.Keys.inlineableConfig
        
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtValueExpression get() = valueExpression
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun toString(): String = value
    }
}
