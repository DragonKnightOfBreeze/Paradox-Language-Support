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
        val EmptyConfig: CwtValueConfig by lazy { resolve(emptyPointer(), CwtConfigGroupInfo(""), "") }
    }
}

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
    return CwtValueConfig.resolve(pointer, info, value, valueTypeId, configs, options, documentation, propertyConfig)
}

fun CwtValueConfig.copyDelegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig,
): CwtValueConfig {
    return CwtValueConfigImpls.Delegate(this, configs).apply { this.parentConfig = parentConfig }
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
        override val configs = configs
        override val options = options
        override val documentation = documentation
        
        override val propertyConfig = propertyConfig
        
        override var parentConfig: CwtMemberConfig<*>? = null
        override var inlineableConfig: CwtInlineableConfig<CwtValue>? = null
        
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtValueExpression get() = valueExpression
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun toString(): String = value
    }
    
    class Delegate(
        private val delegate: CwtValueConfig,
        configs: List<CwtMemberConfig<*>>? = null,
    ) : UserDataHolderBase(), CwtValueConfig by delegate {
        override val configs = configs
        
        override var parentConfig: CwtMemberConfig<*>? = null
        override var inlineableConfig: CwtInlineableConfig<CwtValue>? = null
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun <T : Any?> getUserData(key: Key<T>) = super.getUserData(key) ?: delegate.getUserData(key)
        override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
        
        override fun toString(): String = value
    }
    
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
        
        override var parentConfig: CwtMemberConfig<*>? = null
        override var inlineableConfig: CwtInlineableConfig<CwtValue>? = null
        
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtValueExpression get() = valueExpression
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun <T : Any?> getUserData(key: Key<T>) = super.getUserData(key) ?: propertyConfig.getUserData(key)
        override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
        
        override fun toString(): String = value
    }
}
