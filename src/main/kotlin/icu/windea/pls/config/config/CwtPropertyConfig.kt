package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*

data class CwtPropertyConfig(
    override val pointer: SmartPsiElementPointer<CwtProperty>,
    override val info: CwtConfigGroupInfo,
    override val key: String,
    override val value: String,
    override val valueType: CwtType = CwtType.String,
    override val separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    override val configs: List<CwtMemberConfig<*>>? = null,
    override val options: List<CwtOptionConfig>? = null,
    override val optionValues: List<CwtOptionValueConfig>? = null,
    override val documentation: String? = null
) : CwtMemberConfig<CwtProperty>(), CwtPropertyAware {
    companion object {
        val Empty = CwtPropertyConfig(emptyPointer(), CwtConfigGroupInfo(""), "", "")
    }
    
    val keyExpression: CwtKeyExpression = CwtKeyExpression.resolve(key)
    val valueExpression: CwtValueExpression = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
    
    override val expression: CwtKeyExpression get() = keyExpression
    
    val valueConfig by lazy {
        val valuePointer = when {
            pointer == emptyPointer<CwtValue>() -> emptyPointer()
            else -> {
                val resolvedPointer = resolved().pointer
                val resolvedFile = resolvedPointer.containingFile ?: return@lazy null
                resolvedPointer.element?.propertyValue?.createPointer(resolvedFile)
            }
        }
        if(valuePointer == null) return@lazy null
        CwtValueConfig(valuePointer, info, value, valueType, configs, options, optionValues, documentation).also { it.propertyConfig = this }
    }
    
    override fun resolved(): CwtPropertyConfig = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>() ?: this
    
    override fun resolvedOrNull(): CwtPropertyConfig? = inlineableConfig?.config?.castOrNull<CwtPropertyConfig>()
    
    override fun toString(): String {
        return "$key ${separatorType.text} $value"
    }
}
