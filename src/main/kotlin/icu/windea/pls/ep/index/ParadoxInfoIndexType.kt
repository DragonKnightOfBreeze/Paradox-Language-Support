package icu.windea.pls.ep.index

import icu.windea.pls.model.indexInfo.*

sealed class ParadoxInfoIndexType<T : ParadoxIndexInfo>(val id: Byte) {
    data object ComplexEnumValueUsage : ParadoxInfoIndexType<ParadoxComplexEnumValueIndexInfo>(1)
    data object DynamicValueUsage : ParadoxInfoIndexType<ParadoxDynamicValueIndexInfo>(2)
    data object ParameterUsage : ParadoxInfoIndexType<ParadoxParameterIndexInfo>(3)
    data object LocalisationParameterUsage : ParadoxInfoIndexType<ParadoxLocalisationParameterIndexInfo>(4)

    data object InferredScopeContextAwareDefinitionUsage : ParadoxInfoIndexType<ParadoxInferredScopeContextAwareDefinitionIndexInfo>(5)
    data object EventInOnActionUsage : ParadoxInfoIndexType<ParadoxEventInOnActionIndexInfo>(6)
    data object EventInEventUsage : ParadoxInfoIndexType<ParadoxEventInEventIndexInfo>(7)
    data object OnActionInEventUsage : ParadoxInfoIndexType<ParadoxOnActionInEventIndexInfo>(8)
}
