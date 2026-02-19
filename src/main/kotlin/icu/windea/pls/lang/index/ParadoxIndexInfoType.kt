package icu.windea.pls.lang.index

import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.index.ParadoxEventInEventIndexInfo
import icu.windea.pls.model.index.ParadoxEventInOnActionIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.model.index.ParadoxInferredScopeContextAwareDefinitionIndexInfo
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.model.index.ParadoxOnActionInEventIndexInfo
import icu.windea.pls.model.index.ParadoxParameterIndexInfo

abstract class ParadoxIndexInfoType<T : ParadoxIndexInfo>(val id: Byte) {
    data object DynamicValue : ParadoxIndexInfoType<ParadoxDynamicValueIndexInfo>(2)
    data object Parameter : ParadoxIndexInfoType<ParadoxParameterIndexInfo>(3)
    data object LocalisationParameter : ParadoxIndexInfoType<ParadoxLocalisationParameterIndexInfo>(4)

    data object InferredScopeContextAwareDefinition : ParadoxIndexInfoType<ParadoxInferredScopeContextAwareDefinitionIndexInfo>(5)
    data object EventInOnAction : ParadoxIndexInfoType<ParadoxEventInOnActionIndexInfo>(6)
    data object EventInEvent : ParadoxIndexInfoType<ParadoxEventInEventIndexInfo>(7)
    data object OnActionInEvent : ParadoxIndexInfoType<ParadoxOnActionInEventIndexInfo>(8)
}
