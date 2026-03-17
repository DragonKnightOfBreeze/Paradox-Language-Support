package icu.windea.pls.config.configExpression

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

object CwtConfigExpressionMatcher {
    fun isDynamicValue(dataExpression: CwtDataExpression): Boolean {
        return dataExpression.type in CwtDataTypeSets.DynamicValueInvolved
    }

    fun isParameter(dataExpression: CwtDataExpression): Boolean {
        return dataExpression.type in CwtDataTypeSets.ParameterInvolved
    }

    fun isLocalisationParameter(dataExpression: CwtDataExpression): Boolean {
        return dataExpression.type in CwtDataTypeSets.LocalisationParameterInvolved
    }

    fun isInferredScopeContextAwareDefinitionReference(dataExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        if (dataExpression.type != CwtDataTypes.Definition) return false
        val definitionType = dataExpression.value?.substringBefore('.') ?: return false
        return definitionType in configGroup.definitionTypesModel.supportScopeContextInference
    }

    fun isOnActionReference(dataExpression: CwtDataExpression): Boolean {
        if (dataExpression.type != CwtDataTypes.Definition) return false
        val definitionType = dataExpression.value?.substringBefore('.') ?: return false
        return definitionType == ParadoxDefinitionTypes.onAction
    }

    fun isEventReference(dataExpression: CwtDataExpression): Boolean {
        if (dataExpression.type != CwtDataTypes.Definition) return false
        val definitionType = dataExpression.value?.substringBefore('.') ?: return false
        return definitionType == ParadoxDefinitionTypes.event
    }
}
