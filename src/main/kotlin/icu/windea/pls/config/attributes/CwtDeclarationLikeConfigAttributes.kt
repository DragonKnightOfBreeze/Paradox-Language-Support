package icu.windea.pls.config.attributes

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtDefinitionTypesModel

/**
 * @property dynamicValueInvolved 规则树中是否涉及到可能匹配动态值的数据表达式（[CwtDataExpression]）。参见 [CwtDataTypeSets.DynamicValueInvolved]。
 * @property parameterInvolved 规则树中是否涉及到可能匹配动态值的数据表达式（[CwtDataExpression]）。参见 [CwtDataTypeSets.ParameterInvolved]
 * @property localisationParameterInvolved 规则树中是否涉及到可能匹配动态值的数据表达式（[CwtDataExpression]）。参见 [CwtDataTypeSets.LocalisationParameterInvolved]。
 * @property inferredScopeContextAwareDefinitionReferenceInvolved 规则树中是否涉及到可能匹配可推断作用域上下文的定义引用的数据表达式（[CwtDataExpression]）。参见 [CwtDefinitionTypesModel.supportScopeContextInference]。
 */
interface CwtDeclarationLikeConfigAttributes: CwtConfigAttributes {
    val dynamicValueInvolved: Boolean
    val parameterInvolved: Boolean
    val localisationParameterInvolved: Boolean
    val inferredScopeContextAwareDefinitionReferenceInvolved: Boolean
}
