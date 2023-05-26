package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*

sealed interface CwtPropertyConfig : CwtMemberConfig<CwtProperty>, CwtPropertyAware {
    val keyExpression: CwtKeyExpression
    val valueExpression: CwtValueExpression
    
    val valueConfig: CwtValueConfig?
    
    companion object {
        val EmptyConfig: CwtPropertyConfig = CwtPropertyConfigImpls.ImplA(emptyPointer(), CwtConfigGroupInfo(""), "", "")
        
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
                return CwtPropertyConfigImpls.ImplB(pointer, info, key, value, valueTypeId, separatorTypeId, options, documentation)
            } else {
                return CwtPropertyConfigImpls.ImplA(pointer, info, key, value, valueTypeId, separatorTypeId, configs, options, documentation)
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
    parent: CwtMemberConfig<*>? = null,
    configs: List<CwtMemberConfig<*>>? = null
): CwtPropertyConfig {
    return if(configs == null) {
        CwtPropertyConfigImpls.DelegateA(this, parent)
    } else {
        CwtPropertyConfigImpls.DelegateB(this, parent, configs)
    }
}

fun CwtPropertyConfig.getValueConfig(): CwtValueConfig? {
    val valuePointer = when {
        pointer == emptyPointer<CwtValue>() -> emptyPointer()
        else -> {
            val resolvedPointer = resolved().pointer
            val resolvedFile = resolvedPointer.containingFile ?: return null
            resolvedPointer.element?.propertyValue?.createPointer(resolvedFile)
        }
    } ?: return null
    return CwtValueConfig.resolve(valuePointer, info, value, valueType.id, configs, options, documentation, this)
}

private object CwtPropertyConfigImpls {
    sealed class Impl : UserDataHolderBase(), CwtPropertyConfig {
        override val expression: CwtKeyExpression get() = keyExpression
        
        override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
        override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
        override fun toString(): String = "$key ${separatorType.text} $value"
    }
    
    //memory usage: 12 + 15 * 4 + 2 = 74b => 80b
    
    class ImplA(
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
        @Volatile override var parent: CwtMemberConfig<*>? = null
        @Volatile override var inlineableConfig: CwtInlineableConfig<CwtProperty>? = null
        
        override val valueConfig = getValueConfig()
        override val values: List<CwtValueConfig>? by lazy { configs?.filterIsInstance<CwtValueConfig>() }
        override val properties: List<CwtPropertyConfig>? by lazy { configs?.filterIsInstance<CwtPropertyConfig>() }
        override val keyExpression: CwtKeyExpression = CwtKeyExpression.resolve(key)
        override val valueExpression: CwtValueExpression = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
    }
    
    //memory usage: 12 + 12 * 4 + 2 = 62b => 64b
    
    class ImplB(
        override val pointer: SmartPsiElementPointer<out CwtProperty>,
        override val info: CwtConfigGroupInfo,
        override val key: String,
        override val value: String,
        override val valueTypeId: Byte = CwtType.String.id,
        override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
        override val options: List<CwtOptionMemberConfig<*>>? = null,
        override val documentation: String? = null,
    ) : Impl(), CwtPropertyConfig {
        @Volatile override var parent: CwtMemberConfig<*>? = null
        @Volatile override var inlineableConfig: CwtInlineableConfig<CwtProperty>? = null
        
        override val valueConfig = getValueConfig()
        override val configs: List<CwtMemberConfig<*>>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val values: List<CwtValueConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val properties: List<CwtPropertyConfig>? get() = if(valueTypeId == CwtType.Block.id) emptyList() else null
        override val keyExpression: CwtKeyExpression = CwtKeyExpression.resolve(key)
        override val valueExpression: CwtValueExpression = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
    }
    
    //memory usage: 12 + 2 * 4 = 20b => 24b
    
    class DelegateA(
        delegate: CwtPropertyConfig,
        override var parent: CwtMemberConfig<*>?,
    ) : CwtPropertyConfig by delegate
    
    //memory usage: 12 + 4 * 4 = 28b => 32b
    
    class DelegateB(
        delegate: CwtPropertyConfig,
        override var parent: CwtMemberConfig<*>?,
        override val configs: List<CwtMemberConfig<*>>? = null,
    ) : CwtPropertyConfig by delegate {
        override val values: List<CwtValueConfig>? by lazy { configs?.filterIsInstance<CwtValueConfig>() }
        override val properties: List<CwtPropertyConfig>? by lazy { configs?.filterIsInstance<CwtPropertyConfig>() }
    }
}
