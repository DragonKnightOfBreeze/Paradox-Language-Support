package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*

interface CwtValueConfig : CwtMemberConfig<CwtValue>, CwtValueAware {
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
    return if(configs != null) {
        if(options != null || documentation != null) {
            CwtValueConfigImpls.Impl1(pointer, info, value, valueTypeId, configs, options, documentation, propertyConfig)
        } else {
            CwtValueConfigImpls.Impl2(pointer, info, value, valueTypeId, configs, propertyConfig)
        }
    } else {
        if(options != null || documentation != null) {
            CwtValueConfigImpls.Impl3(pointer, info, value, valueTypeId, options, documentation, propertyConfig)
        } else {
            CwtValueConfigImpls.Impl4(pointer, info, value, valueTypeId, propertyConfig)
        }
    }
    
}

fun CwtValueConfig.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig,
): CwtValueConfig {
    return if(configs != null) {
        CwtValueConfigImpls.Delegate1(this, configs).apply { this.parentConfig = parentConfig }
    } else {
        CwtValueConfigImpls.Delegate2(this).apply { this.parentConfig = parentConfig }
    }
}

fun CwtValueConfig.delegatedWith(value: String): CwtValueConfig {
    return CwtValueConfigImpls.DelegateWith(this, value)
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

//Implementations

private object CwtValueConfigImpls {
    abstract class Impl(
        override val pointer: SmartPsiElementPointer<out CwtValue>,
        override val info: CwtConfigGroupInfo,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        propertyConfig: CwtPropertyConfig? = null,
    ) : UserDataHolderBase(), CwtValueConfig {
        override val propertyConfig = propertyConfig
        
        override var parentConfig: CwtMemberConfig<*>? = null
        override var inlineableConfig: CwtInlineableConfig<CwtValue, CwtMemberConfig<CwtValue>>? = null
        
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtValueExpression get() = valueExpression
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun toString(): String = value
    }
    
    //12 + 10 * 4 + 2 * 1 = 54 => 56
    class Impl1(
        pointer: SmartPsiElementPointer<out CwtValue>,
        info: CwtConfigGroupInfo,
        value: String,
        valueTypeId: Byte = CwtType.String.id,
        configs: List<CwtMemberConfig<*>>? = null,
        options: List<CwtOptionMemberConfig<*>>? = null,
        documentation: String? = null,
        propertyConfig: CwtPropertyConfig? = null,
    ) : Impl(pointer, info, value, valueTypeId, propertyConfig) {
        override val configs = configs
        override val options = options
        override val documentation = documentation
    }
    
    //12 + 8 * 4 + 2 * 1 = 46 => 48
    class Impl2(
        pointer: SmartPsiElementPointer<out CwtValue>,
        info: CwtConfigGroupInfo,
        value: String,
        valueTypeId: Byte = CwtType.String.id,
        configs: List<CwtMemberConfig<*>>? = null,
        propertyConfig: CwtPropertyConfig? = null,
    ) : Impl(pointer, info, value, valueTypeId, propertyConfig) {
        override val configs = configs
        override val options get() = null
        override val documentation get() = null
    }
    
    //12 + 9 * 4 + 2 * 1 = 50 => 56
    class Impl3(
        pointer: SmartPsiElementPointer<out CwtValue>,
        info: CwtConfigGroupInfo,
        value: String,
        valueTypeId: Byte = CwtType.String.id,
        options: List<CwtOptionMemberConfig<*>>? = null,
        documentation: String? = null,
        propertyConfig: CwtPropertyConfig? = null,
    ) : Impl(pointer, info, value, valueTypeId, propertyConfig) {
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val options = options
        override val documentation = documentation
    }
    
    //12 + 7 * 4 + 2 * 1 = 42 => 48
    class Impl4(
        pointer: SmartPsiElementPointer<out CwtValue>,
        info: CwtConfigGroupInfo,
        value: String,
        valueTypeId: Byte = CwtType.String.id,
        propertyConfig: CwtPropertyConfig? = null,
    ) : Impl(pointer, info, value, valueTypeId, propertyConfig) {
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val options get() = null
        override val documentation get() = null
    }
    
    abstract class Delegate(
        private val delegate: CwtValueConfig,
    ) : UserDataHolderBase(), CwtValueConfig by delegate {
        override var parentConfig: CwtMemberConfig<*>? = null
        override var inlineableConfig: CwtInlineableConfig<CwtValue, CwtMemberConfig<CwtValue>>? = null
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
        override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
        
        override fun toString(): String = value
    }
    
    //12 + 5 * 4 = 32 => 32
    class Delegate1(
        delegate: CwtValueConfig,
        configs: List<CwtMemberConfig<*>>? = null,
    ) : Delegate(delegate) {
        override val configs = configs
    }
    
    //12 + 4 * 4 = 28 => 32
    class Delegate2(
        delegate: CwtValueConfig,
    ) : Delegate(delegate) {
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    }
    
    //12 + 5 * 4 = 28 -> 32
    class DelegateWith(
        delegate: CwtValueConfig,
        override val value: String,
        //configs should be always null here
    ) : Delegate(delegate) {
        override val valueExpression: CwtValueExpression get() = CwtValueExpression.resolve(value)
        override val expression: CwtDataExpression get() = valueExpression
        
        override fun toString(): String = value
    }
    
    //12 + 5 * 4 = 32 => 32 
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
        override var inlineableConfig: CwtInlineableConfig<CwtValue, CwtMemberConfig<CwtValue>>? = null
        
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtValueExpression get() = valueExpression
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun <T : Any?> getUserData(key: Key<T>) = super.getUserData(key) ?: propertyConfig.getUserData(key)
        override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
        
        override fun toString(): String = value
    }
}
