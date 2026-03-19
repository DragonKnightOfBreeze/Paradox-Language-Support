package icu.windea.pls.config.match

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

object CwtConfigExpressionMatchService {
    fun matchesDynamicValue(dataExpression: CwtDataExpression): Boolean {
        return dataExpression.type in CwtDataTypeSets.DynamicValueInvolved
    }

    fun matchesParameter(dataExpression: CwtDataExpression): Boolean {
        return dataExpression.type in CwtDataTypeSets.ParameterInvolved
    }

    fun matchesLocalisationParameter(dataExpression: CwtDataExpression): Boolean {
        return dataExpression.type in CwtDataTypeSets.LocalisationParameterInvolved
    }

    fun matchesInferredScopeContextAwareDefinitionReference(dataExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        if (dataExpression.type != CwtDataTypes.Definition) return false
        val definitionType = dataExpression.value?.substringBefore('.') ?: return false
        return definitionType in configGroup.definitionTypesModel.supportScopeContextInference
    }

    fun matchesOnActionReference(dataExpression: CwtDataExpression): Boolean {
        if (dataExpression.type != CwtDataTypes.Definition) return false
        val definitionType = dataExpression.value?.substringBefore('.') ?: return false
        return definitionType == ParadoxDefinitionTypes.onAction
    }

    fun matchesEventReference(dataExpression: CwtDataExpression): Boolean {
        if (dataExpression.type != CwtDataTypes.Definition) return false
        val definitionType = dataExpression.value?.substringBefore('.') ?: return false
        return definitionType == ParadoxDefinitionTypes.event
    }
}
