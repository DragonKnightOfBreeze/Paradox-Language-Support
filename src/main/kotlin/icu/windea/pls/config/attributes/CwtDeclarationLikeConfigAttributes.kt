package icu.windea.pls.config.attributes

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configExpression.CwtDataExpression

/**
 * @property dynamicValueInvolved 规则树中是否涉及到可能匹配动态值的数据表达式（[CwtDataExpression]）。参见 [CwtDataTypeSets.DynamicValueInvolved]。
 * @property parameterInvolved 规则树中是否涉及到可能匹配动态值的数据表达式（[CwtDataExpression]）。参见 [CwtDataTypeSets.ParameterInvolved]
 * @property localisationParameterInvolved 规则树中是否涉及到可能匹配动态值的数据表达式（[CwtDataExpression]）。参见 [CwtDataTypeSets.LocalisationParameterInvolved]。
 */
interface CwtDeclarationLikeConfigAttributes {
    val dynamicValueInvolved: Boolean
    val parameterInvolved: Boolean
    val localisationParameterInvolved: Boolean
}
