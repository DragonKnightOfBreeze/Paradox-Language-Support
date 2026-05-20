package icu.windea.pls.config.attributes

import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.model.expressions.ParadoxDefinitionSubtypeExpression

/**
 * 声明规则的综合属性。
 *
 * 用于进行更准确的语义解析与匹配，以及优化构建合并索引（[ParadoxMergedIndex]）时的性能。
 *
 * @property involvedSubtypes 规则树中涉及到的子类型表达式（[ParadoxDefinitionSubtypeExpression]）中涉及到的子类型的集合。
 *
 * @see CwtDeclarationConfig
 * @see CwtDeclarationConfigAttributesEvaluator
 */
data class CwtDeclarationConfigAttributes(
    val involvedSubtypes: Set<String> = emptySet(),
    override val involvesDynamicValue: Boolean = false,
    override val involvesParameter: Boolean = false,
    override val involvesLocalisationParameter: Boolean = false,
    override val involvesInferredScopeContextAwareDefinitionReference: Boolean = false,
    override val involvesExternalReference: Boolean = false,
) : CwtDeclarationLikeConfigAttributes {
    companion object {
        @JvmField val EMPTY = CwtDeclarationConfigAttributes()
    }
}
