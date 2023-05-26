package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*

data class CwtValueConfig(
    override val pointer: SmartPsiElementPointer<CwtValue>,
    override val info: CwtConfigGroupInfo,
    override val value: String,
    override val valueType: CwtType = CwtType.String,
    override val configs: List<CwtMemberConfig<*>>? = null,
    override val options: List<CwtOptionConfig>? = null,
    override val optionValues: List<CwtOptionValueConfig>? = null,
    override val documentation: String? = null,
) : CwtMemberConfig<CwtValue>(), CwtValueAware{
    companion object {
        val Empty = CwtValueConfig(emptyPointer(), CwtConfigGroupInfo(""), "")
    }
    
    @Volatile var propertyConfig: CwtPropertyConfig? = null
    
    val valueExpression: CwtValueExpression = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
    
    override val expression: CwtValueExpression get() = valueExpression
    
    override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
    
    override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
    
    override fun toString(): String {
        return value
    }
}

val CwtValueConfig.isTagConfig get() = findOptionValue("tag") != null
