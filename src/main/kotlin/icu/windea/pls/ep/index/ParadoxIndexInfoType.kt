package icu.windea.pls.ep.index

import icu.windea.pls.model.indexInfo.*

abstract class ParadoxIndexInfoType<T : ParadoxIndexInfo>(val id: Byte) {
    data object ComplexEnumValue : ParadoxIndexInfoType<ParadoxComplexEnumValueIndexInfo>(1)
    data object DynamicValue : ParadoxIndexInfoType<ParadoxDynamicValueIndexInfo>(2)
    data object Parameter : ParadoxIndexInfoType<ParadoxParameterIndexInfo>(3)
    data object LocalisationParameter : ParadoxIndexInfoType<ParadoxLocalisationParameterIndexInfo>(4)

    data object InferredScopeContextAwareDefinition : ParadoxIndexInfoType<ParadoxInferredScopeContextAwareDefinitionIndexInfo>(5)
    data object EventInOnAction : ParadoxIndexInfoType<ParadoxEventInOnActionIndexInfo>(6)
    data object EventInEvent : ParadoxIndexInfoType<ParadoxEventInEventIndexInfo>(7)
    data object OnActionInEvent : ParadoxIndexInfoType<ParadoxOnActionInEventIndexInfo>(8)
}
