package icu.windea.pls.config.attributes

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

object CwtConfigAttributesUtil {
    fun dynamicValueInvolved(dataExpression: CwtDataExpression): Boolean {
        return dataExpression.type in CwtDataTypeSets.DynamicValueInvolved
    }

    fun parameterInvolved(dataExpression: CwtDataExpression): Boolean {
        return dataExpression.type in CwtDataTypeSets.ParameterInvolved
    }

    fun localisationParameterInvolved(dataExpression: CwtDataExpression): Boolean {
        return dataExpression.type in CwtDataTypeSets.LocalisationParameterInvolved
    }

    fun inferredScopeContextAwareDefinitionReferenceInvolved(dataExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        if (dataExpression.type != CwtDataTypes.Definition) return false
        val type = dataExpression.value?.substringBefore('.') ?: return false
        return type in configGroup.definitionTypesModel.supportScopeContextInference
    }
}
