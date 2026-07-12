package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.CwtExpandableConfig
import icu.windea.pls.lang.index.ParadoxMergedIndex

/**
 * 可展开的规则（并集规则、别名规则、单别名规则）的综合属性。
 *
 * 用于进行更准确的语义解析与匹配，以及优化构建合并索引（[ParadoxMergedIndex]）时的性能。
 *
 * @see CwtExpandableConfig
 * @see CwtExpandableConfigAttributesEvaluator
 */
data class CwtExpandableConfigAttributes(
    override val involveDynamicValue: Boolean = false,
    override val involveParameter: Boolean = false,
    override val involveLocalisationParameter: Boolean = false,
    override val involveInferredScopeContextAwareDefinitionReference: Boolean = false,
    override val involveExternalReference: Boolean = false,
) : CwtDeclarationLikeConfigAttributes {
    companion object {
        @JvmField val EMPTY = CwtExpandableConfigAttributes()
    }
}

