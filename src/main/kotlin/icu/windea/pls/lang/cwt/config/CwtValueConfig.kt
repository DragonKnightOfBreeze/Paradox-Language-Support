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

private object CwtValueConfigImpls {
    abstract class Impl : UserDataHolderBase(), CwtValueConfig {
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtValueExpression get() = valueExpression
        
        override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
        override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
        
        override fun toString(): String = value
    }
    
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
        
        @Lazy override val values: List<CwtValueConfig>? = configs?.filterIsInstanceFast<CwtValueConfig>()
        @Lazy override val properties: List<CwtPropertyConfig>? = configs?.filterIsInstanceFast<CwtPropertyConfig>()
    }
    
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
        
        @Lazy override val values: List<CwtValueConfig>? = configs?.filterIsInstanceFast<CwtValueConfig>()
        @Lazy override val properties: List<CwtPropertyConfig>? = configs?.filterIsInstanceFast<CwtPropertyConfig>()
    }
    
    class DelegateA(
        delegate: CwtValueConfig,
        override var parent: CwtMemberConfig<*>?,
        override val propertyConfig: CwtPropertyConfig? = null,
    ) : CwtValueConfig by delegate {
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val values: List<CwtValueConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val properties: List<CwtPropertyConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    }
    
    class DelegateB(
        delegate: CwtValueConfig,
        override var parent: CwtMemberConfig<*>?,
        override val configs: List<CwtMemberConfig<*>>? = null,
        override val propertyConfig: CwtPropertyConfig? = null,
    ) : CwtValueConfig by delegate {
        @Lazy override val values: List<CwtValueConfig>? = configs?.filterIsInstanceFast<CwtValueConfig>()
        @Lazy override val properties: List<CwtPropertyConfig>? = configs?.filterIsInstanceFast<CwtPropertyConfig>()
    }
}
