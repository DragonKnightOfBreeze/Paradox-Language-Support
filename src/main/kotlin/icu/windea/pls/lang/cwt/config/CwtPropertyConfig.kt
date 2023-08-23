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
        val EmptyConfig: CwtPropertyConfig = CwtPropertyConfigImpls.ImplB(emptyPointer(), CwtConfigGroupInfo(""), "", "")
        
        fun resolve(
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
            if(configs.isNullOrEmpty()) {
                return CwtPropertyConfigImpls.ImplA(pointer, info, key, value, valueTypeId, separatorTypeId, options, documentation)
            } else {
                return CwtPropertyConfigImpls.ImplB(pointer, info, key, value, valueTypeId, separatorTypeId, configs, options, documentation)
            }
        }
    }
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
    parent: CwtMemberConfig<*>? = this.parentConfig,
    configs: List<CwtMemberConfig<*>>? = this.configs
): CwtPropertyConfig {
    return if(configs.isNullOrEmpty()) {
        CwtPropertyConfigImpls.DelegateA(this, parent)
    } else {
        CwtPropertyConfigImpls.DelegateB(this, parent, configs)
    }
}

private object CwtPropertyConfigImpls {
    abstract class Impl : UserDataHolderBase(), CwtPropertyConfig {
        override val keyExpression: CwtKeyExpression get() = CwtKeyExpression.resolve(key)
        override val valueExpression: CwtValueExpression get() = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
        override val expression: CwtKeyExpression get() = keyExpression
        
        override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
        override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
        
        override fun toString(): String = "$key ${separatorType.text} $value"
    }
    
    class ImplA(
        override val pointer: SmartPsiElementPointer<out CwtProperty>,
        override val info: CwtConfigGroupInfo,
        override val key: String,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
        override val options: List<CwtOptionMemberConfig<*>>? = null,
        override val documentation: String? = null,
    ) : Impl(), CwtPropertyConfig {
        @Volatile override var parentConfig: CwtMemberConfig<*>? = null
        @Volatile override var inlineableConfig: CwtInlineableConfig<CwtProperty>? = null
        
        override val valueConfig: CwtValueConfig? = doGetValueConfig()
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    }
    
    class ImplB(
        override val pointer: SmartPsiElementPointer<out CwtProperty>,
        override val info: CwtConfigGroupInfo,
        override val key: String,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
        override val configs: List<CwtMemberConfig<*>>? = null,
        override val options: List<CwtOptionMemberConfig<*>>? = null,
        override val documentation: String? = null,
    ) : Impl(), CwtPropertyConfig {
        @Volatile override var parentConfig: CwtMemberConfig<*>? = null
        @Volatile override var inlineableConfig: CwtInlineableConfig<CwtProperty>? = null
        
        override val valueConfig: CwtValueConfig? = doGetValueConfig()
    }
    
    class DelegateA(
        delegate: CwtPropertyConfig,
        override var parentConfig: CwtMemberConfig<*>?,
    ) : CwtPropertyConfig by delegate {
        override val valueConfig: CwtValueConfig? = doGetValueConfig()
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
    }
    
    class DelegateB(
        delegate: CwtPropertyConfig,
        override var parentConfig: CwtMemberConfig<*>?,
        override val configs: List<CwtMemberConfig<*>>? = null,
    ) : CwtPropertyConfig by delegate {
        override val valueConfig: CwtValueConfig? = doGetValueConfig()
    }
}

private fun CwtPropertyConfig.doGetValueConfig(): CwtValueConfig? {
    val valuePointer = when {
        pointer.isEmpty() -> emptyPointer()
        else -> {
            val resolvedPointer = resolved().pointer
            val resolvedFile = resolvedPointer.containingFile ?: return null
            resolvedPointer.element?.propertyValue?.createPointer(resolvedFile)
        }
    } ?: return null
    return CwtValueConfig.resolve(valuePointer, info, value, valueType.id, configs, options, documentation, this)
}
