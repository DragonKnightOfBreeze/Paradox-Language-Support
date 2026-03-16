package icu.windea.pls.config.attributes

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression

/**
 * 声明规则的综合属性。
 *
 * 用于优化索引时的性能，以及进行更准确的语义解析与匹配。
 *
 * @property involvedSubtypes 规则树中涉及到的子类型表达式（[ParadoxDefinitionSubtypeExpression]）中涉及到的子类型的集合。
 * @property dynamicValueInvolved 规则树中是否涉及到可能匹配动态值的数据表达式（[CwtDataExpression]）。参见 [CwtDataTypeSets.DynamicValueInvolved]。
 * @property parameterInvolved 规则树中是否涉及到可能匹配动态值的数据表达式（[CwtDataExpression]）。参见 [CwtDataTypeSets.ParameterInvolved]
 * @property localisationParameterInvolved 规则树中是否涉及到可能匹配动态值的数据表达式（[CwtDataExpression]）。参见 [CwtDataTypeSets.LocalisationParameterInvolved]。
 *
 * @see CwtDelegatedConfig
 * @see CwtDeclarationConfigAttributesEvaluator
 */
data class CwtDeclarationConfigAttributes(
    val involvedSubtypes: Set<String> = emptySet(),
    val dynamicValueInvolved: Boolean = false,
    val parameterInvolved: Boolean = false,
    val localisationParameterInvolved: Boolean = false,
) {
    companion object {
        @JvmStatic
        val EMPTY = CwtDeclarationConfigAttributes()
    }
}
