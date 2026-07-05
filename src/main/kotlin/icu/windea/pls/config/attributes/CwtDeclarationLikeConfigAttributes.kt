package icu.windea.pls.config.attributes

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configGroup.CwtTypesModel

/**
 * @property involveDynamicValue 规则树中是否涉及可能匹配动态值的数据表达式。参见 [CwtDataTypeSets.DynamicValueInvolved]。
 * @property involveParameter 规则树中是否涉及可能匹配参数的数据表达式。参见 [CwtDataTypeSets.ParameterInvolved]
 * @property involveLocalisationParameter 规则树中是否涉及可能匹配本地化参数的数据表达式。参见 [CwtDataTypeSets.LocalisationParameterInvolved]。
 * @property involveInferredScopeContextAwareDefinitionReference 规则树中是否涉及可能匹配可推断作用域上下文的定义引用的数据表达式。参见 [CwtTypesModel.supportScopeContextInference]。
 * @property involveExternalReference 规则树中是否涉及可能匹配（目前作为动态引用处理的）外部引用的数据表达式。参见 [CwtDataTypeSets.ExternalReferenceInvolved]。
 */
interface CwtDeclarationLikeConfigAttributes : CwtConfigAttributes {
    val involveDynamicValue: Boolean
    val involveParameter: Boolean
    val involveLocalisationParameter: Boolean
    val involveInferredScopeContextAwareDefinitionReference: Boolean
    val involveExternalReference: Boolean
}
