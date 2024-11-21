package icu.windea.pls.ep.index

import icu.windea.pls.model.indexInfo.*

sealed class ParadoxInfoIndexType<T : ParadoxIndexInfo>(val id: Byte) {
    data object ComplexEnumValueUsage : ParadoxInfoIndexType<ParadoxComplexEnumValueUsageInfo>(1)
    data object DynamicValueUsage : ParadoxInfoIndexType<ParadoxDynamicValueUsageInfo>(2)
    data object ParameterUsage : ParadoxInfoIndexType<ParadoxParameterUsageInfo>(3)
    data object LocalisationParameterUsage : ParadoxInfoIndexType<ParadoxLocalisationParameterUsageInfo>(4)

    data object InferredScopeContextAwareDefinitionUsage : ParadoxInfoIndexType<ParadoxInferredScopeContextAwareDefinitionUsageInfo>(5)
    data object EventInOnActionUsage : ParadoxInfoIndexType<ParadoxEventInOnActionUsageInfo>(6)
    data object EventInEventUsage : ParadoxInfoIndexType<ParadoxEventInEventUsageInfo>(7)
    data object OnActionInEventUsage : ParadoxInfoIndexType<ParadoxOnActionInEventUsageInfo>(8)
}
