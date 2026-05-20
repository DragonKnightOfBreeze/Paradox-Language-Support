package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.lang.index.ParadoxMergedIndex

/**
 * 要内联的规则（单别名规则、别名规则）的综合属性。
 *
 * 用于进行更准确的语义解析与匹配，以及优化构建合并索引（[ParadoxMergedIndex]）时的性能。
 *
 * @see CwtSingleAliasConfig
 * @see CwtAliasConfig
 * @see CwtInlinedConfigAttributesEvaluator
 */
data class CwtInlinedConfigAttributes(
    override val involvesDynamicValue: Boolean = false,
    override val involvesParameter: Boolean = false,
    override val involvesLocalisationParameter: Boolean = false,
    override val involvesInferredScopeContextAwareDefinitionReference: Boolean = false,
    override val involvesExternalReference: Boolean = false,
) : CwtDeclarationLikeConfigAttributes {
    companion object {
        @JvmField val EMPTY = CwtInlinedConfigAttributes()
    }
}

