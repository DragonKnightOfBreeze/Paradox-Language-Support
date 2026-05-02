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
    override val dynamicValueInvolved: Boolean = false,
    override val parameterInvolved: Boolean = false,
    override val localisationParameterInvolved: Boolean = false,
    override val inferredScopeContextAwareDefinitionReferenceInvolved: Boolean = false,
) : CwtDeclarationLikeConfigAttributes {
    companion object {
        @JvmStatic
        val EMPTY = CwtDeclarationConfigAttributes()
    }
}
