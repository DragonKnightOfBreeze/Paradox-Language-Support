package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*

class CwtPropertyConfig private constructor(
    override val pointer: SmartPsiElementPointer<CwtProperty>,
    override val info: CwtConfigGroupInfo,
    override val key: String,
    override val value: String,
    override val valueTypeId: Byte = CwtType.String.id,
    override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
    override val configs: List<CwtMemberConfig<*>>? = null,
    override val options: List<CwtOptionConfig>? = null,
    override val optionValues: List<CwtOptionValueConfig>? = null,
    override val documentation: String? = null
) : CwtMemberConfig<CwtProperty>(), CwtPropertyAware {
    val keyExpression: CwtKeyExpression = CwtKeyExpression.resolve(key)
    val valueExpression: CwtValueExpression = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
    
    override val expression: CwtKeyExpression get() = keyExpression
    
    val valueConfig = run {
        val valuePointer = when {
            pointer == emptyPointer<CwtValue>() -> emptyPointer()
            else -> {
                val resolvedPointer = resolved().pointer
                val resolvedFile = resolvedPointer.containingFile ?: return@run null
                resolvedPointer.element?.propertyValue?.createPointer(resolvedFile)
            }
        }
        if(valuePointer == null) return@run null
        CwtValueConfig.resolve(valuePointer, info, value, valueType.id, configs, options, optionValues, documentation).also { it.propertyConfig = this }
    }
    
    override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
    
    override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
    
    override fun toString(): String {
        return "$key ${separatorType.text} $value"
    }
    
    companion object {
        val EmptyConfig = CwtPropertyConfig(emptyPointer(), CwtConfigGroupInfo(""), "", "")
        
        fun resolve(
             pointer: SmartPsiElementPointer<CwtProperty>,
             info: CwtConfigGroupInfo,
             key: String,
             value: String,
             valueTypeId: Byte = CwtType.String.id,
             separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
             configs: List<CwtMemberConfig<*>>? = null,
             options: List<CwtOptionConfig>? = null,
             optionValues: List<CwtOptionValueConfig>? = null,
             documentation: String? = null
        ): CwtPropertyConfig {
            return CwtPropertyConfig(pointer, info, key, value, valueTypeId, separatorTypeId, configs, options, optionValues, documentation)
        }
    }
}
