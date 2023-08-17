package icu.windea.pls.lang.cwt.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*

sealed interface CwtValueConfig : CwtMemberConfig<CwtValue>, CwtValueAware {
    val propertyConfig: CwtPropertyConfig?
    
    companion object {
        val EmptyConfig: CwtValueConfig = CwtValueConfigImpls.ImplA(emptyPointer(), CwtConfigGroupInfo(""), "")
        
        fun resolve(
            pointer: SmartPsiElementPointer<out CwtValue>,
            info: CwtConfigGroupInfo,
            value: String,
            valueTypeId: @EnumId(CwtType::class) Byte = CwtType.String.id,
            configs: List<CwtMemberConfig<*>>? = null,
            options: List<CwtOptionMemberConfig<*>>? = null,
            documentation: String? = null,
            propertyConfig: CwtPropertyConfig? = null
        ): CwtValueConfig {
            return if(propertyConfig == null) {
                if(configs.isNullOrEmpty()) {
                    CwtValueConfigImpls.ImplA(pointer, info, value, valueTypeId, options, documentation)
                } else {
                    CwtValueConfigImpls.ImplB(pointer, info, value, valueTypeId, configs, options, documentation)
                }
            } else {
                if(configs.isNullOrEmpty()) {
                    CwtValueConfigImpls.ImplC(pointer, info, value, valueTypeId, options, documentation, propertyConfig)
                } else {
                    CwtValueConfigImpls.ImplD(pointer, info, value, valueTypeId, configs, options, documentation, propertyConfig)
                }
            }
        }
    }
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
    parent: CwtMemberConfig<*>? = this.parent,
    configs: List<CwtMemberConfig<*>>? = this.configs,
    propertyConfig: CwtPropertyConfig? = this.propertyConfig,
): CwtValueConfig {
    return if(configs.isNullOrEmpty()) {
        CwtValueConfigImpls.DelegateA(this, parent, propertyConfig)
    } else {
        CwtValueConfigImpls.DelegateB(this, parent, configs, propertyConfig)
    }
}

val CwtValueConfig.isTagConfig get() = findOptionValue("tag") != null

private object CwtValueConfigImpls {
    sealed class Impl : UserDataHolderBase(), CwtValueConfig {
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtValueExpression get() = valueExpression
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        override fun toString(): String = value
    }
    
    //memory usage: 12 + 9 * 4 + 1 = 49b => 56b
    
    class ImplA(
        override val pointer: SmartPsiElementPointer<out CwtValue>,
        override val info: CwtConfigGroupInfo,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        override val options: List<CwtOptionMemberConfig<*>>? = null,
        override val documentation: String? = null,
    ) : Impl(), CwtValueConfig {
        @Volatile override var parent: CwtMemberConfig<*>? = null
        @Volatile override var inlineableConfig: CwtInlineableConfig<CwtValue>? = null
        
        override val propertyConfig: CwtPropertyConfig? get() = null
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val values: List<CwtValueConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val properties: List<CwtPropertyConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    }
    
    //memory usage: 12 + 12 * 4 + 1 = 61b => 64b
    
    class ImplB(
        override val pointer: SmartPsiElementPointer<out CwtValue>,
        override val info: CwtConfigGroupInfo,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        override val configs: List<CwtMemberConfig<*>>? = null,
        override val options: List<CwtOptionMemberConfig<*>>? = null,
        override val documentation: String? = null,
    ) : Impl(), CwtValueConfig {
        @Volatile override var parent: CwtMemberConfig<*>? = null
        @Volatile override var inlineableConfig: CwtInlineableConfig<CwtValue>? = null
        
        override val propertyConfig: CwtPropertyConfig? get() = null
        override val values: List<CwtValueConfig>? by lazy { configs?.filterIsInstanceFast<CwtValueConfig>() }
        override val properties: List<CwtPropertyConfig>? by lazy { configs?.filterIsInstanceFast<CwtPropertyConfig>() }
    }
    
    //memory usage: 12 + 10 * 4 + 1 = 53b => 56b
    
    class ImplC(
        override val pointer: SmartPsiElementPointer<out CwtValue>,
        override val info: CwtConfigGroupInfo,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        override val options: List<CwtOptionMemberConfig<*>>? = null,
        override val documentation: String? = null,
        override val propertyConfig: CwtPropertyConfig? = null,
    ) : Impl(), CwtValueConfig {
        @Volatile override var parent: CwtMemberConfig<*>? = null
        @Volatile override var inlineableConfig: CwtInlineableConfig<CwtValue>? = null
        
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val values: List<CwtValueConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val properties: List<CwtPropertyConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    }
    
    //memory usage: 12 + 13 * 4 + 1 = 65b => 72b
    
    class ImplD(
        override val pointer: SmartPsiElementPointer<out CwtValue>,
        override val info: CwtConfigGroupInfo,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        override val configs: List<CwtMemberConfig<*>>? = null,
        override val options: List<CwtOptionMemberConfig<*>>? = null,
        override val documentation: String? = null,
        override val propertyConfig: CwtPropertyConfig? = null,
    ) : Impl(), CwtValueConfig {
        @Volatile override var parent: CwtMemberConfig<*>? = null
        @Volatile override var inlineableConfig: CwtInlineableConfig<CwtValue>? = null
        
        override val values: List<CwtValueConfig>? by lazy { configs?.filterIsInstanceFast<CwtValueConfig>() }
        override val properties: List<CwtPropertyConfig>? by lazy { configs?.filterIsInstanceFast<CwtPropertyConfig>() }
    }
    
    //memory usage: 12 + 4 * 3 = 24b => 24b
    
    class DelegateA(
        delegate: CwtValueConfig,
        override var parent: CwtMemberConfig<*>?,
        override val propertyConfig: CwtPropertyConfig? = null,
    ) : CwtValueConfig by delegate {
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val values: List<CwtValueConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val properties: List<CwtPropertyConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        
        override fun toString(): String = value
    }
    
    //memory usage: 12 + 6 * 4 = 36b => 40b
    
    class DelegateB(
        delegate: CwtValueConfig,
        override var parent: CwtMemberConfig<*>?,
        override val configs: List<CwtMemberConfig<*>>? = null,
        override val propertyConfig: CwtPropertyConfig? = null,
    ) : CwtValueConfig by delegate {
        override val values: List<CwtValueConfig>? by lazy { configs?.filterIsInstanceFast<CwtValueConfig>() }
        override val properties: List<CwtPropertyConfig>? by lazy { configs?.filterIsInstanceFast<CwtPropertyConfig>() }
        
        override fun toString(): String = value
    }
}
