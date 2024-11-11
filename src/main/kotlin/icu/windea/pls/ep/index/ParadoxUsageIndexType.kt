package icu.windea.pls.ep.index

import icu.windea.pls.model.usageInfo.*

sealed class ParadoxUsageIndexType<T : ParadoxUsageInfo>(val id: Byte) {
    data object ComplexEnumValue : ParadoxUsageIndexType<ParadoxComplexEnumValueUsageInfo>(1)
    data object DynamicValue : ParadoxUsageIndexType<ParadoxDynamicValueUsageInfo>(2)
    data object Parameter : ParadoxUsageIndexType<ParadoxParameterUsageInfo>(3)
    data object LocalisationParameter : ParadoxUsageIndexType<ParadoxLocalisationParameterUsageInfo>(4)

    data object InferredScopeContextAwareDefinition : ParadoxUsageIndexType<ParadoxInferredScopeContextAwareDefinitionUsageInfo>(5)
    data object EventInOnAction : ParadoxUsageIndexType<ParadoxEventInOnActionUsageInfo>(6)
    data object EventInEvent : ParadoxUsageIndexType<ParadoxEventInEventUsageInfo>(7)
    data object OnActionInEvent : ParadoxUsageIndexType<ParadoxOnActionInEventUsageInfo>(8)
}
