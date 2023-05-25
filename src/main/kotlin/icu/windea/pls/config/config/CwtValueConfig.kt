package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

data class CwtValueConfig(
    override val pointer: SmartPsiElementPointer<CwtValue>,
    override val info: CwtConfigGroupInfo,
    override val value: String,
    override val booleanValue: Boolean? = null,
    override val intValue: Int? = null,
    override val floatValue: Float? = null,
    override val stringValue: String? = null,
    override val configs: List<CwtDataConfig<*>>? = null,
    override val documentation: String? = null,
    override val options: List<CwtOptionConfig>? = null,
    override val optionValues: List<CwtOptionValueConfig>? = null,
    val propertyConfig: CwtPropertyConfig? = null,
) : CwtDataConfig<CwtValue>() {
    companion object {
        val Empty = CwtValueConfig(emptyPointer(), CwtConfigGroupInfo(""), "")
    }
    
    val valueExpression: CwtValueExpression = if(isBlock) CwtValueExpression.BlockExpression else CwtValueExpression.resolve(value)
    
    override val expression: CwtValueExpression get() = valueExpression
    
    override fun resolved(): CwtValueConfig = inlineableConfig?.config?.castOrNull<CwtValueConfig>() ?: this
    
    override fun resolvedOrNull(): CwtValueConfig? = inlineableConfig?.config?.castOrNull<CwtValueConfig>()
    
    override fun toString(): String = value
}

val CwtValueConfig.isTagConfig: Boolean get() = optionValues?.any { it.stringValue == "tag" } == true
